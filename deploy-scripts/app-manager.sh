#!/bin/bash

# Application Manager Script for EC2
# Usage: ./app-manager.sh {start|stop|restart|status|logs}

APP_DIR="/home/ec2-user/app"
APP_NAME="slack-cab-bot"
JAR_FILE="$APP_DIR/slack.cab-0.0.1-SNAPSHOT.jar"
LOG_FILE="$APP_DIR/app.log"
PID_FILE="$APP_DIR/app.pid"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to get application PID
get_app_pid() {
    pgrep -f "slack.cab.*jar" 2>/dev/null
}

# Function to check if app is running
is_running() {
    local pid=$(get_app_pid)
    [ -n "$pid" ]
}

# Function to start the application
start_app() {
    if is_running; then
        print_warning "Application is already running (PID: $(get_app_pid))"
        return 1
    fi

    print_status "Starting $APP_NAME..."
    cd $APP_DIR

    if [ ! -f $JAR_FILE ]; then
        print_error "JAR file not found in $APP_DIR"
        return 1
    fi

    # Start application in background
    nohup java -jar $JAR_FILE > $LOG_FILE 2>&1 &
    local pid=$!
    echo $pid > $PID_FILE

    # Wait and verify startup
    sleep 10
    if is_running; then
        print_status "✅ Application started successfully"
        print_status "PID: $(get_app_pid)"
        print_status "Log file: $LOG_FILE"
        
        # Check if port 8080 is listening
        if netstat -tlnp 2>/dev/null | grep -q :8080; then
            print_status "✅ Application is listening on port 8080"
        else
            print_warning "⚠️  Port 8080 not yet bound, check logs"
        fi
    else
        print_error "❌ Failed to start application"
        print_error "Check logs: tail -f $LOG_FILE"
        return 1
    fi
}

# Function to stop the application
stop_app() {
    if ! is_running; then
        print_warning "Application is not running"
        return 1
    fi

    local pid=$(get_app_pid)
    print_status "Stopping $APP_NAME (PID: $pid)..."
    
    # Graceful shutdown
    kill $pid
    
    # Wait for graceful shutdown
    local count=0
    while is_running && [ $count -lt 30 ]; do
        sleep 1
        count=$((count + 1))
    done
    
    # Force kill if still running
    if is_running; then
        print_warning "Graceful shutdown failed, force killing..."
        kill -9 $pid
        sleep 2
    fi
    
    if is_running; then
        print_error "❌ Failed to stop application"
        return 1
    else
        print_status "✅ Application stopped successfully"
        rm -f $PID_FILE
    fi
}

# Function to restart the application
restart_app() {
    print_status "Restarting $APP_NAME..."
    stop_app
    sleep 3
    start_app
}

# Function to show application status
show_status() {
    if is_running; then
        local pid=$(get_app_pid)
        print_status "✅ Application is running"
        print_status "PID: $pid"
        print_status "Memory usage: $(ps -p $pid -o %mem --no-headers 2>/dev/null | tr -d ' ')%"
        print_status "CPU usage: $(ps -p $pid -o %cpu --no-headers 2>/dev/null | tr -d ' ')%"
        
        if netstat -tlnp 2>/dev/null | grep -q :8080; then
            print_status "✅ Port 8080 is listening"
        else
            print_warning "⚠️  Port 8080 is not listening"
        fi
    else
        print_error "❌ Application is not running"
    fi
}

# Function to show logs
show_logs() {
    if [ -f $LOG_FILE ]; then
        print_status "Showing last 50 lines of $LOG_FILE"
        echo "----------------------------------------"
        tail -50 $LOG_FILE
    else
        print_error "Log file not found: $LOG_FILE"
    fi
}

# Main script logic
case "$1" in
    start)
        start_app
        ;;
    stop)
        stop_app
        ;;
    restart)
        restart_app
        ;;
    status)
        show_status
        ;;
    logs)
        show_logs
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|status|logs}"
        echo ""
        echo "Commands:"
        echo "  start   - Start the application"
        echo "  stop    - Stop the application"
        echo "  restart - Restart the application"
        echo "  status  - Show application status"
        echo "  logs    - Show application logs"
        exit 1
        ;;
esac

exit $?
