import { CommonModule } from '@angular/common';
import {
  Component,
  OnDestroy,
  OnInit,
  computed,
  inject,
  signal
} from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import { EntityTypeaheadComponent } from '../../../components/entity-typeahead/entity-typeahead';
import {
  Book,
  BookImage,
  BookRequest,
  EntityOption
} from '../../../models/book.model';
import { BookService } from '../../../services/book.service';
import { ToastService } from '../../../services/toast.service';

interface PreviewImage {
  id?: number;
  name: string;
  src: string;
  file?: File;
}

@Component({
  selector: 'app-book-form',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    EntityTypeaheadComponent
  ],
  templateUrl: './book-form.html',
  styleUrl: './book-form.scss'
})
export class BookForm implements OnInit, OnDestroy {
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
  readonly previews = signal<PreviewImage[]>([]);
  readonly removedImageIds = signal<number[]>([]);

  readonly form = this.fb.nonNullable.group({
    title: [
      '',
      [Validators.required, Validators.maxLength(250)]
    ],
    isbn: [
      '',
      [Validators.required, Validators.maxLength(20)]
    ],
    publisher: ['', [Validators.maxLength(200)]],
    printedOn: [''],
    totalCopies: [
      1,
      [Validators.required, Validators.min(1)]
    ],
    about: ['', [Validators.maxLength(5000)]]
  });

  private objectUrls: string[] = [];

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    if (!Number.isFinite(id) || id <= 0) {
      return;
    }

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

  ngOnDestroy(): void {
    this.objectUrls.forEach((url) => URL.revokeObjectURL(url));
  }

  onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const files = Array.from(input.files ?? []);

    if (!files.length) {
      return;
    }

    const current = this.previews().length;
    const available = 5 - current;

    if (files.length > available) {
      this.toast.show(
        `You can keep a maximum of 5 images per book. Select ${available} more.`,
        'error'
      );
      input.value = '';
      return;
    }

    const valid = files.filter(
      (file) =>
        file.type.startsWith('image/') &&
        file.size <= 10 * 1024 * 1024
    );

    if (valid.length !== files.length) {
      this.toast.show(
        'Only image files up to 10 MB are accepted.',
        'error'
      );
    }

    const next = [...this.previews()];

    for (const file of valid) {
      const src = URL.createObjectURL(file);
      this.objectUrls.push(src);
      next.push({
        name: file.name,
        src,
        file
      });
    }

    this.previews.set(next);
    input.value = '';
  }

  removeImage(index: number): void {
    const image = this.previews()[index];

    if (image?.id) {
      this.removedImageIds.update((ids) =>
        ids.includes(image.id!)
          ? ids
          : [...ids, image.id!]
      );
    }

    if (image?.src.startsWith('blob:')) {
      URL.revokeObjectURL(image.src);
      this.objectUrls = this.objectUrls.filter(
        (url) => url !== image.src
      );
    }

    this.previews.update((items) =>
      items.filter((_, itemIndex) => itemIndex !== index)
    );
  }

  save(): void {
    if (
      this.form.invalid ||
      !this.selectedAuthor() ||
      !this.selectedCategory() ||
      this.saving()
    ) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();

    if (
      raw.printedOn &&
      raw.printedOn > new Date().toISOString().slice(0, 10)
    ) {
      this.toast.show(
        'Printed date cannot be in the future.',
        'error'
      );
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

    if (!this.isEdit()) {
      this.booksApi.create(request, this.filePreviews()).subscribe({
        next: (book) => {
          this.toast.show(
            'Book added to the catalog.',
            'success'
          );
          void this.router.navigate(['/books', book.id]);
        },
        error: (error) => {
          this.saving.set(false);
          this.toast.show(
            error?.error?.message || 'Could not save this book.',
            'error'
          );
        }
      });
      return;
    }

    const id = this.bookId()!;

    this.booksApi.update(id, request).subscribe({
      next: () => this.finishImageEdits(id),
      error: (error) => {
        this.saving.set(false);
        this.toast.show(
          error?.error?.message || 'Could not update this book.',
          'error'
        );
      }
    });
  }

  private async finishImageEdits(id: number): Promise<void> {
    try {
      for (const imageId of this.removedImageIds()) {
        await firstValueFrom(
          this.booksApi.deleteImage(id, imageId)
        );
      }

      const additions = this.filePreviews();

      if (additions.length) {
        await firstValueFrom(
          this.booksApi.addImages(id, additions)
        );
      }

      const book = await firstValueFrom(this.booksApi.get(id));

      this.saving.set(false);
      this.toast.show('Book updated.', 'success');
      void this.router.navigate(['/books', book.id]);
    } catch (error: any) {
      this.saving.set(false);
      this.toast.show(
        error?.error?.message ||
          'Book details were saved, but image changes could not be completed.',
        'error'
      );
    }
  }

  private filePreviews(): File[] {
    return this.previews()
      .filter((item) => !!item.file)
      .map((item) => item.file!);
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
      this.selectedAuthor.set({
        id: book.authorId,
        name: book.authorName
      });
    }

    if (book.categoryId && book.categoryName) {
      this.selectedCategory.set({
        id: book.categoryId,
        name: book.categoryName
      });
    }

    this.loadExistingImages(book.images ?? []);
  }

  private loadExistingImages(images: BookImage[]): void {
    for (const image of images) {
      this.booksApi.loadImageBlob(image).subscribe({
        next: (src) => {
          this.objectUrls.push(src);
          this.previews.update((items) => [
            ...items,
            {
              id: image.id,
              name:
                image.originalFileName ||
                `Image ${image.displayOrder + 1}`,
              src
            }
          ]);
        }
      });
    }
  }
}
