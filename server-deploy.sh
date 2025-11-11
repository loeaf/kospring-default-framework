#!/bin/bash

set -e

HARBOR_REGISTRY="211.230.2.71:50029"
HARBOR_USERNAME="admin"
HARBOR_PASSWORD="ehtldhkShchd0315)#"
IMAGE_NAME="cnc"
IMAGE_TAG="latest"
HARBOR_PROJECT="library"
FULL_IMAGE_NAME="${HARBOR_REGISTRY}/${HARBOR_PROJECT}/${IMAGE_NAME}:${IMAGE_TAG}"
CONTAINER_NAME="cnc-app"
PORT="50006"

echo "Starting server deployment process..."

echo "Logging into Harbor registry..."
echo "${HARBOR_PASSWORD}" | docker login http://${HARBOR_REGISTRY} -u ${HARBOR_USERNAME} --password-stdin

echo "Pulling latest image from Harbor..."
docker pull ${FULL_IMAGE_NAME}

echo "Stopping existing containers on port ${PORT}..."
docker stop $(docker ps -q --filter "publish=${PORT}") 2>/dev/null || true
docker rm $(docker ps -aq --filter "publish=${PORT}") 2>/dev/null || true

echo "Removing existing container ${CONTAINER_NAME}..."
docker rm -f ${CONTAINER_NAME} 2>/dev/null || true

echo "Starting new container..."
docker run -d \
  --name ${CONTAINER_NAME} \
  --restart unless-stopped \
  -p ${PORT}:3000 \
  ${FULL_IMAGE_NAME}

sleep 2
if docker ps | grep -q ${CONTAINER_NAME}; then
    echo "Container ${CONTAINER_NAME} is running successfully!"
    echo "Application is available at: http://localhost:${PORT}"
else
    echo "Container failed to start. Checking logs..."
    docker logs ${CONTAINER_NAME}
    exit 1
fi