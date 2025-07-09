import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class UserOnlyGuard implements CanActivate {
  constructor(private auth: AuthService, private router: Router) {}

  canActivate(): boolean {
    // First check if user is logged in
    if (!this.auth.isLoggedIn()) {
      console.log('User not logged in, redirecting to login page');
      // Store current URL for redirect after login
      localStorage.setItem('redirectAfterLogin', window.location.pathname);
      this.router.navigate(['/auth/login']);
      return false;
    }
    
    // Then check if user is admin (redirect to admin panel)
    if (this.auth.isAdmin()) {
      this.router.navigate(['/admin']);
      return false;
    }
    
    // User is logged in and is not admin - allow access
    return true;
  }
} 