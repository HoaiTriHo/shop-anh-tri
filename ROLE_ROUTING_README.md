# Role-Based Routing Feature

## Tổng quan
Tính năng này tự động điều hướng người dùng dựa trên role của họ:
- **Admin users**: Tự động được redirect về `/admin` khi access root hoặc các route không phải admin
- **Regular users**: Không thể access các route có `/admin`, sẽ bị redirect về `/home`

## Các file đã được tạo/cập nhật

### 1. AdminGuard (`frontend/src/app/services/admin.guard.ts`)
- Bảo vệ các admin routes
- Chỉ cho phép user có role `ADMIN` access
- Redirect user thường về `/home` nếu cố tình access admin routes

### 2. RoleRoutingService (`frontend/src/app/services/role-routing.service.ts`)
- Service chính để handle role-based routing
- Tự động redirect admin về `/admin` khi access root hoặc non-admin routes
- Ngăn user thường access admin routes

### 3. RootRedirectComponent (`frontend/src/app/root-redirect/root-redirect.component.ts`)
- Component để handle redirect từ root route (`/`)
- Tự động redirect dựa trên authentication và role

### 4. Cập nhật AppRoutingModule
- Thêm `AdminGuard` cho admin routes
- Sử dụng `RootRedirectComponent` cho root route

### 5. Cập nhật AppComponent
- Thêm logic để handle role-based routing khi route thay đổi
- Tự động redirect khi auth state thay đổi

### 6. Cập nhật LoginComponent
- Sử dụng `RoleRoutingService` để navigate sau khi login

### 7. Cập nhật UserOnlyGuard
- Bỏ logic redirect admin về admin panel (đã có AdminGuard)

## Cách test

### Test với Admin user:
1. Login với tài khoản admin
2. Access `localhost:3000` → Sẽ tự động redirect về `localhost:3000/admin`
3. Access `localhost:3000/home` → Sẽ tự động redirect về `localhost:3000/admin`
4. Access `localhost:3000/products` → Sẽ tự động redirect về `localhost:3000/admin`
5. Access `localhost:3000/admin/orders` → Được phép access

### Test với Regular user:
1. Login với tài khoản user thường
2. Access `localhost:3000` → Sẽ redirect về `localhost:3000/home`
3. Access `localhost:3000/admin` → Sẽ bị redirect về `localhost:3000/home`
4. Access `localhost:3000/admin/orders` → Sẽ bị redirect về `localhost:3000/home`
5. Access `localhost:3000/products` → Được phép access

### Test khi chưa login:
1. Access `localhost:3000` → Sẽ redirect về `localhost:3000/home`
2. Access `localhost:3000/admin` → Sẽ redirect về login page
3. Access `localhost:3000/cart` → Sẽ redirect về login page

## Lưu ý
- Admin có thể access tất cả routes (cả user và admin routes)
- User thường chỉ có thể access user routes
- Khi user thường cố tình access admin routes, họ sẽ bị redirect về home page
- Khi admin access non-admin routes, họ sẽ được redirect về admin dashboard 