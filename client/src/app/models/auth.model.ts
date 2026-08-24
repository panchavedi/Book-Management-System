export type UserRole = 'USER' | 'ADMIN' | 'AUTHOR' | 'LIBRARIAN' | string;

export interface User {
  id: number;
  username: string;
  email?: string;
  role: UserRole;
  enabled?: boolean;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  role?: UserRole;
}

export interface RegisterResponse {
  message: string;
  user: User;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: 'Bearer' | string;
  expiresIn: number;
  user: User;
}

export interface TokenValidationResponse {
  valid: boolean;
  username: string | null;
  userId: number | null;
  role: UserRole | null;
  message: string;
}
