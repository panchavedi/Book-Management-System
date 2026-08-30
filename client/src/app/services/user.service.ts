import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AdminCreateUserRequest, RegisterRequest, RegisterResponse, User, UserUpdateRequest } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly api = 'http://localhost:8081/user';
  private readonly authApi = 'http://localhost:8081/auth';

  createUser(request: RegisterRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${this.authApi}/register`, request);
  }

  createManagedUser(request: AdminCreateUserRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(this.api, request);
  }

  getCurrentUser(): Observable<User> {
    return this.http.get<User>(`${this.api}/me`);
  }

  getById(id: number): Observable<User> {
    return this.http.get<User>(`${this.api}/${id}`);
  }

  getAll(): Observable<User[]> {
    return this.http.get<User[]>(this.api);
  }

  update(id: number, request: UserUpdateRequest): Observable<User> {
    return this.http.put<User>(`${this.api}/${id}`, request);
  }
}
