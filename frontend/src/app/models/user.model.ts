export interface User {
  id?: number;
  username: string;
  email: string;
  role: 'USER' | 'ADMIN';
  createdAt?: Date;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  userId: number;
  username: string;
  role: string;
  message: string;
  success: boolean;
} 