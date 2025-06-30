import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ProductService } from '../services/product.service';
import { OrderService } from '../services/order.service';
import { AuthService } from '../services/auth.service';
import { Product } from '../models/product.model';
import { Order } from '../models/order.model';

// Extended Order interface for admin view
interface AdminOrder extends Order {
  customerName?: string;
  customerEmail?: string;
  customerPhone?: string;
}

@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.css']
})
export class AdminComponent implements OnInit {
  // Tab management
  activeTab: string = 'dashboard';
  
  // Loading states
  isLoading: boolean = false;
  isLoadingProducts: boolean = false;
  isLoadingOrders: boolean = false;
  
  // Data arrays
  products: Product[] = [];
  orders: AdminOrder[] = [];
  filteredProducts: Product[] = [];
  filteredOrders: AdminOrder[] = [];
  
  // Search terms
  productSearchTerm: string = '';
  orderSearchTerm: string = '';
  
  // Statistics
  stats = {
    totalProducts: 0,
    totalOrders: 0,
    totalRevenue: 0,
    pendingOrders: 0
  };

  constructor(
    private productService: ProductService,
    private orderService: OrderService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.checkAdminRole();
    this.loadDashboardData();
  }

  /**
   * Check if current user has admin role
   * Redirect to login if not admin
   */
  checkAdminRole(): void {
    const user = this.authService.getCurrentUser();
    if (!user || user.role !== 'ADMIN') {
      this.router.navigate(['/login']);
    }
  }

  /**
   * Load initial dashboard data
   */
  loadDashboardData(): void {
    this.isLoading = true;
    this.loadProducts();
    this.loadOrders();
    this.calculateStats();
  }

  /**
   * Load all products from backend
   */
  loadProducts(): void {
    this.isLoadingProducts = true;
    this.productService.getAllProducts().subscribe({
      next: (products) => {
        this.products = products;
        this.filteredProducts = products;
        this.isLoadingProducts = false;
        this.calculateStats();
      },
      error: (error) => {
        console.error('Error loading products:', error);
        this.isLoadingProducts = false;
      }
    });
  }

  /**
   * Load all orders from backend
   */
  loadOrders(): void {
    this.isLoadingOrders = true;
    this.orderService.getAllOrders().subscribe({
      next: (orders) => {
        this.orders = orders as AdminOrder[];
        this.filteredOrders = orders as AdminOrder[];
        this.isLoadingOrders = false;
        this.calculateStats();
      },
      error: (error) => {
        console.error('Error loading orders:', error);
        this.isLoadingOrders = false;
      }
    });
  }

  /**
   * Calculate dashboard statistics
   */
  calculateStats(): void {
    this.stats.totalProducts = this.products.length;
    this.stats.totalOrders = this.orders.length;
    this.stats.totalRevenue = this.orders.reduce((sum, order) => sum + order.totalAmount, 0);
    this.stats.pendingOrders = this.orders.filter(order => order.status === 'PENDING').length;
  }

  /**
   * Handle product search
   */
  onProductSearch(): void {
    if (!this.productSearchTerm.trim()) {
      this.filteredProducts = this.products;
      return;
    }
    
    const searchTerm = this.productSearchTerm.toLowerCase();
    this.filteredProducts = this.products.filter(product =>
      product.name.toLowerCase().includes(searchTerm) ||
      product.description.toLowerCase().includes(searchTerm) ||
      product.category.toLowerCase().includes(searchTerm)
    );
  }

  /**
   * Handle order search
   */
  onOrderSearch(): void {
    if (!this.orderSearchTerm.trim()) {
      this.filteredOrders = this.orders;
      return;
    }
    
    const searchTerm = this.orderSearchTerm.toLowerCase();
    this.filteredOrders = this.orders.filter(order =>
      order.id?.toString().includes(searchTerm) ||
      order.customerName?.toLowerCase().includes(searchTerm) ||
      order.customerEmail?.toLowerCase().includes(searchTerm)
    );
  }

  /**
   * Toggle product active status
   */
  toggleProductStatus(product: Product): void {
    if (!product.id) return;
    
    const updateRequest = {
      id: product.id,
      name: product.name,
      description: product.description,
      price: product.price,
      category: product.category,
      brand: product.brand,
      stockQuantity: product.stockQuantity,
      active: !product.active
    };
    
    this.productService.updateProduct(product.id, updateRequest).subscribe({
      next: () => {
        // Update local data
        const index = this.products.findIndex(p => p.id === product.id);
        if (index !== -1) {
          this.products[index] = { ...product, active: !product.active };
          this.onProductSearch(); // Refresh filtered list
        }
      },
      error: (error) => {
        console.error('Error updating product status:', error);
        // Revert the change in UI
        product.active = !product.active;
      }
    });
  }

  /**
   * Handle order status change from select element
   */
  onOrderStatusChange(order: AdminOrder, event: Event): void {
    const selectElement = event.target as HTMLSelectElement;
    const newStatus = selectElement.value as 'PENDING' | 'CONFIRMED' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';
    this.updateOrderStatus(order, newStatus);
  }

  /**
   * Update order status
   */
  updateOrderStatus(order: AdminOrder, newStatus: 'PENDING' | 'CONFIRMED' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED'): void {
    if (!order.id) return;
    
    this.orderService.updateOrderStatus(order.id, newStatus).subscribe({
      next: (updatedOrder) => {
        // Update local data
        const index = this.orders.findIndex(o => o.id === order.id);
        if (index !== -1) {
          this.orders[index] = { ...this.orders[index], status: newStatus };
          this.onOrderSearch(); // Refresh filtered list
          this.calculateStats();
        }
      },
      error: (error: any) => {
        console.error('Error updating order status:', error);
      }
    });
  }

  /**
   * Delete product
   */
  deleteProduct(product: Product): void {
    if (!product.id) return;
    
    if (confirm(`Are you sure you want to delete "${product.name}"?`)) {
      this.productService.deleteProduct(product.id).subscribe({
        next: () => {
          // Remove from local arrays
          this.products = this.products.filter(p => p.id !== product.id);
          this.onProductSearch(); // Refresh filtered list
          this.calculateStats();
        },
        error: (error) => {
          console.error('Error deleting product:', error);
        }
      });
    }
  }

  /**
   * Navigate to product management
   */
  goToProductManagement(): void {
    this.activeTab = 'products';
  }

  /**
   * Navigate to order management
   */
  goToOrderManagement(): void {
    this.activeTab = 'orders';
  }

  /**
   * Export products data
   */
  exportProducts(): void {
    // Simple CSV export
    const csvContent = this.convertToCSV(this.filteredProducts);
    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'products.csv';
    a.click();
    window.URL.revokeObjectURL(url);
  }

  /**
   * Convert products to CSV format
   */
  private convertToCSV(products: Product[]): string {
    const headers = ['ID', 'Name', 'Description', 'Price', 'Category', 'Stock', 'Active'];
    const rows = products.map(p => [
      p.id,
      p.name,
      p.description,
      p.price,
      p.category,
      p.stockQuantity,
      p.active ? 'Yes' : 'No'
    ]);
    
    return [headers, ...rows].map(row => row.join(',')).join('\n');
  }

  /**
   * Logout admin
   */
  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
} 