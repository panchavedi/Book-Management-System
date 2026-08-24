import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Book } from '../../models/book.model';
import { AuthService } from '../../services/auth.service';
import { BookService } from '../../services/book.service';
import { BorrowingStateService } from '../../services/borrowing-state.service';

interface DisplayBook {
  id: number | string;
  title: string;
  authorName: string;
  categoryName?: string;
  availableCopies: number;
  totalCopies: number;
  borrowedCopies: number;
  paletteClass: string;
  subtitle?: string;
}

const DEFAULT_FEATURED: DisplayBook[] = [
  {
    id: 1,
    title: 'The Silhouette of Light',
    authorName: 'Jane Doe',
    categoryName: 'Literature',
    availableCopies: 3,
    totalCopies: 4,
    borrowedCopies: 1,
    paletteClass: 'cover-sky',
    subtitle: 'A Novel of Solitude'
  },
  {
    id: 2,
    title: 'BRUTALISM',
    authorName: 'Marcus Aurelius',
    categoryName: 'Architecture',
    availableCopies: 2,
    totalCopies: 3,
    borrowedCopies: 1,
    paletteClass: 'cover-mustard',
    subtitle: 'Architecture in the Raw'
  },
  {
    id: 3,
    title: "HORIZON'S DAWN",
    authorName: 'Elena Vance',
    categoryName: 'Fiction',
    availableCopies: 4,
    totalCopies: 5,
    borrowedCopies: 1,
    paletteClass: 'cover-teal',
    subtitle: 'A Journey to Tranquility'
  },
  {
    id: 4,
    title: 'The Indigo Hue',
    authorName: 'Samuel T. Coleridge',
    categoryName: 'Poetry',
    availableCopies: 1,
    totalCopies: 2,
    borrowedCopies: 1,
    paletteClass: 'cover-amber',
    subtitle: 'Echoes of Autumn'
  },
  {
    id: 5,
    title: 'CONCRETE CANVAS',
    authorName: 'Marcus Aurelius',
    categoryName: 'Design',
    availableCopies: 3,
    totalCopies: 3,
    borrowedCopies: 0,
    paletteClass: 'cover-purple',
    subtitle: 'Urban Metaphors'
  }
];

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class Dashboard implements OnInit {
  private readonly booksApi = inject(BookService);
  readonly auth = inject(AuthService);
  readonly borrowing = inject(BorrowingStateService);

  readonly books = signal<Book[]>([]);
  readonly loading = signal(true);

  readonly totalBooks = computed(() => this.books().length);
  readonly totalCopies = computed(() => this.books().reduce((sum, book) => sum + book.totalCopies, 0));
  readonly availableCopies = computed(() => this.books().reduce((sum, book) => sum + book.availableCopies, 0));
  readonly borrowedCopies = computed(() => this.borrowing.activeBorrowingCount());
  readonly borrowedCategory = computed(() => this.borrowing.borrowedCategoryName());
  readonly goalProgress = computed(() => Math.min((this.borrowedCopies() / this.borrowing.maxBooks) * 100, 100));

  readonly goalSubtitle = computed(() =>
    this.borrowedCopies() >= this.borrowing.maxBooks
      ? 'Borrowing limit reached.'
      : "You're almost there!"
  );

  readonly displayBooks = computed<DisplayBook[]>(() => {
    const raw = this.books();
    if (raw.length === 0) {
      return DEFAULT_FEATURED;
    }

    const palettes = ['cover-sky', 'cover-mustard', 'cover-teal', 'cover-amber', 'cover-purple'];
    return raw.slice(0, 5).map((b, idx) => ({
      id: b.id,
      title: b.title,
      authorName: b.authorName || 'Unknown author',
      categoryName: b.categoryName,
      availableCopies: b.availableCopies,
      totalCopies: b.totalCopies,
      borrowedCopies: b.borrowedCopies ?? 0,
      paletteClass: palettes[idx % palettes.length],
      subtitle: b.categoryName || 'Edition'
    }));
  });

  ngOnInit(): void {
    this.booksApi.list({ size: 12 }).subscribe({
      next: (books) => {
        this.books.set(books);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }
}

