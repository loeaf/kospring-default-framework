
#!/bin/bash

set -e

# ==========================================
# 설정
# ==========================================
HARBOR_REGISTRY="211.230.2.71:50029"
HARBOR_USERNAME="admin"
HARBOR_PASSWORD="ehtldhkShchd0315)#"
IMAGE_NAME="cnc-app"
IMAGE_TAG="latest"
HARBOR_PROJECT="library"
FULL_IMAGE_NAME="${HARBOR_REGISTRY}/${HARBOR_PROJECT}/${IMAGE_NAME}:${IMAGE_TAG}"
CONTAINER_NAME="cnc-app"
PORT="8080"

# ==========================================
# Harbor 레지스트리 로그인
# ==========================================
echo "🔐 Logging into Harbor registry..."
echo "${HARBOR_PASSWORD}" | docker login http://${HARBOR_REGISTRY} -u ${HARBOR_USERNAME} --password-stdin

if [ $? -ne 0 ]; then
    echo "❌ Failed to login to Harbor registry"
    exit 1
fi

echo "✅ Successfully logged in to Harbor"

# ==========================================
# 최신 이미지 풀
# ==========================================
echo "📥 Pulling latest image from Harbor..."
echo "Image: ${FULL_IMAGE_NAME}"

docker pull ${FULL_IMAGE_NAME}

if [ $? -ne 0 ]; then
    echo "❌ Failed to pull image"
    exit 1
fi

echo "✅ Successfully pulled image"

# ==========================================
# 기존 컨테이너 정리
# ==========================================
echo "🧹 Cleaning up existing containers..."

# 포트를 사용 중인 컨테이너 중지 및 삭제
if [ $(docker ps -q --filter "publish=${PORT}" | wc -l) -gt 0 ]; then
    echo "Stopping containers using port ${PORT}..."
    docker stop $(docker ps -q --filter "publish=${PORT}") 2>/dev/null || true
    docker rm $(docker ps -aq --filter "publish=${PORT}") 2>/dev/null || true
fi

# 같은 이름의 컨테이너 삭제
if [ $(docker ps -a -q -f name=${CONTAINER_NAME} | wc -l) -gt 0 ]; then
    echo "Removing existing container: ${CONTAINER_NAME}"
    docker rm -f ${CONTAINER_NAME} 2>/dev/null || true
fi

echo "✅ Cleanup completed"

# ==========================================
# 새 컨테이너 시작
# ==========================================
echo "🚀 Starting new container..."

docker run -d \
  --name ${CONTAINER_NAME} \
  --restart unless-stopped \
  -p ${PORT}:8080 \
  ${FULL_IMAGE_NAME}

if [ $? -ne 0 ]; then
    echo "❌ Failed to start container"
    exit 1
fi

# ==========================================
# 컨테이너 상태 확인
# ==========================================
echo "⏳ Waiting for container to start..."
sleep 3

if docker ps | grep -q ${CONTAINER_NAME}; then
    echo ""
    echo "✅ =========================================="
    echo "✅  Container is running successfully!"
    echo "✅ =========================================="
    echo ""
    echo "📋 Container Info:"
    echo "   - Name: ${CONTAINER_NAME}"
    echo "   - Image: ${FULL_IMAGE_NAME}"
    echo "   - Port: ${PORT}"
    echo "   - URL: http://localhost:${PORT}"
    echo ""
    echo "🔍 To view logs:"
    echo "   docker logs -f ${CONTAINER_NAME}"
    echo ""
    echo "🛑 To stop container:"
    echo "   docker stop ${CONTAINER_NAME}"
    echo ""
else
    echo ""
    echo "❌ =========================================="
    echo "❌  Container failed to start!"
    echo "❌ =========================================="
    echo ""
    echo "📋 Checking logs..."
    docker logs ${CONTAINER_NAME}
    exit 1
fi
