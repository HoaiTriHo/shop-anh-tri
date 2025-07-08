# Multi-stage build for Spring Boot backend application
# This Dockerfile optimizes the build process and creates a lightweight production image

# Stage 1: Build the application
FROM gradle:8.4-jdk17 AS build

# Set working directory for the build stage
WORKDIR /app

# Copy Gradle files for dependency resolution
COPY build.gradle settings.gradle ./

# Copy source code
COPY src ./src

# Build the application
# This creates a JAR file with all dependencies
RUN gradle build --no-daemon

# Stage 2: Run the application
FROM eclipse-temurin:17-jre AS production

# Create a non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring

# Set working directory
WORKDIR /app

# Copy the built JAR file from build stage
#COPY --from=build /app/build/libs/*.jar app.jar
COPY --from=build /app/build/libs/*.jar ./

# Change ownership to spring user
RUN chown spring:spring app.jar

# Switch to spring user
USER spring

# Expose port 8080 for the application
EXPOSE 8080

# Health check configuration
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/health || exit 1

# JVM options for production
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"

# Start the application
#ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar *.jar"]


# Stage 3: Development stage (optional)
FROM gradle:8.4-jdk17 AS development

# Set working directory
WORKDIR /app

# Copy Gradle files
COPY build.gradle settings.gradle ./

# Copy source code
COPY src ./src

# Expose port 8080 for the application
EXPOSE 8080

# Start the application in development mode
CMD ["gradle", "bootRun"] 