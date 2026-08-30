import { Component, OnInit, inject, signal } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { RouterLink } from '@angular/router';

import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-profile',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './profile.html',
  styleUrl: './profile.scss'
})
export class Profile implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly users = inject(UserService);
  private readonly toast = inject(ToastService);

  readonly auth = inject(AuthService);
  readonly loading = signal(true);
  readonly saving = signal(false);

  readonly form = this.fb.nonNullable.group({
    fullName: [
      '',
      [Validators.required, Validators.maxLength(150)]
    ],
    email: [
      '',
      [
        Validators.required,
        Validators.email,
        Validators.maxLength(150)
      ]
    ],
    phone: [
      '',
      [
        Validators.required,
        Validators.pattern(/^[0-9+()\- .]{7,30}$/)
      ]
    ],
    address: [
      '',
      [Validators.required, Validators.maxLength(500)]
    ]
  });

  ngOnInit(): void {
    this.loadProfile();
  }

  save(): void {
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }

    const userId = this.auth.currentUser()?.id;

    if (!userId) {
      this.toast.show('Your session is no longer available.', 'error');
      return;
    }

    this.saving.set(true);

    this.users.update(userId, this.form.getRawValue()).subscribe({
      next: (user) => {
        this.auth.updateCurrentUser(user);
        this.saving.set(false);
        this.toast.show(
          'Profile updated successfully.',
          'success'
        );
      },
      error: (error) => {
        this.saving.set(false);
        this.toast.show(
          error?.error?.message || 'Could not update your profile.',
          'error'
        );
      }
    });
  }

  logout(): void {
    this.auth.logout().subscribe();
  }

  private loadProfile(): void {
    this.users.getCurrentUser().subscribe({
      next: (user) => {
        this.auth.updateCurrentUser(user);
        this.form.patchValue({
          fullName: user.fullName,
          email: user.email,
          phone: user.phone,
          address: user.address
        });
        this.loading.set(false);
      },
      error: (error) => {
        this.loading.set(false);
        this.toast.show(
          error?.error?.message || 'Could not load your profile.',
          'error'
        );
      }
    });
  }
}
