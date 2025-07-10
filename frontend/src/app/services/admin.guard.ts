import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class AdminGuard implements CanActivate {
  constructor(private auth: AuthService, private router: Router) {}

  canActivate(): boolean {
    // Check if user is logged in
    if (!this.auth.isLoggedIn()) {
      console.log('User not logged in, redirecting to login page');
      // Store current URL for redirect after login
      localStorage.setItem('redirectAfterLogin', window.location.pathname);
      this.router.navigate(['/auth/login']);
      return false;
    }
    
    // Check if user is admin
    if (!this.auth.isAdmin()) {
      console.log('User is not admin, redirecting to home page');
      this.router.navigate(['/home']);
      return false;
    }
    
    // User is logged in and is admin - allow access
    return true;
  }
} 