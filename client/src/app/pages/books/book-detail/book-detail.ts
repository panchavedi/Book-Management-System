import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Book, Borrowing } from '../../../models/book.model';
import { AuthService } from '../../../services/auth.service';
import { BookService } from '../../../services/book.service';
import { BorrowingStateService } from '../../../services/borrowing-state.service';
import { ToastService } from '../../../services/toast.service';
import { BorrowingApiService } from '../../../services/borrowing-api.service';
import { BookCoverComponent } from '../../../components/book-cover/book-cover';

@Component({
  selector: 'app-book-detail',
  imports: [RouterLink, DatePipe, BookCoverComponent],
  templateUrl: './book-detail.html',
  styleUrl: './book-detail.scss'
})
export class BookDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly booksApi = inject(BookService);
  private readonly toast = inject(ToastService);
  private readonly borrowingsApi = inject(BorrowingApiService);

  readonly auth = inject(AuthService);
  readonly borrowing = inject(BorrowingStateService);
  readonly book = signal<Book | null>(null);
  readonly loading = signal(true);
  readonly working = signal(false);
  readonly activeBorrowers = signal<Borrowing[]>([]);
  readonly borrowersLoading = signal(false);
  readonly isStaff = computed(() => this.auth.isStaff());
  readonly isBorrowedByMe = computed(() => this.book()?.borrowed === true);
  readonly canBorrow = computed(() => {
    const book = this.book();
    return !!book && this.borrowing.canBorrow(book);
  });
  readonly borrowMessage = computed(() => {
    const book = this.book();
    return book ? this.borrowing.restrictionMessage(book) : null;
  });

  ngOnInit(): void {
    this.load();
  }

  borrow(): void {
    const book = this.book();
    if (!book || !this.canBorrow() || this.working()) {
      if (book) {
        const restriction = this.borrowing.restrictionMessage(book);
        if (restriction) {
          this.toast.show(restriction, 'info');
        }
      }
      return;
    }

    this.working.set(true);
    this.booksApi.borrow(book.id).subscribe({
      next: (response) => {
        this.borrowing.markBorrowed(book, response);
        this.refresh('Book borrowed successfully.');
      },
      error: (error) => {
        this.working.set(false);
        this.toast.show(error?.error?.message || 'Could not borrow this book.', 'error');
      }
    });
  }

  returnBook(): void {
    const book = this.book();
    if (!book || this.working()) {
      return;
    }

    this.working.set(true);
    this.booksApi.returnBook(book.id).subscribe({
      next: () => {
        this.borrowing.markReturned(book.id);
        this.refresh('Book returned.');
      },
      error: (error) => {
        this.working.set(false);
        this.toast.show(error?.error?.message || 'Could not return this book.', 'error');
      }
    });
  }

  private load(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.booksApi.get(id).subscribe({
      next: (book) => {
        this.book.set(book);
        this.borrowing.syncBook(book);
        this.loading.set(false);
        if (this.isStaff()) {
          this.loadActiveBorrowers(book.id);
        }
      },
      error: () => {
        this.loading.set(false);
        this.toast.show('Could not load this book.', 'error');
      }
    });
  }

  private loadActiveBorrowers(id: number): void {
    this.borrowersLoading.set(true);
    this.borrowingsApi.activeByBook(id).subscribe({
      next: (borrowings) => {
        this.activeBorrowers.set(borrowings);
        this.borrowersLoading.set(false);
      },
      error: () => {
        this.activeBorrowers.set([]);
        this.borrowersLoading.set(false);
      }
    });
  }

  private refresh(message: string): void {
    const book = this.book();
    if (!book) {
      this.working.set(false);
      return;
    }

    this.booksApi.get(book.id).subscribe({
      next: (nextBook) => {
        this.book.set(nextBook);
        this.borrowing.syncBook(nextBook);
        this.working.set(false);
        this.toast.show(message, 'success');
      },
      error: () => {
        this.working.set(false);
        this.toast.show(message, 'success');
      }
    });
  }
}
