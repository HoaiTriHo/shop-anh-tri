import { Component, OnInit } from '@angular/core';
import { CartService } from '../../services/cart.service';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { CartItem } from '../../models/cart.model';

@Component({
  selector: 'app-cart-view',
  templateUrl: './cart-view.component.html',
  styleUrls: ['./cart-view.component.css']
})
export class CartViewComponent implements OnInit {
  cartItems: CartItem[] = [];
  cartTotal = 0;
  cartItemCount = 0;
  loading = false;
  updatingItems: { [key: number]: boolean } = {};
  removingItems: { [key: number]: boolean } = {};
  promoCode = '';
  promoApplied = false;

  constructor(
    private cartService: CartService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Kiểm tra đăng nhập
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }

    // Subscribe to cart changes
    this.cartService.cart$.subscribe(items => {
      this.cartItems = items;
    });

    this.cartService.cartTotal$.subscribe(total => {
      this.cartTotal = total;
    });

    this.cartService.cartItemCount$.subscribe(count => {
      this.cartItemCount = count;
    });
  }

  /**
   * Cập nhật số lượng sản phẩm
   */
  updateQuantity(item: CartItem, newQuantity: number): void {
    if (newQuantity < 1) {
      return;
    }

    this.updatingItems[item.id!] = true;

    this.cartService.updateCartItemQuantity(item.id!, newQuantity).subscribe({
      next: (success) => {
        this.updatingItems[item.id!] = false;
        if (!success) {
          console.error('Failed to update quantity');
        }
      },
      error: (error) => {
        this.updatingItems[item.id!] = false;
        console.error('Error updating quantity:', error);
      }
    });
  }

  /**
   * Xóa sản phẩm khỏi giỏ hàng
   */
  removeItem(item: CartItem): void {
    this.removingItems[item.id!] = true;

    this.cartService.removeFromCart(item.id!).subscribe({
      next: (success) => {
        this.removingItems[item.id!] = false;
        if (!success) {
          console.error('Failed to remove item');
        }
      },
      error: (error) => {
        this.removingItems[item.id!] = false;
        console.error('Error removing item:', error);
      }
    });
  }

  /**
   * Xóa toàn bộ giỏ hàng
   */
  clearCart(): void {
    this.cartService.clearCart();
  }

  /**
   * Chuyển đến trang checkout
   */
  proceedToCheckout(): void {
    if (this.cartItems.length > 0) {
      this.router.navigate(['/cart/checkout']);
    }
  }

  /**
   * Tiếp tục mua sắm
   */
  continueShopping(): void {
    this.router.navigate(['/products']);
  }

  /**
   * Kiểm tra xem item có đang được cập nhật không
   */
  isUpdating(itemId: number): boolean {
    return this.updatingItems[itemId] || false;
  }

  /**
   * Kiểm tra xem item có đang được xóa không
   */
  isRemoving(itemId: number): boolean {
    return this.removingItems[itemId] || false;
  }

  /**
   * Kiểm tra xem giỏ hàng có trống không
   */
  isCartEmpty(): boolean {
    return this.cartItems.length === 0;
  }

  /**
   * Handle image loading errors
   */
  onImageError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.src = 'https://images.unsplash.com/photo-1560472354-b33ff0c44a43?w=400&h=400&fit=crop&crop=center';
    img.alt = 'Product image not available';
  }

  /**
   * Apply promo code
   */
  applyPromoCode(): void {
    if (this.promoCode.trim()) {
      // This is a placeholder - you would typically call an API
      if (this.promoCode.toLowerCase() === 'discount10') {
        this.promoApplied = true;
        console.log('Promo code applied: 10% discount');
      } else {
        this.promoApplied = false;
        console.log('Invalid promo code');
      }
    }
  }

  /**
   * Get cart total
   */
  getSubtotal(): number {
    return this.cartService.getCartTotal();
  }

  /**
   * Get shipping cost
   */
  getShippingCost(): number {
    return this.getSubtotal() > 50 ? 0 : 5.99;
  }

  /**
   * Get tax amount
   */
  getTax(): number {
    return this.getSubtotal() * 0.08; // 8% tax
  }

  /**
   * Get total amount
   */
  getTotal(): number {
    let total = this.getSubtotal() + this.getShippingCost() + this.getTax();
    
    // Apply promo discount if applicable
    if (this.promoApplied) {
      total = total * 0.9; // 10% discount
    }
    
    return total;
  }
}
