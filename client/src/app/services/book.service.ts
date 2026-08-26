import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Book, BookRequest, BookSearchParams, Borrowing, PagedResponse } from '../models/book.model';

@Injectable({ providedIn: 'root' })
export class BookService {
  private readonly http = inject(HttpClient);
  private readonly api = 'http://localhost:8085/books';

  list(params: BookSearchParams = {}): Observable<Book[]> {
    return this.http.get<Book[] | PagedResponse<Book>>(this.api, { params: this.params(params) }).pipe(
      map((response) => this.unwrap(response))
    );
  }

  get(id: number): Observable<Book> {
    return this.http.get<Book>(`${this.api}/${id}`).pipe(map((book) => this.normalize(book)));
  }

  create(req: BookRequest): Observable<Book> {
    return this.http.post<Book>(this.api, this.cleanRequest(req)).pipe(map((book) => this.normalize(book)));
  }

  update(id: number, req: BookRequest): Observable<Book> {
    return this.http.put<Book>(`${this.api}/${id}`, this.cleanRequest(req)).pipe(map((book) => this.normalize(book)));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/${id}`);
  }

  search(params: BookSearchParams): Observable<Book[]> {
    return this.http.get<Book[] | PagedResponse<Book>>(`${this.api}/search`, { params: this.params(params) }).pipe(
      map((response) => this.unwrap(response))
    );
  }

  byAuthor(authorId: number): Observable<Book[]> {
    return this.http.get<Book[]>(`${this.api}/author/${authorId}`).pipe(
      map((books) => books.map((book) => this.normalize(book)))
    );
  }

  byCategory(categoryId: number): Observable<Book[]> {
    return this.http.get<Book[]>(`${this.api}/category/${categoryId}`).pipe(
      map((books) => books.map((book) => this.normalize(book)))
    );
  }

  borrow(id: number): Observable<Borrowing> {
    return this.http.post<Borrowing>(`${this.api}/${id}/borrow`, null);
  }

  returnBook(id: number): Observable<Borrowing> {
    return this.http.post<Borrowing>(`${this.api}/${id}/return`, null);
  }

  private params(params: BookSearchParams): HttpParams {
    let httpParams = new HttpParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        httpParams = httpParams.set(key, String(value));
      }
    });
    return httpParams;
  }

  private unwrap(response: Book[] | PagedResponse<Book>): Book[] {
    const books = Array.isArray(response) ? response : response.content;
    return books.map((book) => this.normalize(book));
  }

  private normalize(book: Book): Book {
    const borrowedCopies = book.borrowedCopies ?? Math.max(book.totalCopies - book.availableCopies, 0);
    return {
      ...book,
      publisher: book.publisher ?? null,
      printedOn: book.printedOn ?? null,
      about: book.about ?? null,
      borrowed: !!book.borrowed,
      borrowedCopies,
      status: book.availableCopies > 0 ? 'AVAILABLE' : borrowedCopies > 0 ? 'BORROWED' : 'UNAVAILABLE'
    };
  }

  private cleanRequest(req: BookRequest): BookRequest {
    return {
      title: req.title.trim(),
      isbn: req.isbn.trim(),
      authorId: req.authorId,
      categoryId: req.categoryId,
      publisher: req.publisher?.trim() || undefined,
      printedOn: req.printedOn || undefined,
      totalCopies: req.totalCopies,
      about: req.about?.trim() || undefined
    };
  }
}
