import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { debounceTime, startWith, switchMap } from 'rxjs';
import { Book, BookSearchParams } from '../../../models/book.model';
import { AuthService } from '../../../services/auth.service';
import { BookService } from '../../../services/book.service';
import { ToastService } from '../../../services/toast.service';
import { BookCardComponent } from '../../../components/book-card/book-card';
import { BorrowingStateService } from '../../../services/borrowing-state.service';

@Component({
  selector: 'app-book-list',
  imports: [ReactiveFormsModule, RouterLink, BookCardComponent],
  templateUrl: './book-list.html',
  styleUrl: './book-list.scss'
})
export class BookList implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly booksApi = inject(BookService);
  private readonly toast = inject(ToastService);
  readonly auth = inject(AuthService);
  readonly borrowing = inject(BorrowingStateService);

  readonly books = signal<Book[]>([]);
  readonly loading = signal(true);

  readonly filters = this.fb.nonNullable.group({
    keyword: ['']
  });

  ngOnInit(): void {
    this.filters.valueChanges
      .pipe(
        startWith(this.filters.getRawValue()),
        debounceTime(250),
        switchMap((filters) => {
          const params: BookSearchParams = {
            keyword: filters.keyword ?? ''
          };
          this.loading.set(true);
          return params.keyword ? this.booksApi.search(params) : this.booksApi.list({ size: 60 });
        })
      )
      .subscribe({
        next: (books) => {
          this.books.set(books);
          this.loading.set(false);
        },
        error: () => {
          this.books.set([]);
          this.loading.set(false);
          this.toast.show('Could not load books from the book service.', 'error');
        }
      });
  }

  delete(book: Book): void {
    if (!window.confirm(`Delete "${book.title}" from the catalog?`)) {
      return;
    }

    this.booksApi.delete(book.id).subscribe({
      next: () => {
        this.books.update((books) => books.filter((item) => item.id !== book.id));
        this.toast.show('Book deleted.', 'success');
      },
      error: (err) => {
        const msg = err?.error?.message || err?.error?.detail || 'Could not delete this book.';
        this.toast.show(msg, 'error');
      }
    });
  }
}
