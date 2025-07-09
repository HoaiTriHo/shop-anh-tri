import { Component, OnInit, Inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { CartService } from '../../services/cart.service';
import { AuthService } from '../../services/auth.service';
import { Product } from '../../models/product.model';
import { DOCUMENT } from '@angular/common';

@Component({
  selector: 'app-product-detail',
  templateUrl: './product-detail.component.html',
  styleUrls: ['./product-detail.component.css']
})
export class ProductDetailComponent implements OnInit {
  product: Product | null = null;
  loading = false;
  error = '';
  quantity = 1;
  addingToCart = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private cartService: CartService,
    private authService: AuthService,
    @Inject(DOCUMENT) private document: Document
  ) {}

  ngOnInit(): void {
    this.loadProduct();
  }

  /**
   * Load product details by ID
   */
  loadProduct(): void {
    const productId = this.route.snapshot.paramMap.get('id');
    if (!productId) {
      this.error = 'Product ID not found';
      return;
    }

    this.loading = true;
    this.productService.getProductById(+productId).subscribe({
      next: (product) => {
        this.product = product;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading product:', error);
        this.error = 'Failed to load product details';
        this.loading = false;
      }
    });
  }

  /**
   * Add product to cart
   */
  addToCart(): void {
    if (!this.product) return;

    // Check if user is logged in first
    if (!this.authService.isLoggedIn()) {
      console.log('User not logged in, redirecting to login page');
      // Store the current page info for better UX after login
      localStorage.setItem('redirectAfterLogin', `/products/${this.product.id}`);
      this.router.navigate(['/auth/login']);
      return;
    }

    this.addingToCart = true;
    this.cartService.addToCart(this.product.id!, this.quantity).subscribe({
      next: (success) => {
        this.addingToCart = false;
        if (success) {
          // Show toast notification
          const appRoot = this.document.querySelector('app-root') as any;
          if (appRoot && appRoot.showToast) {
            appRoot.showToast('Đã thêm vào giỏ hàng');
          }
        } else {
          console.error('Failed to add product to cart');
          // You can show an error message to user here
        }
      },
      error: (error) => {
        this.addingToCart = false;
        console.error('Error adding to cart:', error);
        // Handle specific error cases
        if (error.status === 401) {
          // Token expired or invalid, redirect to login
          this.authService.logout();
          this.router.navigate(['/auth/login']);
        }
      }
    });
  }

  /**
   * Buy now (add to cart and go to checkout)
   */
  buyNow(): void {
    if (!this.product) return;

    // Check if user is logged in first
    if (!this.authService.isLoggedIn()) {
      console.log('User not logged in, redirecting to login page');
      // Store the current page info for better UX after login
      localStorage.setItem('redirectAfterLogin', `/products/${this.product.id}`);
      this.router.navigate(['/auth/login']);
      return;
    }

    this.addingToCart = true;
    this.cartService.addToCart(this.product.id!, this.quantity).subscribe({
      next: (success) => {
        this.addingToCart = false;
        if (success) {
          this.router.navigate(['/cart/checkout']);
        } else {
          console.error('Failed to add product to cart');
          // You can show an error message to user here
        }
      },
      error: (error) => {
        this.addingToCart = false;
        console.error('Error adding to cart:', error);
        // Handle specific error cases
        if (error.status === 401) {
          // Token expired or invalid, redirect to login
          this.authService.logout();
          this.router.navigate(['/auth/login']);
        }
      }
    });
  }

  /**
   * Increase quantity
   */
  increaseQuantity(): void {
    if (this.product && this.quantity < this.product.stockQuantity!) {
      this.quantity++;
    }
  }

  /**
   * Decrease quantity
   */
  decreaseQuantity(): void {
    if (this.quantity > 1) {
      this.quantity--;
    }
  }

  /**
   * Check if product is in cart
   */
  isInCart(): boolean {
    if (!this.product) return false;
    return this.cartService.isInCart(this.product.id!);
  }

  /**
   * Get quantity of product in cart
   */
  getCartQuantity(): number {
    if (!this.product) return 0;
    return this.cartService.getItemQuantity(this.product.id!);
  }

  /**
   * Handle image loading errors
   */
  onImageError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.src = 'https://images.unsplash.com/photo-1560472354-b33ff0c44a43?w=400&h=400&fit=crop&crop=center';
    img.alt = 'Product image not available';
  }
}
