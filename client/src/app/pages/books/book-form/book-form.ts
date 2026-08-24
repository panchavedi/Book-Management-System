import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Book, BookRequest, EntityOption } from '../../../models/book.model';
import { BookService } from '../../../services/book.service';
import { ToastService } from '../../../services/toast.service';
import { EntityTypeaheadComponent } from '../../../components/entity-typeahead/entity-typeahead';

@Component({
  selector: 'app-book-form',
  imports: [CommonModule, ReactiveFormsModule, RouterLink, EntityTypeaheadComponent],
  templateUrl: './book-form.html',
  styleUrl: './book-form.scss'
})
export class BookForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly booksApi = inject(BookService);
  private readonly toast = inject(ToastService);

  readonly bookId = signal<number | null>(null);
  readonly selectedAuthor = signal<EntityOption | null>(null);
  readonly selectedCategory = signal<EntityOption | null>(null);
  readonly saving = signal(false);
  readonly loading = signal(false);
  readonly isEdit = computed(() => this.bookId() !== null);

  readonly form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(250)]],
    isbn: ['', [Validators.required, Validators.maxLength(20)]],
    publisher: ['', Validators.maxLength(200)],
    printedOn: [''],
    totalCopies: [1, [Validators.required, Validators.min(1)]],
    about: ['', Validators.maxLength(5000)]
  });

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (Number.isFinite(id) && id > 0) {
      this.bookId.set(id);
      this.loading.set(true);
      this.booksApi.get(id).subscribe({
        next: (book) => {
          this.patch(book);
          this.loading.set(false);
        },
        error: () => {
          this.loading.set(false);
          this.toast.show('Could not load this book.', 'error');
        }
      });
    }
  }

  save(): void {
    if (this.form.invalid || !this.selectedAuthor() || !this.selectedCategory() || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    if (raw.printedOn && raw.printedOn > new Date().toISOString().slice(0, 10)) {
      this.toast.show('Printed date cannot be in the future.', 'error');
      return;
    }

    const request: BookRequest = {
      title: raw.title.trim(),
      isbn: raw.isbn.trim(),
      authorId: this.selectedAuthor()!.id,
      categoryId: this.selectedCategory()!.id,
      publisher: raw.publisher.trim(),
      printedOn: raw.printedOn || undefined,
      totalCopies: raw.totalCopies,
      about: raw.about.trim()
    };

    this.saving.set(true);
    const save$ = this.isEdit()
      ? this.booksApi.update(this.bookId()!, request)
      : this.booksApi.create(request);

    save$.subscribe({
      next: (book) => {
        this.toast.show(this.isEdit() ? 'Book updated.' : 'Book added to the catalog.', 'success');
        this.router.navigate(['/books', book.id]);
      },
      error: (err) => {
        this.saving.set(false);
        const msg = err?.error?.message || err?.error?.detail || (this.isEdit() ? 'Could not update this book.' : 'Could not save this book.');
        this.toast.show(msg, 'error');
      }
    });
  }

  private patch(book: Book): void {
    this.form.patchValue({
      title: book.title,
      isbn: book.isbn,
      publisher: book.publisher ?? '',
      printedOn: book.printedOn ?? '',
      totalCopies: book.totalCopies,
      about: book.about ?? ''
    });

    if (book.authorId && book.authorName) {
      this.selectedAuthor.set({ id: book.authorId, name: book.authorName });
    }

    if (book.categoryId && book.categoryName) {
      this.selectedCategory.set({ id: book.categoryId, name: book.categoryName });
    }
  }
}
