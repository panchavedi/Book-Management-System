export interface EntityOption {
  id: number;
  name: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface Author extends EntityOption {}

export interface Category extends EntityOption {}

export type BookAvailability = 'AVAILABLE' | 'BORROWED' | 'UNAVAILABLE';

export interface Book {
  id: number;
  title: string;
  isbn: string;
  authorId: number;
  authorName: string;
  categoryId: number;
  categoryName: string;
  publisher: string | null;
  printedOn: string | null;
  totalCopies: number;
  availableCopies: number;
  borrowedCopies: number;
  about: string | null;
  status?: BookAvailability;
}

export interface BookRequest {
  title: string;
  isbn: string;
  authorId: number;
  categoryId: number;
  publisher?: string;
  printedOn?: string;
  totalCopies: number;
  about?: string;
}

export interface BookSearchParams {
  keyword?: string;
  page?: number;
  size?: number;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface Borrowing {
  id: number;
  bookId: number;
  bookTitle: string;
  borrowerId: number;
  borrowedOn: string;
  returnedOn: string | null;
  status: 'BORROWED' | 'RETURNED' | string;
}
