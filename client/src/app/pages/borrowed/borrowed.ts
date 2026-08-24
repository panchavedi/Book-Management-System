import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { BorrowingStateService } from '../../services/borrowing-state.service';
import { BookService } from '../../services/book.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-borrowed',
  imports: [RouterLink, DatePipe],
  templateUrl: './borrowed.html',
  styleUrl: './borrowed.scss'
})
export class Borrowed {
  private readonly booksApi = inject(BookService);
  private readonly toast = inject(ToastService);
  readonly borrowing = inject(BorrowingStateService);
  readonly loading = signal(false);
  readonly workingBookId = signal<number | null>(null);

  returnBook(bookId: number): void {
    if (this.workingBookId() !== null) {
      return;
    }

    this.workingBookId.set(bookId);
    this.booksApi.returnBook(bookId).subscribe({
      next: () => {
        this.borrowing.markReturned(bookId);
        this.workingBookId.set(null);
        this.toast.show('Book returned successfully.', 'success');
      },
      error: (err) => {
        this.workingBookId.set(null);
        const msg = err?.error?.message || err?.error?.detail || 'Could not return this book.';
        this.toast.show(msg, 'error');
      }
    });
  }
}
