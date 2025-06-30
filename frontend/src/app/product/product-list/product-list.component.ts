import { Component, OnInit } from '@angular/core';
import { ProductService } from '../../services/product.service';
import { CartService } from '../../services/cart.service';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { Product } from '../../models/product.model';

@Component({
  selector: 'app-product-list',
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.css']
})
export class ProductListComponent implements OnInit {
  products: Product[] = [];
  filteredProducts: Product[] = [];
  currentPage = 1;
  itemsPerPage = 12;
  totalItems = 0;
  totalPages = 0;
  
  // Search and filter properties
  searchQuery = '';
  selectedCategory = '';
  selectedPrice = '';
  selectedSort = 'name';
  showFilters = false;
  
  // Loading states
  loading = false;
  addingToCart: { [key: number]: boolean } = {};

  // Math object for template
  Math = Math;

  constructor(
    private productService: ProductService,
    private cartService: CartService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  /**
   * Load products from backend with pagination, search, and filters
   */
  loadProducts(): void {
    this.loading = true;
    
    // Calculate page for backend (0-based)
    const page = this.currentPage - 1;
    
    this.productService.getProducts({
      page: page,
      size: this.itemsPerPage,
      query: this.searchQuery,
      category: this.selectedCategory,
      price: this.selectedPrice,
      sort: this.selectedSort
    }).subscribe({
      next: (response: any) => {
        this.products = response.products;
        this.filteredProducts = response.products;
        this.totalItems = response.total;
        this.totalPages = Math.ceil(this.totalItems / this.itemsPerPage);
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading products:', error);
        this.loading = false;
      }
    });
  }

  /**
   * Search products
   */
  onSearch(): void {
    this.currentPage = 1;
    this.loadProducts();
  }

  /**
   * Filter by category
   */
  onCategoryChange(): void {
    this.currentPage = 1;
    this.loadProducts();
  }

  /**
   * Filter by price range
   */
  onPriceChange(): void {
    this.currentPage = 1;
    this.loadProducts();
  }

  /**
   * Sort products
   */
  onSortChange(): void {
    this.currentPage = 1;
    this.loadProducts();
  }

  /**
   * Toggle filters visibility
   */
  toggleFilters(): void {
    this.showFilters = !this.showFilters;
  }

  /**
   * Clear all filters
   */
  clearFilters(): void {
    this.searchQuery = '';
    this.selectedCategory = '';
    this.selectedPrice = '';
    this.selectedSort = 'name';
    this.currentPage = 1;
    this.loadProducts();
  }

  /**
   * Navigate to specific page
   */
  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.loadProducts();
      // Scroll to top of product list
      this.scrollToTop();
    }
  }

  /**
   * Scroll to top of product list
   */
  private scrollToTop(): void {
    const productListElement = document.querySelector('.product-grid');
    if (productListElement) {
      productListElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }

  /**
   * Add product to cart
   * Redirects to login if user is not authenticated
   */
  addToCart(product: Product): void {
    // Check if user is logged in first
    if (!this.authService.isLoggedIn()) {
      console.log('User not logged in, redirecting to login page');
      // Store the current page info for better UX after login
      localStorage.setItem('redirectAfterLogin', '/products');
      // Redirect to login page
      this.router.navigate(['/login']);
      return;
    }

    // Set loading state for this product
    this.addingToCart[product.id!] = true;

    this.cartService.addToCart(product.id!, 1).subscribe({
      next: (success: boolean) => {
        this.addingToCart[product.id!] = false;
        if (success) {
          // Show success message (you can implement a toast service)
          console.log(`Added ${product.name} to cart successfully`);
          // You can add a visual feedback here like a toast notification
        } else {
          console.error('Failed to add product to cart');
          // You can show an error message to user here
        }
      },
      error: (error: any) => {
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
   * Navigate to product detail page
   */
  viewProduct(product: Product): void {
    this.router.navigate(['/products', product.id]);
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
   * Get page numbers for pagination
   */
  getPageNumbers(): number[] {
    const pages: number[] = [];
    const maxVisiblePages = 5;
    let startPage = Math.max(1, this.currentPage - Math.floor(maxVisiblePages / 2));
    let endPage = Math.min(this.totalPages, startPage + maxVisiblePages - 1);
    
    if (endPage - startPage + 1 < maxVisiblePages) {
      startPage = Math.max(1, endPage - maxVisiblePages + 1);
    }
    
    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }
    
    return pages;
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
