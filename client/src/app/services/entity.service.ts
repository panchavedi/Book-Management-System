import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Author, Category, EntityOption } from '../models/book.model';

type EntityKind = 'authors' | 'categories';

@Injectable({ providedIn: 'root' })
export class EntityService {
  private readonly http = inject(HttpClient);
  private readonly api = 'http://localhost:8082';

  listAuthors(): Observable<Author[]> {
    return this.http.get<Author[]>(`${this.api}/authors`);
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

  listCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.api}/categories`);
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

  private search<T extends EntityOption>(kind: EntityKind, query: string): Observable<T[]> {
    const keyword = query.trim();
    const params = new HttpParams().set('keyword', keyword);
    return this.http.get<T[]>(`${this.api}/${kind}/search`, { params });
  }

  private create<T extends EntityOption>(kind: EntityKind, name: string): Observable<T> {
    return this.http.post<T>(`${this.api}/${kind}`, { name: name.trim() });
  }
}
