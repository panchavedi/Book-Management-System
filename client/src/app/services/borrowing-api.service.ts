import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError, map } from 'rxjs';
import { Borrowing } from '../models/book.model';

@Injectable({ providedIn: 'root' })
export class BorrowingApiService {
  private readonly http=inject(HttpClient);private readonly api='http://localhost:8085/borrow';
  myHistory():Observable<Borrowing[]>{return this.http.get<Borrowing[]>(`${this.api}/me`)}
  myActive():Observable<Borrowing[]>{return this.http.get<Borrowing[]>(`${this.api}/me/books`).pipe(catchError((e)=>e?.status===404?this.myHistory().pipe(map(items=>items.filter(x=>x.status==='BORROWED'))):(()=>{throw e})()))}
  adminActive(sort:'asc'|'desc'='desc'):Observable<Borrowing[]>{return this.http.get<Borrowing[]>(`${this.api}/active`,{params:new HttpParams().set('sort',sort)})}
  activeByBook(bookId:number):Observable<Borrowing[]>{return this.http.get<Borrowing[]>(`${this.api}/books/${bookId}`)}
  historyByBook(bookId:number):Observable<Borrowing[]>{return this.http.get<Borrowing[]>(`${this.api}/books/${bookId}/history`)}
  adminHistory():Observable<Borrowing[]>{return this.http.get<Borrowing[]>(`${this.api}/history`)}
}
