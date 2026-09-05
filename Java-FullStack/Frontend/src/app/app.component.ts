import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { filter, map } from 'rxjs';
import { NavbarComponent } from './shared/components/navbar/navbar.component';
import { FooterComponent } from './shared/components/footer/footer.component';
import { ToastComponent } from './shared/components/toast/toast.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, NavbarComponent, FooterComponent, ToastComponent],
  template: `
    <app-navbar [activeSlug]="activeSlug()"></app-navbar>
    <main>
      <router-outlet></router-outlet>
    </main>
    <app-footer></app-footer>
    <app-toast></app-toast>
  `,
})
export class AppComponent {
  private router = inject(Router);

  // The customer dashboard shows its own profile/logout controls in its topbar, so the
  // shared nav's right side is hidden there — matches app.js#mountNav(activeSlug) exactly.
  activeSlug = toSignal(
    this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd),
      map((e) => (e.urlAfterRedirects.startsWith('/dashboard') ? 'dashboard' : undefined))
    ),
    { initialValue: this.router.url.startsWith('/dashboard') ? 'dashboard' : undefined }
  );
}