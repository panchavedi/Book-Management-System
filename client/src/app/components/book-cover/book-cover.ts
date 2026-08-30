import { Component, Input, OnChanges, OnDestroy, SimpleChanges, inject, signal } from '@angular/core';
import { BookImage } from '../../models/book.model';
import { BookService } from '../../services/book.service';

@Component({
  selector: 'app-book-cover',
  templateUrl: './book-cover.html',
  styleUrl: './book-cover.scss'
})
export class BookCoverComponent implements OnChanges, OnDestroy {
  @Input({ required: true }) title = '';
  @Input() images: BookImage[] = [];
  @Input() size: 'small' | 'medium' | 'large' = 'medium';
  @Input() eager = false;

  readonly imageUrl = signal<string | null>(null);
  readonly loading = signal(false);
  private readonly books = inject(BookService);
  private objectUrl: string | null = null;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['images']) this.loadFirstImage();
  }

  ngOnDestroy(): void {
    this.revoke();
  }

  private loadFirstImage(): void {
    this.revoke();
    const image = [...(this.images ?? [])].sort((a, b) => a.displayOrder - b.displayOrder)[0];
    if (!image) {
      this.loading.set(false);
      this.imageUrl.set(null);
      return;
    }

    this.loading.set(true);
    this.books.loadImageBlob(image).subscribe({
      next: (url) => {
        this.objectUrl = url;
        this.imageUrl.set(url);
        this.loading.set(false);
      },
      error: () => {
        this.imageUrl.set(null);
        this.loading.set(false);
      }
    });
  }

  private revoke(): void {
    if (this.objectUrl) URL.revokeObjectURL(this.objectUrl);
    this.objectUrl = null;
  }
}
