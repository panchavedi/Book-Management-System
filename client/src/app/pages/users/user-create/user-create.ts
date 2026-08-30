import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ToastService } from '../../../services/toast.service';
import { UserService } from '../../../services/user.service';
import { ManagedUserRole } from '../../../models/auth.model';

@Component({
  selector: 'app-user-create',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './user-create.html',
  styleUrl: './user-create.scss'
})
export class UserCreate {
  private readonly fb = inject(FormBuilder);
  private readonly users = inject(UserService);
  private readonly toast = inject(ToastService);

  readonly saving = signal(false);
  readonly roles: ManagedUserRole[] = ['USER', 'LIBRARIAN', 'AUTHOR', 'ADMIN'];
  readonly form = this.fb.nonNullable.group({
    fullName: ['', [Validators.required, Validators.maxLength(150)]],
    username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
    phone: ['', [Validators.required, Validators.pattern(/^[0-9+()\- .]{7,30}$/)]],
    address: ['', [Validators.required, Validators.maxLength(500)]],
    password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(100)]],
    role: ['USER' as ManagedUserRole, [Validators.required]]
  });

  save(): void {
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.users.createManagedUser(this.form.getRawValue()).subscribe({
      next: (response) => {
        this.toast.show(`${response.user.fullName || response.user.username} created as ${response.user.role}.`, 'success');
        this.form.reset({ fullName: '', username: '', email: '', phone: '', address: '', password: '', role: 'USER' });
        this.saving.set(false);
      },
      error: (err) => {
        this.toast.show(err?.error?.message || 'Unable to create the account.', 'error');
        this.saving.set(false);
      }
    });
  }
}
