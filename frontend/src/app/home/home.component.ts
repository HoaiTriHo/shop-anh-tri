import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ProductService } from '../services/product.service';
import { CartService } from '../services/cart.service';
import { AuthService } from '../services/auth.service';
import { Product } from '../models/product.model';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  featuredProducts: Product[] = [];
  loading = false;
  addingToCart: { [key: number]: boolean } = {};
  
  categories = [
    { name: 'Electronics', icon: 'bi-laptop', count: 25 },
    { name: 'Clothing', icon: 'bi-tshirt', count: 18 },
    { name: 'Books', icon: 'bi-book', count: 32 }
  ];

  constructor(
    private productService: ProductService,
    private cartService: CartService,
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadFeaturedProducts();
  }

  /**
   * Load featured products (first 4 products - 1 row)
   */
  loadFeaturedProducts(): void {
    this.loading = true;
    this.productService.getProducts({ size: 4 }).subscribe({
      next: (response) => {
        // Get products from response
        this.featuredProducts = response.products;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading featured products:', error);
        this.loading = false;
      }
    });
  }

  /**
   * Add product to cart
   */
  addToCart(product: Product): void {
    // Check if user is logged in first
    if (!this.authService.isLoggedIn()) {
      console.log('User not logged in, redirecting to login page');
      // Store the current page info for better UX after login
      localStorage.setItem('redirectAfterLogin', '/');
      this.router.navigate(['/login']);
      return;
    }

    this.addingToCart[product.id!] = true;
    this.cartService.addToCart(product.id!, 1).subscribe({
      next: (success) => {
        this.addingToCart[product.id!] = false;
        if (success) {
          console.log(`Added ${product.name} to cart successfully`);
          // You can show a success message here
        } else {
          console.error('Failed to add product to cart');
          // You can show an error message to user here
        }
      },
      error: (error) => {
        this.addingToCart[product.id!] = false;
        console.error('Error adding to cart:', error);
        // Handle specific error cases
        if (error.status === 401) {
          // Token expired or invalid, redirect to login
          this.authService.logout();
          this.router.navigate(['/login']);
        }
      }
    });
  }

  /**
   * Navigate to product detail
   */
  viewProduct(product: Product): void {
    this.router.navigate(['/products', product.id]);
  }

  /**
   * Navigate to category
   */
  navigateToCategory(category: any): void {
    this.router.navigate(['/products'], { queryParams: { category: category.name.toLowerCase() } });
  }

  /**
   * Navigate to all products
   */
  viewAllProducts(): void {
    this.router.navigate(['/products']);
  }

  /**
   * Check if product is in cart
   */
  isInCart(productId: number): boolean {
    return this.cartService.isInCart(productId);
  }

  /**
   * Get quantity of product in cart
   */
  getCartQuantity(productId: number): number {
    return this.cartService.getItemQuantity(productId);
  }

  /**
   * Check if adding to cart is in progress for this product
   */
  isAddingToCart(productId: number): boolean {
    return this.addingToCart[productId] || false;
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