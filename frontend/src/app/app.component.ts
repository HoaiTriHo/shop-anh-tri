import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './services/auth.service';
import { Subscription } from 'rxjs';
import { CartService } from './services/cart.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'shop-frontend';
  
  // Authentication state
  isLoggedIn = false;
  isAdmin = false;
  username = '';
  cartItemCount = 0;
  
  // UI state
  isLoading = false;
  alertMessage = '';
  alertType = 'info';
  alertIcon = 'info-circle';

  // Subscription for auth changes
  private authSubscription?: Subscription;

  constructor(
    private router: Router,
    private authService: AuthService,
    private cartService: CartService
  ) {}

  ngOnInit(): void {
    // Subscribe to authentication changes
    this.authSubscription = this.authService.currentUser$.subscribe(user => {
      this.updateAuthState(user);
    });
    
    // Initialize authentication state
    this.checkAuthState();

    // Subscribe to cart item count changes
    this.cartService.cartItemCount$.subscribe(count => {
      this.cartItemCount = count;
    });
  }

  ngOnDestroy(): void {
    // Clean up subscription
    if (this.authSubscription) {
      this.authSubscription.unsubscribe();
    }
  }

  /**
   * Update authentication state based on user data
   */
  private updateAuthState(user: any): void {
    if (user) {
      this.isLoggedIn = true;
      this.username = user.username || '';
      this.isAdmin = user.role === 'ADMIN';
    } else {
      this.isLoggedIn = false;
      this.username = '';
      this.isAdmin = false;
    }
  }

  /**
   * Check authentication state from localStorage
   */
  checkAuthState(): void {
    const token = localStorage.getItem('jwt_token');
    const user = localStorage.getItem('user');
    
    if (token && user) {
      try {
        const userData = JSON.parse(user);
        this.updateAuthState(userData);
      } catch (error) {
        this.clearAuthData();
      }
    } else {
      this.updateAuthState(null);
    }
  }

  /**
   * Logout user and redirect to home page
   */
  logout(): void {
    this.authService.logout();
    this.showAlert('Successfully logged out', 'success', 'check-circle');
    
    // Navigate to home page after logout
    this.router.navigate(['/']);
  }

  /**
   * Clear authentication data
   */
  private clearAuthData(): void {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('user');
    this.updateAuthState(null);
    this.cartItemCount = 0;
  }

  /**
   * Show alert message
   */
  showAlert(message: string, type: string = 'info', icon: string = 'info-circle'): void {
    this.alertMessage = message;
    this.alertType = type;
    this.alertIcon = icon;
    
    // Auto-hide after 5 seconds
    setTimeout(() => {
      this.clearAlert();
    }, 5000);
  }

  /**
   * Clear alert message
   */
  clearAlert(): void {
    this.alertMessage = '';
  }

  /**
   * Set loading state
   */
  setLoading(loading: boolean): void {
    this.isLoading = loading;
  }

  /**
   * Update cart item count
   */
  updateCartCount(count: number): void {
    this.cartItemCount = count;
  }

  isAdminRoute(): boolean {
    return this.router.url.startsWith('/admin');
  }
}
