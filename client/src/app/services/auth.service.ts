import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
  TokenValidationResponse,
  User
} from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly tokenKey = 'bms_access_token';
  private readonly userKey = 'bms_user';
  private readonly api = 'http://localhost:8081/api/auth';

  readonly currentUser = signal<User | null>(this.readStoredUser());
  readonly isAuthenticated = computed(() => !!this.token());
  readonly isAdmin = computed(() => this.currentUser()?.role?.toUpperCase() === 'ADMIN');

  login(req: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.api}/login`, req).pipe(
      tap((response) => {
        localStorage.setItem(this.tokenKey, response.accessToken);
        localStorage.setItem(this.userKey, JSON.stringify(response.user));
        this.currentUser.set(response.user);
      })
    );
  }

  register(req: RegisterRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${this.api}/register`, req);
  }

  validate(token = this.token()): Observable<TokenValidationResponse> {
    const params = new HttpParams().set('token', token ?? '');
    return this.http.get<TokenValidationResponse>(`${this.api}/validate`, { params });
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
    Object.keys(localStorage)
      .filter((key) => key.startsWith('bms_active_borrowings_') || key === 'bms_active_borrowing')
      .forEach((key) => localStorage.removeItem(key));
    this.currentUser.set(null);
    this.router.navigateByUrl('/login');
  }

  token(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  private readStoredUser(): User | null {
    const raw = localStorage.getItem(this.userKey);
    if (!raw) {
      return this.userFromToken();
    }

    try {
      return JSON.parse(raw) as User;
    } catch {
      return this.userFromToken();
    }
  }

  private userFromToken(token = this.token()): User | null {
    if (!token) {
      return null;
    }

    try {
      const payload = JSON.parse(atob(token.split('.')[1] ?? ''));
      const role = payload.role ?? payload.authority ?? payload.roles?.[0] ?? payload.authorities?.[0] ?? 'USER';

      return {
        id: Number(payload.id ?? payload.userId ?? payload.sub ?? 0),
        username: payload.username ?? payload.sub ?? 'reader',
        email: payload.email,
        role: String(role).replace('ROLE_', '').toUpperCase(),
        enabled: payload.enabled ?? true
      };
    } catch {
      return null;
    }
  }
}
