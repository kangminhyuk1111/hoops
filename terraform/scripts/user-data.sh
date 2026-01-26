#!/bin/bash
set -e

# Log output
exec > >(tee /var/log/user-data.log) 2>&1
echo "Starting user-data script at $(date)"

# Update system
dnf update -y

# Install Docker
dnf install -y docker
systemctl start docker
systemctl enable docker

# Install Docker Compose
curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose
ln -sf /usr/local/bin/docker-compose /usr/bin/docker-compose

# Install git
dnf install -y git

# Add ec2-user to docker group
usermod -aG docker ec2-user

# Create app directory
mkdir -p /home/ec2-user/hoops
chown ec2-user:ec2-user /home/ec2-user/hoops

# Create environment file
cat > /home/ec2-user/hoops/.env << 'ENVEOF'
# MySQL
MYSQL_ROOT_PASSWORD=${mysql_root_password}

# Kakao OAuth
KAKAO_CLIENT_ID=${kakao_client_id}
KAKAO_CLIENT_SECRET=${kakao_client_secret}
KAKAO_JS_KEY=${kakao_js_key}
KAKAO_REDIRECT_URI=http://${domain_name}/api/auth/kakao/callback

# JWT
JWT_SECRET=${jwt_secret}

# Spring
SPRING_PROFILES_ACTIVE=prod

# Server
PUBLIC_IP=${domain_name}

# Grafana
GF_SECURITY_ADMIN_PASSWORD=${grafana_admin_password}
ENVEOF

chown ec2-user:ec2-user /home/ec2-user/hoops/.env
chmod 600 /home/ec2-user/hoops/.env

# Create docker-compose.yml for production
cat > /home/ec2-user/hoops/docker-compose.yml << 'COMPOSEEOF'
services:
  mysql:
    image: mysql:8.0
    container_name: hoops-mysql
    environment:
      MYSQL_ROOT_PASSWORD: $${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: hoops
      MYSQL_CHARACTER_SET_SERVER: utf8mb4
      MYSQL_COLLATION_SERVER: utf8mb4_unicode_ci
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    networks:
      - hoops-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-p$${MYSQL_ROOT_PASSWORD}"]
      interval: 10s
      timeout: 5s
      retries: 5
    restart: unless-stopped

  backend:
    image: hoops-backend:latest
    container_name: hoops-backend
    depends_on:
      mysql:
        condition: service_healthy
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: $${SPRING_PROFILES_ACTIVE:-prod}
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/hoops?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: $${MYSQL_ROOT_PASSWORD}
      KAKAO_CLIENT_ID: $${KAKAO_CLIENT_ID}
      KAKAO_CLIENT_SECRET: $${KAKAO_CLIENT_SECRET}
      KAKAO_REDIRECT_URI: $${KAKAO_REDIRECT_URI}
      JWT_SECRET: $${JWT_SECRET}
    networks:
      - hoops-network
    restart: unless-stopped

  frontend:
    image: hoops-frontend:latest
    container_name: hoops-frontend
    depends_on:
      - backend
    ports:
      - "3000:3000"
    networks:
      - hoops-network
    restart: unless-stopped

networks:
  hoops-network:
    driver: bridge

volumes:
  mysql-data:
COMPOSEEOF

chown ec2-user:ec2-user /home/ec2-user/hoops/docker-compose.yml

# Create deployment script
cat > /home/ec2-user/hoops/deploy.sh << 'DEPLOYEOF'
#!/bin/bash
set -e

cd /home/ec2-user/hoops

# Pull latest code
if [ -d "app" ]; then
    cd app
    git pull origin main
else
    git clone https://github.com/kangminhyuk1111/hoops.git app
    cd app
fi

# Build backend image
echo "Building backend image..."
docker build -t hoops-backend:latest ./backend

# Build frontend image
echo "Building frontend image..."
docker build -t hoops-frontend:latest \
    --build-arg NEXT_PUBLIC_API_URL=http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4):8080 \
    --build-arg NEXT_PUBLIC_KAKAO_CLIENT_ID=$KAKAO_CLIENT_ID \
    --build-arg NEXT_PUBLIC_KAKAO_REDIRECT_URI=$KAKAO_REDIRECT_URI \
    ./frontend

# Return to hoops directory
cd /home/ec2-user/hoops

# Start or restart services
echo "Starting services..."
# Start MySQL if not running
docker-compose up -d mysql
# Wait for MySQL to be healthy
sleep 10
# Restart backend and frontend only
docker-compose up -d --no-deps backend frontend

# Cleanup unused images
docker image prune -f

echo "Deployment complete!"
echo "Backend: http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4):8080"
echo "Frontend: http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4):3000"
DEPLOYEOF

chmod +x /home/ec2-user/hoops/deploy.sh
chown ec2-user:ec2-user /home/ec2-user/hoops/deploy.sh

echo "User-data script completed at $(date)"
