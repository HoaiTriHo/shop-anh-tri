# Multi-stage build for Spring Boot backend application
# This Dockerfile optimizes the build process and creates a lightweight production image

# Stage 1: Build the application
FROM gradle:8.4-jdk17 AS build

# Đặt thư mục làm việc cho stage build
WORKDIR /app

# Copy file cấu hình Gradle để cài dependencies trước (tăng tốc build cache)
COPY build.gradle settings.gradle ./

# Copy toàn bộ source code vào image
COPY src ./src

# Build ứng dụng, tạo file JAR trong build/libs/
# Thêm flag --info để xem thông tin chi tiết về quá trình build
RUN gradle build --no-daemon --info

# Kiểm tra xem file JAR có được tạo không
RUN ls -la build/libs/ || echo "No JAR files found in build/libs/"

# Stage 2: Tạo image production nhẹ
FROM eclipse-temurin:17-jre AS production

# Tạo user không phải root để tăng bảo mật
RUN groupadd -r spring && useradd -r -g spring spring

# Đặt thư mục làm việc
WORKDIR /app

# Copy file JAR chính (không phải plain JAR) từ stage trước
# Sử dụng pattern cụ thể để chỉ copy file JAR chính, không copy plain JAR
COPY --from=build /app/build/libs/shop-backend-0.0.1-SNAPSHOT.jar ./app.jar
# Copy file cấu hình application.properties vào đúng vị trí để Spring Boot đọc
COPY src/main/resources/application.properties ./application.properties

# Chuyển quyền sở hữu file cho user spring
RUN chown spring:spring app.jar

# Chạy app với user spring (không phải root)
USER spring

# Mở port 8080 cho ứng dụng
EXPOSE 8080

# Health check: kiểm tra endpoint /api/health mỗi 30s
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/health || exit 1

# Thiết lập biến môi trường cho JVM (có thể chỉnh sửa khi chạy container)
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"

# Khởi động ứng dụng với JAVA_OPTS và file app.jar
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]


# Stage 3: Development stage (optional, chỉ dùng khi phát triển local)
FROM gradle:8.4-jdk17 AS development

# Đặt thư mục làm việc
WORKDIR /app

# Copy file cấu hình Gradle
COPY build.gradle settings.gradle ./

# Copy source code
COPY src ./src

# Mở port 8080 cho dev
EXPOSE 8080

# Chạy ứng dụng ở chế độ dev (hot reload)
CMD ["gradle", "bootRun"] 