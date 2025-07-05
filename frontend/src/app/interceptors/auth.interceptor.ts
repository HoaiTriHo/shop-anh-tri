import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { Router } from '@angular/router';
import { jwtDecode } from 'jwt-decode';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private router: Router) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    // Get token from localStorage
    const token = localStorage.getItem('jwt_token');
    const isAuthApi = request.url.includes('/api/auth/login') || request.url.includes('/api/auth/register');
    
    if (!isAuthApi && token) {
      // Decode token để lấy exp
      try {
        const decoded: any = jwtDecode(token);
        const now = Math.floor(Date.now() / 1000);
        if (decoded.exp && decoded.exp < now) {
          // Token đã hết hạn, tự động logout và chuyển hướng về login
          localStorage.removeItem('jwt_token');
          localStorage.removeItem('user');
          this.router.navigate(['/login']);
          return new Observable<HttpEvent<unknown>>(); // Không gửi request nữa
        }
      } catch (e) {
        // Nếu decode lỗi, cũng logout
        localStorage.removeItem('jwt_token');
        localStorage.removeItem('user');
        this.router.navigate(['/login']);
        return new Observable<HttpEvent<unknown>>();
      }
      // Nếu token còn hạn, thêm vào header
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }
    return next.handle(request);
  }
} 