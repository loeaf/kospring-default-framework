# 아래 기능 한번에 실행 Start ===============================================================================
# 할당
echo "=== 1. 라운드에 광고 할당 생성 ==="
curl -X POST "http://localhost:8080/api/posts/rounds/1/circular-assignments" \
  -H "Content-Type: application/json"

echo -e "\n\n"

# 포스트 생성
echo "=== 2. 라운드의 모든 할당에 대해 포스트 일괄 생성 ==="
curl -X POST "http://localhost:8080/api/advertisement-posts/test/rounds/1/create-all" \
  -H "Content-Type: application/json"

echo -e "\n\n"

# 포스트 상태 변경 - 승인
echo "=== 3. 모든 포스트 승인 ==="
curl -X PUT "http://localhost:8080/api/advertisement-posts/test/rounds/1/status/APPROVED" \
  -H "Content-Type: application/json"

echo -e "\n\n"

# 아래 기능 한번에 실행 End ===============================================================================

# 주문 진행률 확인 (승인 후)
echo "=== 3-1. 주문 진행률 업데이트 (승인 후) ==="
curl -X PUT "http://localhost:8080/api/orders/round/1/update-progress" \
  -H "Content-Type: application/json"

echo -e "\n\n"

# 포스트 상태 변경 - 게시
echo "=== 4. 모든 포스트 게시 ==="
curl -X PUT "http://localhost:8080/api/advertisement-posts/test/rounds/1/status/PUBLISHED" \
  -H "Content-Type: application/json"

echo "모든 포스터가 게시되었으므로 주문 상태를 완료로 업데이트합니다..."
curl -X PUT "http://localhost:8080/api/orders/round/1/complete-if-posts-published" \
  -H "Content-Type: application/json"

echo -e "\n\n"

# 주문 진행률 업데이트 (게시 후 - 자동으로 100%가 됨)
echo "=== 4-1. 주문 진행률 확인 (게시 후) ==="
curl -X GET "http://localhost:8080/api/orders/round/1/info" \
  -H "Content-Type: application/json"

echo -e "\n\n"
#
## 전체 워크플로우 실행 (위 단계들을 한번에)
#echo "=== 전체 워크플로우 실행 (생성 → 승인 → 게시) ==="
#echo "Note: 위 단계들을 개별 실행하거나, 아래 명령어로 한번에 실행 가능"
#echo "curl -X POST \"http://localhost:8080/api/advertisement-posts/test/rounds/1/full-workflow\" \\"
#echo "  -H \"Content-Type: application/json\""

#echo -e "\n\n"
#
## 결과 확인
#echo "=== 5. 라운드의 모든 포스트 조회 ==="
#curl -X GET "http://localhost:8080/api/advertisement-posts/test/rounds/1/all-posts" \
#  -H "Content-Type: application/json"

#echo -e "\n\n"
#
## 모든 포스트 게시 완료 후 주문 상태 업데이트
#echo "=== 6. 포스트 게시 완료로 주문 상태 업데이트 ==="
#curl -X PUT "http://localhost:8080/api/orders/round/1/complete-if-posts-published" \
#  -H "Content-Type: application/json"


