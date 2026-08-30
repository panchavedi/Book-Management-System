import { DatePipe } from '@angular/common';
import {
  Component,
  OnInit,
  inject,
  signal
} from '@angular/core';
import { RouterLink } from '@angular/router';

import { Borrowing } from '../../models/book.model';
import { BorrowingApiService } from '../../services/borrowing-api.service';
import { ToastService } from '../../services/toast.service';

type ViewMode = 'active' | 'history';

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

  readonly mode = signal<ViewMode>('active');
  readonly activeBorrowings = signal<Borrowing[]>([]);
  readonly history = signal<Borrowing[]>([]);
  readonly books = signal<BookBorrowingGroup[]>([]);
  readonly loading = signal(true);

  ngOnInit(): void {
    this.loadActive();
  }

  setMode(mode: ViewMode): void {
    this.mode.set(mode);

    if (mode === 'history' && !this.history().length) {
      this.loadHistory();
    }
  }

  refresh(): void {
    if (this.mode() === 'active') {
      this.loadActive();
      return;
    }

    this.loadHistory();
  }

  toggleBook(group: BookBorrowingGroup): void {
    this.books.update((groups) =>
      groups.map((item) =>
        item.bookId === group.bookId
          ? { ...item, open: !item.open }
          : item
      )
    );

    if (!group.open) {
      this.loadBookBorrowers(group.bookId);
    }
  }

  private loadActive(): void {
    this.loading.set(true);

    this.api.adminActive('desc').subscribe({
      next: (items) => {
        this.activeBorrowings.set(items);

        const map = new Map<number, BookBorrowingGroup>();

        for (const item of items) {
          const group = map.get(item.bookId);

          if (group) {
            group.activeCount++;
            continue;
          }

          map.set(item.bookId, {
            bookId: item.bookId,
            bookTitle: item.bookTitle,
            activeCount: 1,
            open: false,
            borrowers: [],
            loading: false
          });
        }

        this.books.set([...map.values()]);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.toast.show(
          'Could not load active borrowings.',
          'error'
        );
      }
    });
  }

  private loadHistory(): void {
    this.loading.set(true);

    this.api.adminHistory().subscribe({
      next: (items) => {
        this.history.set(items);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.toast.show(
          'Could not load borrowing history.',
          'error'
        );
      }
    });
  }

  private loadBookBorrowers(bookId: number): void {
    this.books.update((groups) =>
      groups.map((group) =>
        group.bookId === bookId
          ? { ...group, loading: true }
          : group
      )
    );

    this.api.activeByBook(bookId).subscribe({
      next: (borrowers) => {
        this.books.update((groups) =>
          groups.map((group) =>
            group.bookId === bookId
              ? {
                  ...group,
                  borrowers,
                  loading: false
                }
              : group
          )
        );
      },
      error: () => {
        this.books.update((groups) =>
          groups.map((group) =>
            group.bookId === bookId
              ? { ...group, loading: false }
              : group
          )
        );
        this.toast.show(
          'Could not load borrowers.',
          'error'
        );
      }
    });
  }
}
