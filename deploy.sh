#!/bin/bash
set -e

cd /home/ec2-user/hoops

# Load environment variables
source .env

# Pull latest code
if [ -d "app" ]; then
    cd app
    git pull origin main
else
    git clone https://github.com/kangminhyuk1111/hoops.git app
    cd app
fi

# Sync deploy script from source
cp -f deploy.sh /home/ec2-user/hoops/deploy.sh

# Build backend image
echo "Building backend image..."
docker build -t hoops-backend:latest ./backend

# Build frontend image
echo "Building frontend image..."
docker build -t hoops-frontend:latest \
    --build-arg NEXT_PUBLIC_API_URL=http://${PUBLIC_IP}:8080 \
    --build-arg NEXT_PUBLIC_KAKAO_CLIENT_ID=${KAKAO_CLIENT_ID} \
    --build-arg NEXT_PUBLIC_KAKAO_REDIRECT_URI=${KAKAO_REDIRECT_URI} \
    --build-arg NEXT_PUBLIC_KAKAO_JS_KEY=c963cdb39992726ac3b783c8e215f1ae \
    ./frontend

# Return to hoops directory
cd /home/ec2-user/hoops

# Restart only backend and frontend (keep MySQL running)
echo "Restarting backend and frontend..."
docker-compose up -d --no-deps backend frontend

# Restart monitoring containers
echo "Restarting monitoring..."
docker-compose -f app/monitoring/docker-compose.monitoring.yml up -d

# Cleanup unused images
docker image prune -f

echo "Deployment complete!"
echo "Backend: http://${PUBLIC_IP}:8080"
echo "Frontend: http://${PUBLIC_IP}:3000"
echo "Grafana: http://${PUBLIC_IP}:3001"
