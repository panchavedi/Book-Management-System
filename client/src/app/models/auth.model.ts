export type UserRole = 'USER' | 'ADMIN' | 'AUTHOR' | 'LIBRARIAN' | string;

export interface User {
  id: number;
  username: string;
  email: string;
  role: UserRole;
  fullName: string;
  phone: string;
  address: string;
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
  fullName: string;
  phone: string;
  address: string;
}

export type ManagedUserRole = 'USER' | 'ADMIN' | 'AUTHOR' | 'LIBRARIAN';

export interface AdminCreateUserRequest {
  username: string;
  email: string;
  password: string;
  fullName: string;
  phone: string;
  address: string;
  role: ManagedUserRole;
}

export interface UserUpdateRequest {
  email: string;
  fullName: string;
  phone: string;
  address: string;
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

export interface LogoutResponse {
  message: string;
}

export interface TokenValidationResponse {
  valid: boolean;
  username: string | null;
  userId: number | null;
  role: UserRole | null;
  message: string;
}
