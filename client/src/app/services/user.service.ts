import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { RegisterRequest, RegisterResponse } from '../models/auth.model';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly auth = inject(AuthService);

  createUser(request: RegisterRequest): Observable<RegisterResponse> {
    return this.auth.register(request);
  }
}
