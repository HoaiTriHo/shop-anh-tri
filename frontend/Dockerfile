# Multi-stage build for Angular frontend application
# This Dockerfile optimizes the build process and creates a lightweight production image

# Stage 1: Build the application
FROM node:18-alpine AS build

# Set working directory
WORKDIR /app

# Copy package files for dependency resolution
COPY package*.json ./

# Install dependencies
# Using npm ci for faster, reliable builds in production
RUN npm install

# Copy source code
COPY . .

# Build the application for production
# This creates optimized files in the dist folder
RUN npm run build

# Stage 2: Serve the application with Nginx
FROM nginx:alpine AS production

# Copy custom nginx configuration
# This configuration handles Angular routing and API proxy
COPY nginx.conf /etc/nginx/nginx.conf

# Copy built application from build stage
COPY --from=build /app/dist/shop-frontend /usr/share/nginx/html

# Copy template env file
COPY src/assets/env.template.js /usr/share/nginx/html/assets/env.template.js

# Add entrypoint script
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Expose port 3000 for the application
EXPOSE 3000

# Start Nginx
ENTRYPOINT ["/entrypoint.sh"]
CMD ["nginx", "-g", "daemon off;"]

# Stage 3: Development stage (optional)
FROM node:18-alpine AS development

# Set working directory
WORKDIR /app

# Copy package files
COPY package*.json ./

# Install all dependencies including dev dependencies
RUN npm install

# Copy source code
COPY . .

# Expose port 4200 for Angular dev server
EXPOSE 4200

# Start Angular development server
CMD ["npm", "start"] 