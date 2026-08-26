import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Borrowing } from '../../models/book.model';
import { BorrowingApiService } from '../../services/borrowing-api.service';
import { BorrowingStateService } from '../../services/borrowing-state.service';
import { BookService } from '../../services/book.service';
import { ToastService } from '../../services/toast.service';

type ReadingTab = 'active' | 'history';

@Component({
  selector: 'app-borrowed',
  imports: [RouterLink, DatePipe],
  templateUrl: './borrowed.html',
  styleUrl: './borrowed.scss'
})
export class Borrowed implements OnInit {
  private readonly booksApi = inject(BookService);
  private readonly borrowingApi = inject(BorrowingApiService);
  private readonly toast = inject(ToastService);
  readonly borrowing = inject(BorrowingStateService);

  readonly activeTab = signal<ReadingTab>('active');
  readonly active = signal<Borrowing[]>([]);
  readonly history = signal<Borrowing[]>([]);
  readonly loading = signal(true);
  readonly workingBookId = signal<number | null>(null);

  ngOnInit(): void {
    this.loadActive();
  }

  selectTab(tab: ReadingTab): void {
    this.activeTab.set(tab);
    if (tab === 'active' && this.active().length === 0) {
      this.loadActive();
    }
    if (tab === 'history' && this.history().length === 0) {
      this.loadHistory();
    }
  }

  returnBook(bookId: number): void {
    if (this.workingBookId() !== null) {
      return;
    }

    this.workingBookId.set(bookId);
    this.booksApi.returnBook(bookId).subscribe({
      next: () => {
        this.borrowing.markReturned(bookId);
        this.workingBookId.set(null);
        this.loadActive();
        this.loadHistory();
        this.toast.show('Book returned successfully.', 'success');
      },
      error: (err) => {
        this.workingBookId.set(null);
        const msg = err?.error?.message || err?.error?.detail || 'Could not return this book.';
        this.toast.show(msg, 'error');
      }
    });
  }

  private loadActive(): void {
    this.loading.set(true);
    this.borrowingApi.myActive().subscribe({
      next: (items) => {
        this.active.set(items);
        this.borrowing.syncFromServer(items);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.toast.show('Could not load your active borrowings.', 'error');
      }
    });
  }

  private loadHistory(): void {
    this.borrowingApi.myHistory().subscribe({
      next: (items) => this.history.set(items),
      error: () => this.toast.show('Could not load borrowing history.', 'error')
    });
  }
}
