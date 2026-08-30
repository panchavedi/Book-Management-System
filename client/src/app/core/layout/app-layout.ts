import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ToastComponent } from '../../components/toast/toast';

type NavItem = { label: string; route: string; icon: string; roles?: string[] };

@Component({
  selector: 'app-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, ToastComponent],
  templateUrl: './app-layout.html',
  styleUrl: './app-layout.scss'
})
export class AppLayout {
  readonly auth = inject(AuthService);
  readonly sidebarOpen = signal(true);
  readonly menuOpen = signal(false);
  readonly navItems: NavItem[] = [
    { label: 'Discover', route: '/dashboard', icon: 'home' },
    { label: 'Library', route: '/books', icon: 'book' },
    { label: 'My Reading', route: '/borrowed', icon: 'heart' },
    { label: 'Borrowings', route: '/borrowings', icon: 'chart', roles: ['ADMIN'] },
    { label: 'Add Book', route: '/books/new', icon: 'plus', roles: ['ADMIN'] },
    { label: 'People', route: '/users/new', icon: 'users', roles: ['ADMIN'] }
  ];
  readonly visibleNav = computed(() => {
    const role = this.auth.currentUser()?.role?.toUpperCase();
    return this.navItems.filter((item) => !item.roles || !!role && item.roles.includes(role));
  });

  async logout(): Promise<void> {
    this.menuOpen.set(false);
    this.auth.logout().subscribe();
  }
}
