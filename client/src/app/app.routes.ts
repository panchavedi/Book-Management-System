import { Routes } from '@angular/router';
import { AppLayout } from './core/layout/app-layout';
import { adminGuard, authGuard } from './guards/auth-guard';
import { Login } from './pages/auth/login/login';
import { Borrowed } from './pages/borrowed/borrowed';
import { Dashboard } from './pages/dashboard/dashboard';
import { BookDetail } from './pages/books/book-detail/book-detail';
import { BookForm } from './pages/books/book-form/book-form';
import { BookList } from './pages/books/book-list/book-list';
import { UserCreate } from './pages/users/user-create/user-create';
import { Borrowings } from './pages/borrowings/borrowings';
import { Profile } from './pages/profile/profile';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: '', component: AppLayout, canActivate: [authGuard], children: [
    { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
    { path: 'dashboard', component: Dashboard },
    { path: 'books', component: BookList },
    { path: 'books/new', component: BookForm, canActivate: [adminGuard] },
    { path: 'books/:id', component: BookDetail },
    { path: 'books/:id/edit', component: BookForm, canActivate: [adminGuard] },
    { path: 'borrowed', component: Borrowed },
    { path: 'borrowings', component: Borrowings, canActivate: [adminGuard] },
    { path: 'users/new', component: UserCreate, canActivate: [adminGuard] },
    { path: 'profile', component: Profile }
  ]},
  { path: '**', redirectTo: 'dashboard' }
];
