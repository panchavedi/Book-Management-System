import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Book } from '../../../models/book.model';
import { AuthService } from '../../../services/auth.service';
import { BookService } from '../../../services/book.service';
import { BorrowingStateService } from '../../../services/borrowing-state.service';
import { ToastService } from '../../../services/toast.service';

@Component({
  selector: 'app-book-detail',
  imports: [RouterLink, DatePipe],
  templateUrl: './book-detail.html',
  styleUrl: './book-detail.scss'
})
export class BookDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly booksApi = inject(BookService);
  private readonly toast = inject(ToastService);
  readonly auth = inject(AuthService);
  readonly borrowing = inject(BorrowingStateService);

  readonly book = signal<Book | null>(null);
  readonly loading = signal(true);
  readonly working = signal(false);
  readonly isBorrowedByMe = computed(() => {
    const book = this.book();
    return !!book && this.borrowing.isBorrowed(book.id);
  });
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
        const message = this.borrowing.restrictionMessage(book);
        if (message) {
          this.toast.show(message, 'info');
        }
      }
      return;
    }

    this.working.set(true);
    this.booksApi.borrow(book.id).subscribe({
      next: (borrowing) => {
        this.borrowing.markBorrowed(book, borrowing);
        this.refreshAfterAction('Book borrowed successfully.');
      },
      error: (err) => {
        this.working.set(false);
        const msg = err?.error?.message || err?.error?.detail || 'Could not borrow this book.';
        this.toast.show(msg, 'error');
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
        this.refreshAfterAction('Book returned. You can now borrow another book.');
      },
      error: (err) => {
        this.working.set(false);
        const msg = err?.error?.message || err?.error?.detail || 'Could not return this book.';
        this.toast.show(msg, 'error');
      }
    });
  }

  private load(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.booksApi.get(id).subscribe({
      next: (book) => {
        this.borrowing.syncBook(book);
        this.book.set(book);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.toast.show('Could not load this book.', 'error');
      }
    });
  }

  private refreshAfterAction(message: string): void {
    const book = this.book();
    if (!book) {
      this.working.set(false);
      return;
    }

    this.booksApi.get(book.id).subscribe({
      next: (updated) => {
        this.book.set(updated);
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
