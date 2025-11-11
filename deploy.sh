#!/bin/bash

set -e

echo "Starting build and deployment process..."

./mvnw clean package -DskipTests

HARBOR_REGISTRY="211.230.2.71:50029"
HARBOR_USERNAME="admin"
HARBOR_PASSWORD="ehtldhkShchd0315)#"
IMAGE_NAME="cnc-app"
IMAGE_TAG="latest"
HARBOR_PROJECT="library"
FULL_IMAGE_NAME="${HARBOR_REGISTRY}/${HARBOR_PROJECT}/${IMAGE_NAME}:${IMAGE_TAG}"

echo "Logging into Harbor registry..."
echo "${HARBOR_PASSWORD}" | docker login ${HARBOR_REGISTRY} -u ${HARBOR_USERNAME} --password-stdin

echo "Building Docker image..."
docker build --platform linux/amd64 -t ${IMAGE_NAME}:${IMAGE_TAG} .

echo "Tagging image for Harbor..."
docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${FULL_IMAGE_NAME}

echo "Pushing image to Harbor..."
docker push ${FULL_IMAGE_NAME}

echo "Build and push completed successfully!"
echo "Image: ${FULL_IMAGE_NAME}"




