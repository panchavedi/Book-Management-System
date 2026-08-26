import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged, finalize, map, startWith, switchMap } from 'rxjs';
import { Book, EntityOption } from '../../../models/book.model';
import { AuthService } from '../../../services/auth.service';
import { BookService } from '../../../services/book.service';
import { EntityService } from '../../../services/entity.service';
import { ToastService } from '../../../services/toast.service';
import { BookCardComponent } from '../../../components/book-card/book-card';
import { BorrowingStateService } from '../../../services/borrowing-state.service';

type FilterKind = 'author' | 'category' | null;

@Component({
  selector: 'app-book-list',
  imports: [ReactiveFormsModule, RouterLink, BookCardComponent],
  templateUrl: './book-list.html',
  styleUrl: './book-list.scss'
})
export class BookList implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly booksApi = inject(BookService);
  private readonly entities = inject(EntityService);
  private readonly toast = inject(ToastService);
  readonly auth = inject(AuthService);
  readonly borrowing = inject(BorrowingStateService);

  readonly books = signal<Book[]>([]);
  readonly loading = signal(true);
  readonly activeFilter = signal<FilterKind>(null);
  readonly selectedAuthor = signal<EntityOption | null>(null);
  readonly selectedCategory = signal<EntityOption | null>(null);
  readonly entityOptions = signal<EntityOption[]>([]);
  readonly entityLoading = signal(false);

  readonly filters = this.fb.nonNullable.group({
    keyword: ['']
  });

  ngOnInit(): void {
    this.filters.controls.keyword.valueChanges
      .pipe(
        startWith(this.filters.controls.keyword.value),
        debounceTime(250),
        distinctUntilChanged(),
        switchMap((keyword) => this.queryUnifiedSearch(keyword))
      )
      .subscribe({
        next: (books) => this.books.set(books),
        error: () => {
          this.books.set([]);
          this.loading.set(false);
          this.entityLoading.set(false);
          this.toast.show('Could not load search results.', 'error');
        }
      });
  }

  toggleFilter(kind: Exclude<FilterKind, null>): void {
    if (this.activeFilter() === kind) {
      this.activeFilter.set(null);
      this.selectedAuthor.set(null);
      this.selectedCategory.set(null);
      this.entityOptions.set([]);
      this.filters.controls.keyword.setValue('', { emitEvent: true });
      return;
    }

    this.activeFilter.set(kind);
    this.selectedAuthor.set(null);
    this.selectedCategory.set(null);
    this.entityOptions.set([]);
    this.filters.controls.keyword.setValue('', { emitEvent: false });
    this.loadEntities('');
  }

  selectEntity(option: EntityOption): void {
    if (this.activeFilter() === 'author') {
      this.selectedAuthor.set(option);
      this.selectedCategory.set(null);
      this.loadByAuthor(option.id);
    } else if (this.activeFilter() === 'category') {
      this.selectedCategory.set(option);
      this.selectedAuthor.set(null);
      this.loadByCategory(option.id);
    }

    this.entityOptions.set([]);
    this.filters.controls.keyword.setValue(option.name, { emitEvent: false });
  }

  clearEntitySelection(): void {
    this.activeFilter.set(null);
    this.selectedAuthor.set(null);
    this.selectedCategory.set(null);
    this.entityOptions.set([]);
    this.filters.controls.keyword.setValue('', { emitEvent: true });
  }

  private queryUnifiedSearch(keyword: string) {
    const query = keyword.trim();
    const filter = this.activeFilter();

    if (filter) {
      // Typing after selection starts a new entity search using the same single search box.
      if (filter === 'author' && this.selectedAuthor()?.name !== keyword) {
        this.selectedAuthor.set(null);
      }
      if (filter === 'category' && this.selectedCategory()?.name !== keyword) {
        this.selectedCategory.set(null);
      }

      this.loading.set(false);
      return this.searchEntities(query);
    }

    this.loading.set(true);
    this.entityOptions.set([]);
    return (query ? this.booksApi.search({ keyword: query }) : this.booksApi.list({ size: 60 }))
      .pipe(finalize(() => this.loading.set(false)));
  }

  private loadEntities(query: string): void {
    this.entityLoading.set(true);
    const search$ = this.activeFilter() === 'author'
      ? this.entities.searchAuthors(query)
      : this.entities.searchCategories(query);

    search$.subscribe({
      next: (options) => {
        this.entityOptions.set(options);
        this.entityLoading.set(false);
      },
      error: () => {
        this.entityOptions.set([]);
        this.entityLoading.set(false);
        this.toast.show(`Could not search ${this.activeFilter() || 'entities'}.`, 'error');
      }
    });
  }

  private searchEntities(query: string) {
    this.entityLoading.set(true);
    this.entityOptions.set([]);
    const search$ = this.activeFilter() === 'author'
      ? this.entities.searchAuthors(query)
      : this.entities.searchCategories(query);

    return search$.pipe(
      map((options) => {
        this.entityOptions.set(options);
        this.entityLoading.set(false);
        const selected = this.activeFilter() === 'author' ? this.selectedAuthor() : this.selectedCategory();
        if (selected) {
          this.loading.set(false);
          return this.activeFilter() === 'author'
            ? []
            : [];
        }
        this.loading.set(false);
        return this.books();
      }),
      finalize(() => this.entityLoading.set(false))
    );
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

  private loadByAuthor(authorId: number): void {
    this.loading.set(true);
    this.booksApi.byAuthor(authorId).pipe(finalize(() => this.loading.set(false))).subscribe({
      next: (books) => this.books.set(books),
      error: () => {
        this.books.set([]);
        this.toast.show('Could not filter books by author.', 'error');
      }
    });
  }

  private loadByCategory(categoryId: number): void {
    this.loading.set(true);
    this.booksApi.byCategory(categoryId).pipe(finalize(() => this.loading.set(false))).subscribe({
      next: (books) => this.books.set(books),
      error: () => {
        this.books.set([]);
        this.toast.show('Could not filter books by category.', 'error');
      }
    });
  }

}
