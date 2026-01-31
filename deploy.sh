#!/bin/bash
set -e

cd /home/ec2-user/hoops

# Load environment variables
source .env

DOMAIN=${DOMAIN:-hoops-basketball.shop}

# Pull latest code
if [ -d "app" ]; then
    cd app
    git stash --include-untracked 2>/dev/null || true
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
    --build-arg NEXT_PUBLIC_API_URL=https://${DOMAIN} \
    --build-arg NEXT_PUBLIC_KAKAO_CLIENT_ID=${KAKAO_CLIENT_ID} \
    --build-arg NEXT_PUBLIC_KAKAO_REDIRECT_URI=https://${DOMAIN}/auth/kakao/callback \
    --build-arg NEXT_PUBLIC_KAKAO_JS_KEY=${KAKAO_JS_KEY} \
    ./frontend

# Return to hoops directory
cd /home/ec2-user/hoops

# Sync docker-compose.yml and nginx config
cp -f app/docker-compose.yml /home/ec2-user/hoops/docker-compose.yml
mkdir -p /home/ec2-user/hoops/nginx
cp -f app/nginx/nginx.conf /home/ec2-user/hoops/nginx/nginx.conf

# Restart services (keep MySQL running)
echo "Restarting redis, backend, frontend, nginx..."
docker-compose up -d --no-deps redis backend frontend nginx

# Restart monitoring containers
echo "Restarting monitoring..."
docker-compose -f app/monitoring/docker-compose.monitoring.yml up -d

# Cleanup unused images
docker image prune -f

# Sync deploy script from source (at the very end)
cp -f app/deploy.sh /home/ec2-user/hoops/deploy.sh

echo "Deployment complete!"
echo "Site: https://${DOMAIN}"
echo "Grafana: http://${PUBLIC_IP}:3001"
