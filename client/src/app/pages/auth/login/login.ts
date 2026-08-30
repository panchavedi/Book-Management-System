import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

type AuthMode = 'login' | 'register';

@Component({ selector: 'app-login', imports: [ReactiveFormsModule], templateUrl: './login.html', styleUrl: './login.scss' })
export class Login {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly mode = signal<AuthMode>('login');
  readonly loading = signal(false);
  readonly message = signal('');
  readonly error = signal('');

  readonly loginForm = this.fb.nonNullable.group({ username: ['', [Validators.required]], password: ['', [Validators.required]] });
  readonly registerForm = this.fb.nonNullable.group({
    fullName: ['', [Validators.required, Validators.maxLength(150)]],
    username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
    phone: ['', [Validators.required, Validators.pattern(/^[0-9+()\- .]{7,30}$/)]],
    address: ['', [Validators.required, Validators.maxLength(500)]],
    password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(100)]]
  });

  setMode(mode: AuthMode): void { this.mode.set(mode); this.error.set(''); this.message.set(''); }

  submit(): void { this.mode() === 'login' ? this.login() : this.register(); }

  private login(): void {
    if (this.loginForm.invalid || this.loading()) { this.loginForm.markAllAsTouched(); return; }
    this.loading.set(true); this.error.set('');
    this.auth.login(this.loginForm.getRawValue()).subscribe({
      next: () => void this.router.navigateByUrl('/dashboard'),
      error: (err) => { this.error.set(err?.error?.message || 'Username or password is incorrect.'); this.loading.set(false); }
    });
  }

  private register(): void {
    if (this.registerForm.invalid || this.loading()) { this.registerForm.markAllAsTouched(); return; }
    this.loading.set(true); this.error.set('');
    this.auth.register(this.registerForm.getRawValue()).subscribe({
      next: () => {
        this.message.set('Account created. You can sign in now.');
        this.loading.set(false);
        const username = this.registerForm.controls.username.value;
        this.setMode('login');
        this.loginForm.patchValue({ username, password: '' });
        this.registerForm.reset({ fullName:'', username:'', email:'', phone:'', address:'', password:'' });
      },
      error: (err) => { this.error.set(err?.error?.message || 'Unable to create the account.'); this.loading.set(false); }
    });
  }
}
