#!/bin/bash

# Quick Start Script for E-commerce Shop Application
# This script provides easy commands to start, stop, and manage the application

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_header() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE}  E-commerce Shop Application${NC}"
    echo -e "${BLUE}================================${NC}"
}

# Function to check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker and try again."
        exit 1
    fi
}

# Function to start the application
start_app() {
    print_header
    print_status "Starting E-commerce Shop Application..."
    
    check_docker
    
    print_status "Building and starting all services..."
    docker-compose up -d --build
    
    print_status "Waiting for services to be ready..."
    sleep 30
    
    print_status "Application is starting up!"
    print_status "Frontend: http://localhost"
    print_status "Backend API: http://localhost:8080/api"
    print_status "Database: localhost:3306"
    
    print_status "Use './start.sh logs' to view logs"
    print_status "Use './start.sh stop' to stop the application"
}

# Function to stop the application
stop_app() {
    print_header
    print_status "Stopping E-commerce Shop Application..."
    
    docker-compose down
    
    print_status "Application stopped successfully!"
}

# Function to restart the application
restart_app() {
    print_header
    print_status "Restarting E-commerce Shop Application..."
    
    stop_app
    sleep 5
    start_app
}

# Function to show logs
show_logs() {
    print_header
    print_status "Showing application logs..."
    
    docker-compose logs -f
}

# Function to show status
show_status() {
    print_header
    print_status "Application Status:"
    
    echo ""
    docker-compose ps
    echo ""
    
    print_status "Service Health Checks:"
    echo ""
    
    # Check MySQL
    if docker-compose exec -T mysql mysqladmin ping -h localhost > /dev/null 2>&1; then
        echo -e "${GREEN}✓ MySQL: Running${NC}"
    else
        echo -e "${RED}✗ MySQL: Not responding${NC}"
    fi
    
    # Check Backend
    if curl -f http://localhost:8080/api/health > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Backend: Running${NC}"
    else
        echo -e "${RED}✗ Backend: Not responding${NC}"
    fi
    
    # Check Frontend
    if curl -f http://localhost/health > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Frontend: Running${NC}"
    else
        echo -e "${RED}✗ Frontend: Not responding${NC}"
    fi
}

# Function to clean up
cleanup() {
    print_header
    print_warning "This will remove all containers, volumes, and images!"
    read -p "Are you sure? (y/N): " -n 1 -r
    echo
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        print_status "Cleaning up application..."
        docker-compose down -v --rmi all
        print_status "Cleanup completed!"
    else
        print_status "Cleanup cancelled."
    fi
}

# Function to show help
show_help() {
    print_header
    echo "Usage: $0 [COMMAND]"
    echo ""
    echo "Commands:"
    echo "  start     Start the application (default)"
    echo "  stop      Stop the application"
    echo "  restart   Restart the application"
    echo "  logs      Show application logs"
    echo "  status    Show application status"
    echo "  cleanup   Remove all containers and volumes"
    echo "  help      Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 start     # Start the application"
    echo "  $0 logs      # View logs"
    echo "  $0 stop      # Stop the application"
}

# Main script logic
case "${1:-start}" in
    start)
        start_app
        ;;
    stop)
        stop_app
        ;;
    restart)
        restart_app
        ;;
    logs)
        show_logs
        ;;
    status)
        show_status
        ;;
    cleanup)
        cleanup
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        print_error "Unknown command: $1"
        echo ""
        show_help
        exit 1
        ;;
esac 