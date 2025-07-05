# Shop Anh Trí - E-commerce Application

A full-stack e-commerce application built with Angular 16+ frontend and Spring Boot backend.

## 🚀 Features

- **User Authentication**: JWT-based authentication with role-based access control
- **Product Management**: Browse, search, and filter products
- **Shopping Cart**: Add/remove items and manage quantities
- **Order Management**: Place orders and track order history
- **Admin Panel**: Product and order management for administrators
- **Responsive Design**: Mobile-friendly interface

## 🛠️ Tech Stack

### Frontend
- **Angular 16+**: Modern frontend framework
- **Bootstrap 5**: Responsive UI components
- **Font Awesome**: Icons
- **RxJS**: Reactive programming

### Backend
- **Spring Boot 3+**: Java-based backend framework
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Database operations
- **MySQL**: Database
- **JWT**: Token-based authentication

### DevOps
- **Docker**: Containerization
- **Docker Compose**: Multi-container orchestration

## 📦 Project Structure

```
shop-app/
├── frontend/          # Angular application
├── backend/           # Spring Boot application
├── mysql/            # Database initialization
├── docker-compose.yml # Container orchestration
└── README.md         # This file
```

## 🚀 Quick Start

### Prerequisites
- Docker and Docker Compose
- Node.js 18+ (for local development)
- Java 17+ (for local development)

### Using Docker (Recommended)

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd shop-app
   ```

2. **Start the application**
   ```bash
   docker-compose up -d
   ```

3. **Access the application**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080
   - Database: localhost:3306

### Local Development

1. **Backend Setup**
   ```bash
   cd backend
   ./gradlew bootRun
   ```

2. **Frontend Setup**
   ```bash
   cd frontend
   npm install
   npm start
   ```

## 👥 Đăng nhập tài khoản

### Người dùng thông thường
- Vui lòng tự tạo 1 tài khoản mới để trải nghiệm mua sắm (qua chức năng Đăng ký trên website).

### Tài khoản Admin
- **Username**: `global`
- **Password**: `123456`
- **Role**: `ADMIN`

## 🔧 Configuration

### Environment Variables

The application uses the following environment variables:

- `DB_HOST`: MySQL host (default: localhost)
- `DB_PORT`: MySQL port (default: 3306)
- `DB_NAME`: Database name (default: shop_db)
- `DB_USER`: Database user (default: shop_user)
- `DB_PASSWORD`: Database password (default: shop_password)

### API Endpoints

- **Authentication**: `/api/auth/*`
- **Products**: `/api/products/*`
- **Orders**: `/api/orders/*`
- **Health Check**: `/api/health`

## 📱 Features Overview

### For Customers
- Browse products by category
- Search products by name
- Add items to shopping cart
- Place orders
- View order history
- User profile management

### For Administrators
- Product management (CRUD operations)
- Order management
- User management
- Sales analytics

## 🛡️ Security

- JWT-based authentication
- Role-based access control
- Password encryption
- CORS configuration
- Input validation

## 📊 Database Schema

The application includes the following main entities:
- **Users**: User accounts and authentication
- **Products**: Product catalog
- **Orders**: Customer orders
- **Order Items**: Individual items in orders

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## 📞 Support

For support and questions:
- Email: info@shopanhtri.com
- Phone: 0123 456 789

---

**Shop Anh Trí** - Your trusted online shopping destination! 🛒✨ 

## Thông tin môi trường FE (Angular)

- Khi bạn build frontend (FE) bằng Docker Compose, Angular sẽ **luôn sử dụng file `src/environments/environment.prod.ts`** để lấy các biến môi trường (ví dụ: domain API).
- Nếu muốn đổi domain API (hoặc các biến môi trường khác), bạn **phải sửa giá trị trong file `environment.prod.ts`** rồi build lại Docker image FE.
- Khi chạy lệnh build FE (trong Dockerfile hoặc docker-compose), Angular sẽ tự động dùng file này cho production build.

### Ví dụ thay đổi domain API:
1. Mở file: `frontend/src/environments/environment.prod.ts`
2. Sửa dòng:
   ```ts
   export const environment = {
     production: true,
     apiUrl: 'http://localhost:8080' // Đổi thành domain backend thật nếu deploy
   };
   ```
3. Build lại image FE:
   ```bash
   docker-compose build frontend
   docker-compose up -d frontend
   ```

## Lưu ý
- Docker Compose **không tự động chọn file môi trường** cho Angular, mà Angular CLI sẽ tự động dùng file `environment.prod.ts` khi build production.
- Nếu muốn domain API động mà không cần build lại image, hãy tham khảo giải pháp đọc config từ file ngoài (assets/config.json), cần sửa code FE.

## Các thông tin cũ/lỗi thời đã được xoá khỏi README này để tránh nhầm lẫn. 