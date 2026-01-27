#!/bin/bash
set -e

cd /home/ec2-user/hoops

# Load environment variables
source .env

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
    --build-arg NEXT_PUBLIC_API_URL=http://${PUBLIC_IP}:8080 \
    --build-arg NEXT_PUBLIC_KAKAO_CLIENT_ID=${KAKAO_CLIENT_ID} \
    --build-arg NEXT_PUBLIC_KAKAO_REDIRECT_URI=${KAKAO_REDIRECT_URI} \
    --build-arg NEXT_PUBLIC_KAKAO_JS_KEY=${KAKAO_JS_KEY} \
    ./frontend

# Return to hoops directory
cd /home/ec2-user/hoops

# Restart redis, backend and frontend (keep MySQL running)
echo "Restarting redis, backend and frontend..."
docker-compose up -d --no-deps redis backend frontend

# Restart monitoring containers
echo "Restarting monitoring..."
docker-compose -f app/monitoring/docker-compose.monitoring.yml up -d

# Cleanup unused images
docker image prune -f

# Sync deploy script and docker-compose from source
cp -f app/deploy.sh /home/ec2-user/hoops/deploy.sh
cp -f app/docker-compose.yml /home/ec2-user/hoops/docker-compose.yml

echo "Deployment complete!"
echo "Backend: http://${PUBLIC_IP}:8080"
echo "Frontend: http://${PUBLIC_IP}:3000"
echo "Grafana: http://${PUBLIC_IP}:3001"
