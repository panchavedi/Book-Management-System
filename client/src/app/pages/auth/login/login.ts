import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

type AuthMode = 'login' | 'register';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly mode = signal<AuthMode>('login');
  readonly loading = signal(false);
  readonly message = signal('');
  readonly error = signal('');

  readonly loginForm = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required]
  });

  readonly registerForm = this.fb.nonNullable.group({
    username: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]]
  });

  setMode(mode: AuthMode): void {
    this.mode.set(mode);
    this.error.set('');
    this.message.set('');
  }

  submit(): void {
    this.mode() === 'login' ? this.login() : this.register();
  }

  private login(): void {
    if (this.loginForm.invalid || this.loading()) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.error.set('');

    this.auth.login(this.loginForm.getRawValue()).subscribe({
      next: () => this.router.navigateByUrl('/dashboard'),
      error: () => {
        this.error.set('Unable to sign in.');
        this.loading.set(false);
      }
    });
  }

  private register(): void {
    if (this.registerForm.invalid || this.loading()) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.error.set('');

    this.auth.register(this.registerForm.getRawValue()).subscribe({
      next: () => {
        this.message.set('Account created.');
        this.loading.set(false);
        this.setMode('login');
        this.loginForm.patchValue({
          username: this.registerForm.controls.username.value,
          password: ''
        });
      },
      error: () => {
        this.error.set('Unable to register.');
        this.loading.set(false);
      }
    });
  }
}
