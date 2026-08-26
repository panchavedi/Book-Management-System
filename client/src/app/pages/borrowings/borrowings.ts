import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Borrowing } from '../../models/book.model';
import { BorrowingApiService } from '../../services/borrowing-api.service';
import { ToastService } from '../../services/toast.service';

interface BookBorrowingGroup {
  bookId: number;
  bookTitle: string;
  activeCount: number;
  open: boolean;
  borrowers: Borrowing[];
  loading: boolean;
}

@Component({
  selector: 'app-borrowings',
  imports: [DatePipe, RouterLink],
  templateUrl: './borrowings.html',
  styleUrl: './borrowings.scss'
})
export class Borrowings implements OnInit {
  private readonly api = inject(BorrowingApiService);
  private readonly toast = inject(ToastService);

  readonly activeBorrowings = signal<Borrowing[]>([]);
  readonly books = signal<BookBorrowingGroup[]>([]);
  readonly loading = signal(true);

  ngOnInit(): void {
    this.load();
  }

  toggleBook(group: BookBorrowingGroup): void {
    this.books.update((groups) => groups.map((item) => item.bookId === group.bookId ? { ...item, open: !item.open } : item));

    if (!group.open) {
      this.loadBookBorrowers(group.bookId);
    }
  }

  refresh(): void {
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.api.adminActive('desc').subscribe({
      next: (items) => {
        this.activeBorrowings.set(items);
        const grouped = new Map<number, BookBorrowingGroup>();
        items.forEach((item) => {
          const existing = grouped.get(item.bookId);
          if (existing) {
            existing.activeCount += 1;
          } else {
            grouped.set(item.bookId, {
              bookId: item.bookId,
              bookTitle: item.bookTitle,
              activeCount: 1,
              open: false,
              borrowers: [],
              loading: false
            });
          }
        });
        this.books.set([...grouped.values()]);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.toast.show('Could not load active borrowings.', 'error');
      }
    });
  }

  private loadBookBorrowers(bookId: number): void {
    this.books.update((groups) => groups.map((item) => item.bookId === bookId ? { ...item, loading: true } : item));
    this.api.activeByBook(bookId).subscribe({
      next: (borrowers) => this.books.update((groups) => groups.map((item) => item.bookId === bookId ? { ...item, borrowers, loading: false } : item)),
      error: () => {
        this.books.update((groups) => groups.map((item) => item.bookId === bookId ? { ...item, loading: false } : item));
        this.toast.show(`Could not load active borrowers for ${bookId}.`, 'error');
      }
    });
  }
}
