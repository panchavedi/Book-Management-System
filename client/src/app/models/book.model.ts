export interface EntityOption {
  id: number;
  name: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface Author extends EntityOption {}
export interface Category extends EntityOption {}

export type BookAvailability = 'AVAILABLE' | 'BORROWED' | 'UNAVAILABLE';

export interface BookImage {
  id: number;
  url: string;
  originalFileName?: string;
  contentType?: string;
  fileSize?: number;
  displayOrder: number;
}

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
  borrowed: boolean;
  images: BookImage[];
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

export interface BorrowerDetails {
  id: number;
  username: string;
  email?: string;
  role?: string;
  fullName?: string;
  phone?: string;
  address?: string;
}

export interface Borrowing {
  id: number;
  bookId: number;
  bookTitle: string;
  isbn?: string;
  categoryId?: number;
  categoryName?: string;
  borrowerId: number;
  borrower?: BorrowerDetails;
  borrowedOn: string;
  returnedOn: string | null;
  status: 'BORROWED' | 'RETURNED' | string;
}
