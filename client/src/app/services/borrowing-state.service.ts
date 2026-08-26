import { Injectable, computed, inject, signal } from '@angular/core';
import { Book, Borrowing } from '../models/book.model';
import { AuthService } from './auth.service';

export interface ActiveBorrowing {
  bookId: number;
  title: string;
  isbn: string;
  categoryId: number;
  categoryName: string;
  borrowedOn: string;
}

@Injectable({ providedIn: 'root' })
export class BorrowingStateService {
  readonly maxBooks = 5;
  private readonly storagePrefix = 'bms_active_borrowings';
  private readonly auth = inject(AuthService);

  readonly activeBorrowings = signal<ActiveBorrowing[]>(this.read());
  readonly activeBorrowingCount = computed(() => this.activeBorrowings().length);
  readonly hasActiveBorrowing = computed(() => this.activeBorrowingCount() > 0);
  readonly hasReachedLimit = computed(() => this.activeBorrowingCount() >= this.maxBooks);
  readonly borrowedCategoryId = computed(() => {
    const categoryId = this.activeBorrowings()[0]?.categoryId;
    return categoryId && categoryId > 0 ? categoryId : null;
  });
  readonly borrowedCategoryName = computed(() => {
    const category = this.activeBorrowings()[0];
    return category?.categoryId && category.categoryId > 0 ? category.categoryName : null;
  });

  isBorrowed(bookId: number): boolean {
    return this.activeBorrowings().some((item) => item.bookId === bookId);
  }

  canBorrow(book: Book): boolean {
    if (this.isBorrowed(book.id) || book.availableCopies <= 0 || this.hasReachedLimit()) {
      return false;
    }

    const categoryId = this.borrowedCategoryId();
    return categoryId === null || categoryId === book.categoryId;
  }

  restrictionMessage(book: Book): string | null {
    if (this.isBorrowed(book.id)) {
      return 'You already have this book.';
    }

    if (book.availableCopies <= 0) {
      return 'No copies are currently available.';
    }

    if (this.hasReachedLimit()) {
      return 'You have reached the 5-book borrowing limit. Return a book before borrowing another.';
    }

    const categoryName = this.borrowedCategoryName();
    if (categoryName && this.borrowedCategoryId() !== book.categoryId) {
      return `Your current borrowing category is ${categoryName}. Return all ${categoryName} books before choosing another category.`;
    }

    return null;
  }


  syncBook(book: Book): void {
    const current = this.activeBorrowings();
    const index = current.findIndex((item) => item.bookId === book.id);
    if (index < 0) {
      return;
    }

    const existing = current[index];
    if (existing.categoryId === book.categoryId && existing.categoryName === book.categoryName) {
      return;
    }

    const next = [...current];
    next[index] = {
      ...existing,
      title: book.title,
      isbn: book.isbn,
      categoryId: book.categoryId,
      categoryName: book.categoryName
    };
    this.persist(next);
    this.activeBorrowings.set(next);
  }


  syncFromServer(borrowings: Borrowing[]): void {
    const current = this.activeBorrowings();
    const next: ActiveBorrowing[] = borrowings.map((borrowing) => {
      const existing = current.find((item) => item.bookId === borrowing.bookId);
      return {
        bookId: borrowing.bookId,
        title: borrowing.bookTitle || existing?.title || `Book #${borrowing.bookId}`,
        isbn: borrowing.isbn ?? existing?.isbn ?? '',
        categoryId: borrowing.categoryId ?? existing?.categoryId ?? 0,
        categoryName: borrowing.categoryName ?? existing?.categoryName ?? 'Current category',
        borrowedOn: borrowing.borrowedOn
      };
    });

    this.persist(next);
    this.activeBorrowings.set(next);
  }

  markBorrowed(book: Book, response?: Borrowing): void {
    if (!this.canBorrow(book)) {
      return;
    }

    const borrowing: ActiveBorrowing = {
      bookId: book.id,
      title: book.title,
      isbn: book.isbn,
      categoryId: book.categoryId,
      categoryName: book.categoryName,
      borrowedOn: response?.borrowedOn ?? new Date().toISOString()
    };

    const next = [...this.activeBorrowings().filter((item) => item.bookId !== book.id), borrowing];
    this.persist(next);
    this.activeBorrowings.set(next);
  }

  markReturned(bookId: number): void {
    const next = this.activeBorrowings().filter((item) => item.bookId !== bookId);
    this.persist(next);
    this.activeBorrowings.set(next);
  }

  private storageKey(): string {
    const userId = this.auth.currentUser()?.id ?? 'guest';
    return `${this.storagePrefix}_${userId}`;
  }

  private persist(items: ActiveBorrowing[]): void {
    if (items.length) {
      localStorage.setItem(this.storageKey(), JSON.stringify(items));
    } else {
      localStorage.removeItem(this.storageKey());
    }
  }

  private read(): ActiveBorrowing[] {
    const userId = this.auth.currentUser()?.id ?? 'guest';
    const currentKey = `${this.storagePrefix}_${userId}`;
    const raw = localStorage.getItem(currentKey);

    if (raw) {
      try {
        const parsed = JSON.parse(raw);
        if (Array.isArray(parsed)) {
          return parsed.filter((item) => item && typeof item.bookId === 'number');
        }
      } catch {
        localStorage.removeItem(currentKey);
      }
    }

    // Migrate the previous single-borrowing format if it exists.
    const legacyRaw = localStorage.getItem('bms_active_borrowing');
    if (!legacyRaw) {
      return [];
    }

    try {
      const legacy = JSON.parse(legacyRaw) as Partial<ActiveBorrowing>;
      if (legacy.bookId && legacy.title) {
        const migrated: ActiveBorrowing = {
          bookId: legacy.bookId,
          title: legacy.title,
          isbn: legacy.isbn ?? '',
          categoryId: legacy.categoryId ?? 0,
          categoryName: legacy.categoryName ?? 'Current category',
          borrowedOn: legacy.borrowedOn ?? new Date().toISOString()
        };
        this.persist([migrated]);
        localStorage.removeItem('bms_active_borrowing');
        return [migrated];
      }
    } catch {
      localStorage.removeItem('bms_active_borrowing');
    }

    return [];
  }
}
