import { Component, OnInit } from '@angular/core';
import { CartService } from '../../services/cart.service';
import { OrderService } from '../../services/order.service';
import { CartItem } from '../../models/cart.model';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-checkout',
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.css']
})
export class CheckoutComponent implements OnInit {
  cartItems: CartItem[] = [];
  cartTotal: number = 0;
  checkoutForm: FormGroup;
  orderSuccess: boolean = false;
  isLoading: boolean = false;
  errorMessage: string = '';

  // Payment methods
  paymentMethods = [
    { value: 'COD', label: 'Thanh toán khi nhận hàng (COD)' },
    { value: 'BANK_TRANSFER', label: 'Chuyển khoản ngân hàng' },
    { value: 'CREDIT_CARD', label: 'Thẻ tín dụng/ghi nợ' },
    { value: 'E_WALLET', label: 'Ví điện tử' }
  ];

  constructor(
    private cartService: CartService,
    private orderService: OrderService,
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService
  ) {
    // Khởi tạo form với các trường cần thiết
    this.checkoutForm = this.fb.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      address: ['', Validators.required],
      phone: ['', [Validators.required, Validators.pattern(/^[0-9]{10,}$/)]],
      paymentMethod: ['COD', Validators.required],
    });
  }

  ngOnInit(): void {
    // Lấy cart items và tổng tiền
    this.cartService.cart$.subscribe(items => {
      this.cartItems = items;
    });
    this.cartService.cartTotal$.subscribe(total => {
      this.cartTotal = total;
    });

    // Tự động lấy profile user và patch vào form nếu đã đăng nhập
    if (this.authService.isLoggedIn()) {
      this.authService.getProfile().subscribe({
        next: (profile) => {
          // Ghép họ tên
          const fullName = ((profile.firstName || '') + ' ' + (profile.lastName || '')).trim();
          this.checkoutForm.patchValue({
            name: fullName,
            email: profile.email || '',
            address: profile.address || '',
            phone: profile.phoneNumber || ''
          });
        },
        error: (err) => {
          // Không làm gì nếu lỗi
        }
      });
    }
  }

  onSubmit(): void {
    if (this.checkoutForm.invalid) {
      this.checkoutForm.markAllAsTouched();
      return;
    }

    if (this.cartItems.length === 0) {
      this.errorMessage = 'Giỏ hàng của bạn đang trống.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    // Lấy thông tin từ form
    const formValue = this.checkoutForm.value;
    
    // Tạo checkout request
    const checkoutRequest = this.orderService.convertCartToCheckoutRequest(
      this.cartItems,
      formValue.name,
      formValue.email,
      formValue.address,
      formValue.phone,
      formValue.paymentMethod
    );

    // Gọi API checkout
    this.orderService.checkout(checkoutRequest).subscribe({
      next: (order) => {
        this.isLoading = false;
        this.orderSuccess = true;
        // Xóa giỏ hàng sau khi đặt hàng thành công
        this.cartService.clearCart().subscribe({
          next: (success) => {
            if (!success) {
              console.error('Failed to clear cart after checkout');
            }
          },
          error: (error) => {
            console.error('Error clearing cart after checkout:', error);
          }
        });
        // Không tự động chuyển trang nữa
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = 'Có lỗi xảy ra khi đặt hàng. Vui lòng thử lại.';
        console.error('Checkout error:', error);
      }
    });
  }

  // Thêm hàm này để chuyển về trang sản phẩm khi nhấn nút
  onContinueShopping(): void {
    this.router.navigate(['/products']);
  }

  getPaymentMethodLabel(value: string): string {
    const method = this.paymentMethods.find(m => m.value === value);
    return method ? method.label : value;
  }
}
