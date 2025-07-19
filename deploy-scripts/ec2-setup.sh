#!/bin/bash

# EC2 Instance Setup Script for Java Spring Boot Application
# Run this script once on your EC2 instance to prepare the environment

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_header() {
    echo -e "\n${BLUE}=== $1 ===${NC}"
}

print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if running as ec2-user
if [ "$USER" != "ec2-user" ]; then
    print_error "This script should be run as ec2-user"
    exit 1
fi

print_header "EC2 Instance Setup for Spring Boot Application"

# Update system packages
print_header "Updating System Packages"
sudo yum update -y

# Install Java 21
print_header "Installing Java 21"
if java -version 2>&1 | grep -q "21\."; then
    print_status "Java 21 is already installed"
    java -version
else
    print_status "Installing Amazon Corretto 21..."
    sudo yum install -y java-21-amazon-corretto-devel
    
    # Set JAVA_HOME
    echo 'export JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto' >> ~/.bashrc
    echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
    source ~/.bashrc
    
    print_status "Java 21 installed successfully"
    java -version
fi

# Create application directory
print_header "Setting Up Application Directory"
APP_DIR="/home/ec2-user/app"
if [ ! -d "$APP_DIR" ]; then
    mkdir -p $APP_DIR
    print_status "Created application directory: $APP_DIR"
else
    print_status "Application directory already exists: $APP_DIR"
fi

# Set proper permissions
chmod 755 $APP_DIR
print_status "Set permissions for application directory"

# Install useful tools
print_header "Installing Additional Tools"
sudo yum install -y \
    htop \
    tree \
    wget \
    curl \
    unzip \
    git \
    net-tools

# Configure firewall for port 8080
print_header "Configuring Security"
print_status "Opening port 8080 in firewall..."

# For Amazon Linux 2
if command -v firewall-cmd &> /dev/null; then
    sudo firewall-cmd --permanent --add-port=8080/tcp
    sudo firewall-cmd --reload
    print_status "Firewall configured for port 8080"
else
    print_warning "firewall-cmd not found, make sure Security Group allows port 8080"
fi

# Create systemd service (optional - for production use)
print_header "Creating Systemd Service (Optional)"
cat > /tmp/slack-cab-bot.service << 'EOF'
[Unit]
Description=Slack CAB Bot Spring Boot Application
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/home/ec2-user/app
ExecStart=/usr/bin/java -jar /home/ec2-user/app/slack.cab-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF

sudo mv /tmp/slack-cab-bot.service /etc/systemd/system/
sudo systemctl daemon-reload
print_status "Systemd service created (not enabled by default)"
print_status "To use systemd service:"
print_status "  sudo systemctl enable slack-cab-bot"
print_status "  sudo systemctl start slack-cab-bot"

# Create log rotation configuration
print_header "Setting Up Log Rotation"
cat > /tmp/slack-cab-bot << 'EOF'
/home/ec2-user/app/app.log {
    daily
    missingok
    rotate 7
    compress
    delaycompress
    notifempty
    copytruncate
    su ec2-user ec2-user
}
EOF

sudo mv /tmp/slack-cab-bot /etc/logrotate.d/
print_status "Log rotation configured"

# Download and setup app manager script
print_header "Setting Up Application Manager"
if [ ! -f "$APP_DIR/app-manager.sh" ]; then
    # If the script exists in the repo, copy it
    if [ -f "deploy-scripts/app-manager.sh" ]; then
        cp deploy-scripts/app-manager.sh $APP_DIR/
    else
        print_warning "app-manager.sh not found in deploy-scripts/"
        print_status "You can copy it manually later"
    fi
fi

if [ -f "$APP_DIR/app-manager.sh" ]; then
    chmod +x $APP_DIR/app-manager.sh
    print_status "Application manager script is ready"
    print_status "Usage: $APP_DIR/app-manager.sh {start|stop|restart|status|logs}"
fi

# Create useful aliases
print_header "Setting Up Aliases"
cat >> ~/.bashrc << 'EOF'

# Application management aliases
alias app-start='cd /home/ec2-user/app && ./app-manager.sh start'
alias app-stop='cd /home/ec2-user/app && ./app-manager.sh stop'
alias app-restart='cd /home/ec2-user/app && ./app-manager.sh restart'
alias app-status='cd /home/ec2-user/app && ./app-manager.sh status'
alias app-logs='cd /home/ec2-user/app && ./app-manager.sh logs'
alias app-tail='tail -f /home/ec2-user/app/app.log'
alias app-dir='cd /home/ec2-user/app'
EOF

print_status "Aliases added to ~/.bashrc"

# Display summary
print_header "Setup Complete!"
print_status "✅ Java 21 installed"
print_status "✅ Application directory created: $APP_DIR"
print_status "✅ Firewall configured for port 8080"
print_status "✅ Systemd service created"
print_status "✅ Log rotation configured"
print_status "✅ Application manager script ready"
print_status "✅ Useful aliases added"

echo ""
print_header "Next Steps:"
echo "1. Make sure your Security Group allows inbound traffic on port 8080"
echo "2. Deploy your JAR file to $APP_DIR"
echo "3. Use the application manager: $APP_DIR/app-manager.sh start"
echo "4. Or use aliases: app-start, app-stop, app-status, app-logs"
echo ""
print_status "Reload your shell: source ~/.bashrc"
print_status "Check Java version: java -version"
print_status "Test deployment: Deploy your JAR and run app-start"

echo ""
print_header "Useful Commands:"
echo "• app-start     - Start the application"
echo "• app-stop      - Stop the application"
echo "• app-restart   - Restart the application"
echo "• app-status    - Check application status"
echo "• app-logs      - View application logs"
echo "• app-tail      - Follow logs in real-time"
echo "• app-dir       - Go to application directory"
