import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { UserRole } from '../../../models/auth.model';
import { ToastService } from '../../../services/toast.service';
import { UserService } from '../../../services/user.service';

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
  readonly roles: UserRole[] = ['USER', 'ADMIN', 'AUTHOR', 'LIBRARIAN'];

  readonly form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    role: ['USER' as UserRole, Validators.required]
  });

  save(): void {
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.users.createUser(this.form.getRawValue()).subscribe({
      next: (response) => {
        this.toast.show(`${response.user.username} created.`, 'success');
        this.form.reset({ username: '', email: '', password: '', role: 'USER' });
        this.saving.set(false);
      },
      error: () => {
        this.toast.show('Unable to create user.', 'error');
        this.saving.set(false);
      }
    });
  }
}
