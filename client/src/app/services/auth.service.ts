import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, catchError, of, switchMap, tap } from 'rxjs';
import {
  LoginRequest,
  LoginResponse,
  LogoutResponse,
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
  private readonly api = 'http://localhost:8081/auth';

  readonly currentUser = signal<User | null>(this.readStoredUser());
  private readonly authenticated = signal(this.hasUsableToken());
  readonly isAuthenticated = computed(() => this.authenticated());
  readonly isAdmin = computed(() => this.currentUser()?.role?.toUpperCase() === 'ADMIN');
  readonly isStaff = computed(() => {
    const role = this.currentUser()?.role?.toUpperCase();
    return role === 'ADMIN' || role === 'LIBRARIAN';
  });

  login(req: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.api}/login`, req).pipe(
      tap((response) => this.storeSession(response.accessToken, response.user))
    );
  }

  register(req: RegisterRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${this.api}/register`, req);
  }

  validate(token = this.token()): Observable<TokenValidationResponse> {
    const params = new HttpParams().set('token', token ?? '');
    return this.http.get<TokenValidationResponse>(`${this.api}/validate`, { params });
  }

  logout(): Observable<LogoutResponse | null> {
    const token = this.token();
    const request$ = token
      ? this.http.post<LogoutResponse>(`${this.api}/logout`, {}).pipe(catchError(() => of(null)))
      : of(null);

    return request$.pipe(
      tap(() => {
        this.clearSession();
        void this.router.navigateByUrl('/login');
      })
    );
  }

  updateCurrentUser(user: User): void {
    localStorage.setItem(this.userKey, JSON.stringify(user));
    this.currentUser.set(user);
    this.authenticated.set(true);
  }

  token(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  initializeSession(): Observable<boolean> {
    const token = this.token();
    if (!token || !this.hasUsableToken(token)) {
      this.clearSession();
      return of(false);
    }

    return this.validate(token).pipe(
      switchMap((result) => {
        if (!result.valid) {
          this.clearSession();
          return of(false);
        }
        this.authenticated.set(true);
        return of(true);
      }),
      catchError(() => of(true))
    );
  }

  private storeSession(accessToken: string, user: User): void {
    localStorage.setItem(this.tokenKey, accessToken);
    localStorage.setItem(this.userKey, JSON.stringify(user));
    this.currentUser.set(user);
    this.authenticated.set(true);
  }

  private clearSession(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
    Object.keys(localStorage)
      .filter((key) => key.startsWith('bms_active_borrowings_') || key === 'bms_active_borrowing')
      .forEach((key) => localStorage.removeItem(key));
    this.currentUser.set(null);
    this.authenticated.set(false);
  }

  private hasUsableToken(token = this.token()): boolean {
    if (!token) return false;
    try {
      const payload = JSON.parse(atob(token.split('.')[1] ?? ''));
      return !payload.exp || Number(payload.exp) * 1000 > Date.now();
    } catch {
      return false;
    }
  }

  private readStoredUser(): User | null {
    const raw = localStorage.getItem(this.userKey);
    if (!raw) return this.userFromToken();

    try {
      return JSON.parse(raw) as User;
    } catch {
      return this.userFromToken();
    }
  }

  private userFromToken(token = this.token()): User | null {
    if (!token) return null;
    try {
      const payload = JSON.parse(atob(token.split('.')[1] ?? ''));
      const role = payload.role ?? payload.authority ?? payload.roles?.[0] ?? payload.authorities?.[0] ?? 'USER';
      return {
        id: Number(payload.userId ?? payload.id ?? payload.sub ?? 0),
        username: payload.username ?? payload.sub ?? 'reader',
        email: payload.email ?? '',
        fullName: payload.fullName ?? '',
        phone: payload.phone ?? '',
        address: payload.address ?? '',
        role: String(role).replace('ROLE_', '').toUpperCase(),
        enabled: payload.enabled ?? true
      };
    } catch {
      return null;
    }
  }
}
