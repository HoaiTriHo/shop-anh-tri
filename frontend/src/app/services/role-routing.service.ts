import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class RoleRoutingService {

  constructor(
    private authService: AuthService,
    private router: Router
  ) { }

  /**
   * Handle routing based on user role
   * Admin users are redirected to /admin
   * Regular users are redirected to /home
   */
  handleRoleBasedRouting(): void {
    if (!this.authService.isLoggedIn()) {
      // User not logged in, stay on current page or go to login
      return;
    }

    const currentPath = window.location.pathname;
    
    // If user is admin
    if (this.authService.isAdmin()) {
      // If admin is on root path or home, redirect to admin dashboard
      if (currentPath === '/' || currentPath === '/home') {
        this.router.navigate(['/admin']);
        return;
      }
      
      // If admin is on non-admin routes, redirect to admin dashboard
      if (!currentPath.startsWith('/admin')) {
        this.router.navigate(['/admin']);
        return;
      }
    } else {
      // If regular user tries to access admin routes, redirect to home
      if (currentPath.startsWith('/admin')) {
        this.router.navigate(['/home']);
        return;
      }
    }
  }

  /**
   * Get default route for current user
   */
  getDefaultRoute(): string {
    if (!this.authService.isLoggedIn()) {
      return '/home';
    }
    
    return this.authService.isAdmin() ? '/admin' : '/home';
  }

  /**
   * Navigate to appropriate page after login
   */
  navigateAfterLogin(): void {
    if (this.authService.isAdmin()) {
      this.router.navigate(['/admin']);
    } else {
      this.router.navigate(['/home']);
    }
  }
} 