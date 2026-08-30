import { CommonModule } from '@angular/common';
import {
  Component,
  OnInit,
  computed,
  inject,
  signal
} from '@angular/core';
import { RouterLink } from '@angular/router';

import { BookCoverComponent } from '../../components/book-cover/book-cover';
import { Book } from '../../models/book.model';
import { AuthService } from '../../services/auth.service';
import { BookService } from '../../services/book.service';
import { BorrowingApiService } from '../../services/borrowing-api.service';
import { BorrowingStateService } from '../../services/borrowing-state.service';

@Component({
  selector: 'app-dashboard',
  imports: [
    RouterLink,
    BookCoverComponent,
    CommonModule
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class Dashboard implements OnInit {
  private readonly booksApi = inject(BookService);
  private readonly borrowingApi = inject(BorrowingApiService);

  readonly auth = inject(AuthService);
  readonly borrowing = inject(BorrowingStateService);
  readonly books = signal<Book[]>([]);
  readonly loading = signal(true);

  readonly totalCopies = computed(() =>
    this.books().reduce(
      (total, book) => total + book.totalCopies,
      0
    )
  );

  readonly availableCopies = computed(() =>
    this.books().reduce(
      (total, book) => total + book.availableCopies,
      0
    )
  );

  readonly borrowedCopies = computed(() =>
    this.borrowing.activeBorrowingCount()
  );

  readonly featured = computed(() =>
    this.books().slice(0, 4)
  );

  readonly goalProgress = computed(() =>
    Math.min(
      (this.borrowedCopies() / this.borrowing.maxBooks) * 100,
      100
    )
  );

  ngOnInit(): void {
    this.borrowingApi.myActive().subscribe({
      next: (items) => this.borrowing.syncFromServer(items),
      error: () => {}
    });

    this.booksApi.list({ size: 20 }).subscribe({
      next: (books) => {
        this.books.set(books);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }
}
