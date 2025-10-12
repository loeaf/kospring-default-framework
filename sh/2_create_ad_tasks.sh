#!/bin/bash

# 테스트용 ad_task 생성 스크립트
# member id : 17, 16, 19, 20, 21, 22, 23
# roundId : 32
echo "Creating test ad_task with your sample data..."

  curl -X POST http://localhost:8080/api/ad-tasks/test-data \
    -H "Content-Type: application/json" \
    -d '{
      "roundId": 33,
      "memberIds": [17, 16, 19, 20, 21, 22],
      "taskStatus": "COMPLETED",
      "adContent": "Sample HTML content for test ads",
      "adType": "interactive"
    }'

echo -e "\n"
echo "Test ad_task created successfully!"
echo "Now you can use adTaskId from the response in your create-order.sh script."