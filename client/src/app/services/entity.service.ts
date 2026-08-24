import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Author, Category, EntityOption, PagedResponse } from '../models/book.model';

type EntityKind = 'authors' | 'categories';

@Injectable({ providedIn: 'root' })
export class EntityService {
  private readonly http = inject(HttpClient);
  private readonly api = 'http://localhost:8082';

  // --- Author Operations ---
  listAuthors(): Observable<Author[]> {
    return this.list<Author>('authors');
  }

  getAuthor(id: number): Observable<Author> {
    return this.http.get<Author>(`${this.api}/authors/${id}`);
  }

  searchAuthors(query: string): Observable<Author[]> {
    return this.search<Author>('authors', query);
  }

  createAuthor(name: string): Observable<Author> {
    return this.create<Author>('authors', name);
  }

  updateAuthor(id: number, name: string): Observable<Author> {
    return this.http.put<Author>(`${this.api}/authors/${id}`, { name: name.trim() });
  }

  deleteAuthor(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/authors/${id}`);
  }

  // --- Category Operations ---
  listCategories(): Observable<Category[]> {
    return this.list<Category>('categories');
  }

  getCategory(id: number): Observable<Category> {
    return this.http.get<Category>(`${this.api}/categories/${id}`);
  }

  searchCategories(query: string): Observable<Category[]> {
    return this.search<Category>('categories', query);
  }

  createCategory(name: string): Observable<Category> {
    return this.create<Category>('categories', name);
  }

  updateCategory(id: number, name: string): Observable<Category> {
    return this.http.put<Category>(`${this.api}/categories/${id}`, { name: name.trim() });
  }

  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/categories/${id}`);
  }

  // --- Generic Helpers ---
  private list<T extends EntityOption>(kind: EntityKind): Observable<T[]> {
    return this.http.get<T[] | PagedResponse<T>>(`${this.api}/${kind}`).pipe(
      map((response) => (Array.isArray(response) ? response : response.content))
    );
  }

  private search<T extends EntityOption>(kind: EntityKind, query: string): Observable<T[]> {
    return this.list<T>(kind).pipe(
      map((items) => {
        const q = query.trim().toLowerCase();
        return q ? items.filter((item) => item.name.toLowerCase().includes(q)) : items;
      })
    );
  }

  private create<T extends EntityOption>(kind: EntityKind, name: string): Observable<T> {
    return this.http.post<T>(`${this.api}/${kind}`, { name: name.trim() });
  }
}

