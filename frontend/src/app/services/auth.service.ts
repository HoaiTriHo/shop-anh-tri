import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { User, LoginRequest, RegisterRequest, AuthResponse } from '../models/user.model';
import { jwtDecode } from 'jwt-decode';
import { environment } from '../../environments/environment';

interface JwtPayload {
  sub: string;
  role: string;
  exp?: number;
  [key: string]: any;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = `${environment.apiUrl}/api/auth`;
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  private userRole: string | null = null;

  constructor(private http: HttpClient) {
    // Check if user is already logged in
    this.loadStoredUser();
  }

  /**
   * Load user from localStorage on app startup
   */
  private loadStoredUser(): void {
    const token = localStorage.getItem('jwt_token');
    const userStr = localStorage.getItem('user');
    
    if (token && userStr) {
      try {
        const user = JSON.parse(userStr);
        this.currentUserSubject.next(user);
        this.decodeAndStoreRole(token);
      } catch (error) {
        this.clearAuthData();
      }
    }
  }

  /**
   * Login user with username and password
   */
  login(loginRequest: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/login`, loginRequest)
      .pipe(
        tap(response => {
          // Create user object from response
          const user: User = {
            id: response.userId,
            username: response.username,
            email: '', // Backend doesn't return email in login response
            role: response.role as 'USER' | 'ADMIN'
          };
          
          this.storeAuthData(response.token, user);
          this.currentUserSubject.next(user);
          this.decodeAndStoreRole(response.token);
        })
      );
  }

  /**
   * Register new user
   */
  register(registerRequest: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/register`, registerRequest)
      .pipe(
        tap(response => {
          // Create user object from response
          const user: User = {
            id: response.userId,
            username: response.username,
            email: registerRequest.email, // Use email from request
            role: response.role as 'USER' | 'ADMIN'
          };
          
          this.storeAuthData(response.token, user);
          this.currentUserSubject.next(user);
          this.decodeAndStoreRole(response.token);
        })
      );
  }

  /**
   * Logout current user
   */
  logout(): void {
    this.clearAuthData();
    this.currentUserSubject.next(null);
    this.userRole = null;
  }

  /**
   * Get current user
   */
  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  /**
   * Check if user is logged in
   */
  isLoggedIn(): boolean {
    return this.currentUserSubject.value !== null;
  }

  /**
   * Check if user is admin
   */
  isAdmin(): boolean {
    return this.getRole() === 'ADMIN';
  }

  /**
   * Check if user is user
   */
  isUser(): boolean {
    return this.getRole() === 'USER';
  }

  /**
   * Get user role
   */
  getRole(): string | null {
    if (this.userRole) return this.userRole;
    const token = this.getToken();
    if (token) {
      this.decodeAndStoreRole(token);
      return this.userRole;
    }
    return null;
  }

  /**
   * Get JWT token
   */
  getToken(): string | null {
    return localStorage.getItem('jwt_token');
  }

  /**
   * Store authentication data in localStorage
   */
  private storeAuthData(token: string, user: User): void {
    localStorage.setItem('jwt_token', token);
    localStorage.setItem('user', JSON.stringify(user));
  }

  /**
   * Clear authentication data from localStorage
   */
  private clearAuthData(): void {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('user');
  }

  /**
   * Decode JWT and store user role
   */
  private decodeAndStoreRole(token: string): void {
    try {
      const decoded = jwtDecode<JwtPayload>(token);
      this.userRole = decoded.role || null;
    } catch (e) {
      this.userRole = null;
    }
  }

  /**
   * Lấy thông tin profile user hiện tại
   */
  getProfile() {
    return this.http.get<any>(`${environment.apiUrl}/api/users/profile`);
  }

  /**
   * Cập nhật thông tin profile user hiện tại
   */
  updateProfile(profile: any) {
    return this.http.put<any>(`${environment.apiUrl}/api/users/profile`, profile);
  }
}
