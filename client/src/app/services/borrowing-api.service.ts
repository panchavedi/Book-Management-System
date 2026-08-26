import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError, map } from 'rxjs';
import { Borrowing } from '../models/book.model';

@Injectable({ providedIn: 'root' })
export class BorrowingApiService {
  private readonly http = inject(HttpClient);
  private readonly api = 'http://localhost:8085/borrow';

  myHistory(): Observable<Borrowing[]> {
    return this.http.get<Borrowing[]>(`${this.api}/me`);
  }

  myActive(): Observable<Borrowing[]> {
    return this.http.get<Borrowing[]>(`${this.api}/me/books`).pipe(
      // Backward-compatible fallback for a running library-service instance that
      // still exposes only /borrow/me. The source-of-truth endpoint remains /borrow/me/books.
      catchError((error) => {
        if (error?.status !== 404) {
          throw error;
        }

        return this.myHistory().pipe(
          map((items) => items.filter((item) => item.status === 'BORROWED'))
        );
      })
    );
  }

  adminActive(sort: 'asc' | 'desc' = 'desc'): Observable<Borrowing[]> {
    const params = new HttpParams().set('sort', sort);
    return this.http.get<Borrowing[]>(`${this.api}/active`, { params });
  }

  activeByBook(bookId: number): Observable<Borrowing[]> {
    return this.http.get<Borrowing[]>(`${this.api}/books/${bookId}`);
  }
}
