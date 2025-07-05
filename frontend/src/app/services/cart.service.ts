import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { map, catchError, tap } from 'rxjs/operators';
import { CartItem, Cart, AddToCartRequest, UpdateCartItemRequest } from '../models/cart.model';
import { Product } from '../models/product.model';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CartService {
  // Observable để theo dõi trạng thái giỏ hàng
  private cartSubject = new BehaviorSubject<CartItem[]>([]);
  public cart$ = this.cartSubject.asObservable();

  // Observable để theo dõi tổng số lượng sản phẩm
  private cartItemCountSubject = new BehaviorSubject<number>(0);
  public cartItemCount$ = this.cartItemCountSubject.asObservable();

  // Observable để theo dõi tổng giá trị giỏ hàng
  private cartTotalSubject = new BehaviorSubject<number>(0);
  public cartTotal$ = this.cartTotalSubject.asObservable();

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {
    // Theo dõi trạng thái đăng nhập để load giỏ hàng
    this.authService.currentUser$.subscribe(user => {
      if (user) {
        this.loadCart();
      } else {
        this.clearCart();
      }
    });
  }

  /**
   * Lấy giỏ hàng từ backend
   * Chỉ gọi khi user đã đăng nhập
   */
  private loadCart(): void {
    if (!this.authService.isLoggedIn()) {
      return;
    }

    this.http.get<any>(`${environment.apiUrl}/api/cart`).pipe(
      map(response => {
        // Chuyển đổi response từ backend thành CartItem[]
        return response.items.map((item: any) => ({
          id: item.id,
          product: {
            id: item.product.id,
            name: item.product.name,
            description: item.product.description,
            price: item.product.price,
            imageUrl: item.product.imageUrl,
            category: item.product.category,
            brand: item.product.brand,
            stockQuantity: item.product.stockQuantity,
            active: item.product.active
          },
          quantity: item.quantity,
          price: item.price
        }));
      }),
      catchError(error => {
        console.error('Error loading cart:', error);
        return of([]);
      })
    ).subscribe(items => {
      this.cartSubject.next(items);
      this.updateCartMetrics();
    });
  }

  /**
   * Thêm sản phẩm vào giỏ hàng
   * Chỉ hoạt động khi user đã đăng nhập
   */
  addToCart(productId: number, quantity: number = 1): Observable<boolean> {
    // Check authentication first
    if (!this.authService.isLoggedIn()) {
      console.log('User not authenticated, cannot add to cart');
      return of(false);
    }

    const request = {
      productId: productId,
      quantity: quantity
    };

    return this.http.post<any>(`${environment.apiUrl}/api/cart/add`, request).pipe(
      map(response => {
        // Cập nhật giỏ hàng từ response
        const items = response.items.map((item: any) => ({
          id: item.id,
          product: {
            id: item.product.id,
            name: item.product.name,
            description: item.product.description,
            price: item.product.price,
            imageUrl: item.product.imageUrl,
            category: item.product.category,
            brand: item.product.brand,
            stockQuantity: item.product.stockQuantity,
            active: item.product.active
          },
          quantity: item.quantity,
          price: item.price
        }));
        this.cartSubject.next(items);
        this.updateCartMetrics();
        console.log('Successfully added product to cart');
        return true;
      }),
      catchError(error => {
        console.error('Error adding to cart:', error);
        
        // Handle specific error cases
        if (error.status === 401) {
          console.log('Authentication error - user needs to login');
          // The component will handle the redirect
        } else if (error.status === 400) {
          console.log('Bad request - invalid product or quantity');
        } else if (error.status === 404) {
          console.log('Product not found');
        } else if (error.status === 500) {
          console.log('Server error');
        }
        
        return of(false);
      })
    );
  }

  /**
   * Cập nhật số lượng sản phẩm trong giỏ hàng
   */
  updateCartItemQuantity(itemId: number, quantity: number): Observable<boolean> {
    if (!this.authService.isLoggedIn()) {
      return of(false);
    }

    return this.http.put<any>(`${environment.apiUrl}/api/cart/item/${itemId}?quantity=${quantity}`, {}).pipe(
      map(response => {
        // Cập nhật giỏ hàng từ response
        const items = response.items.map((item: any) => ({
          id: item.id,
          product: {
            id: item.product.id,
            name: item.product.name,
            description: item.product.description,
            price: item.product.price,
            imageUrl: item.product.imageUrl,
            category: item.product.category,
            brand: item.product.brand,
            stockQuantity: item.product.stockQuantity,
            active: item.product.active
          },
          quantity: item.quantity,
          price: item.price
        }));
        this.cartSubject.next(items);
        this.updateCartMetrics();
        return true;
      }),
      catchError(error => {
        console.error('Error updating cart item:', error);
        return of(false);
      })
    );
  }

  /**
   * Xóa sản phẩm khỏi giỏ hàng
   */
  removeFromCart(itemId: number): Observable<boolean> {
    if (!this.authService.isLoggedIn()) {
      return of(false);
    }

    return this.http.delete<any>(`${environment.apiUrl}/api/cart/item/${itemId}`).pipe(
      map(response => {
        // Cập nhật giỏ hàng từ response
        const items = response.items.map((item: any) => ({
          id: item.id,
          product: {
            id: item.product.id,
            name: item.product.name,
            description: item.product.description,
            price: item.product.price,
            imageUrl: item.product.imageUrl,
            category: item.product.category,
            brand: item.product.brand,
            stockQuantity: item.product.stockQuantity,
            active: item.product.active
          },
          quantity: item.quantity,
          price: item.price
        }));
        this.cartSubject.next(items);
        this.updateCartMetrics();
        return true;
      }),
      catchError(error => {
        console.error('Error removing from cart:', error);
        return of(false);
      })
    );
  }

  /**
   * Xóa toàn bộ giỏ hàng
   */
  clearCart(): Observable<boolean> {
    if (!this.authService.isLoggedIn()) {
      return of(false);
    }

    return this.http.delete<any>(`${environment.apiUrl}/api/cart`).pipe(
      map(response => {
        // Cập nhật giỏ hàng từ response
        const items = response.items.map((item: any) => ({
          id: item.id,
          product: {
            id: item.product.id,
            name: item.product.name,
            description: item.product.description,
            price: item.product.price,
            imageUrl: item.product.imageUrl,
            category: item.product.category,
            brand: item.product.brand,
            stockQuantity: item.product.stockQuantity,
            active: item.product.active
          },
          quantity: item.quantity,
          price: item.price
        }));
        this.cartSubject.next(items);
        this.updateCartMetrics();
        return true;
      }),
      catchError(error => {
        console.error('Error clearing cart:', error);
        return of(false);
      })
    );
  }

  /**
   * Lấy danh sách sản phẩm trong giỏ hàng
   */
  getCartItems(): CartItem[] {
    return this.cartSubject.value;
  }

  /**
   * Lấy số lượng sản phẩm trong giỏ hàng
   */
  getCartItemCount(): number {
    return this.cartItemCountSubject.value;
  }

  /**
   * Lấy tổng giá trị giỏ hàng
   */
  getCartTotal(): number {
    return this.cartTotalSubject.value;
  }

  /**
   * Kiểm tra xem sản phẩm có trong giỏ hàng không
   */
  isInCart(productId: number): boolean {
    return this.cartSubject.value.some(item => item.product.id === productId);
  }

  /**
   * Lấy số lượng của một sản phẩm trong giỏ hàng
   */
  getItemQuantity(productId: number): number {
    const item = this.cartSubject.value.find(item => item.product.id === productId);
    return item ? item.quantity : 0;
  }

  /**
   * Cập nhật các metrics của giỏ hàng (số lượng, tổng tiền)
   */
  private updateCartMetrics(): void {
    const items = this.cartSubject.value;
    
    // Tính tổng số lượng
    const totalItems = items.reduce((sum, item) => sum + item.quantity, 0);
    this.cartItemCountSubject.next(totalItems);
    
    // Tính tổng tiền
    const totalPrice = items.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    this.cartTotalSubject.next(totalPrice);
  }
}
