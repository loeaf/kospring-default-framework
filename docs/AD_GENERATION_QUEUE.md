# 광고 생성 큐 시스템 문서

## 개요
라운드가 생성될 때 모든 활성 회원에 대해 AI 기반 광고를 자동으로 생성하는 큐 시스템입니다.

## 시스템 구조

### 1. 큐 플로우
```
라운드 생성 → 활성 회원 조회 → Redis 큐에 작업 추가 → Python AI 워커 → HTML 파일 생성
```

### 2. Redis 큐 이름
- `ad_generation_queue`: 메인 작업 큐
- `ad_generation_processing`: 처리 중인 작업 (옵션)
- `ad_generation_failed`: 실패한 작업 (옵션)

## 큐 메시지 구조

### AdTaskMessage
```json
{
  "taskId": 123,
  "roundId": 45,
  "memberId": 67,
  "roundInfo": {
    "id": 45,
    "title": "헬스케어 라운드 #1",
    "description": "헬스케어 분야 광고 라운드입니다.",
    "category": "헬스케어",
    "orderAmount": 100000000.00,
    "templateCost": 10000000.00,
    "aiGenerationCost": 20000000.00,
    "targetingPostingCost": 30000000.00,
    "serverRentalCost": 25000000.00,
    "otherCosts": 15000000.00,
    "startDate": "2024-01-01T09:00:00",
    "endDate": "2024-01-31T18:00:00",
    "maxParticipants": 50
  },
  "memberInfo": {
    "id": 67,
    "email": "company@example.com",
    "companyName": "테스트 회사",
    "businessRegistrationNumber": "123-45-67890",
    "contactNumber": "010-1234-5678"
  },
  "createdAt": "2024-01-01T08:00:00"
}
```

## Python Redis 클라이언트 예시

### 1. 큐에서 메시지 받기
```python
import redis
import json
from typing import Optional, Dict, Any

class AdGenerationWorker:
    def __init__(self, redis_host='localhost', redis_port=6379):
        self.redis_client = redis.Redis(host=redis_host, port=redis_port, decode_responses=True)
        self.queue_name = 'ad_generation_queue'
        
    def get_next_task(self) -> Optional[Dict[Any, Any]]:
        """큐에서 다음 작업을 가져옵니다."""
        try:
            # Redis LIST에서 오른쪽에서 팝 (FIFO)
            message = self.redis_client.brpop(self.queue_name, timeout=30)
            if message:
                queue_name, task_json = message
                return json.loads(task_json)
            return None
        except Exception as e:
            print(f"큐에서 메시지 가져오기 실패: {e}")
            return None
    
    def process_task(self, task: Dict[Any, Any]) -> bool:
        """작업을 처리하고 광고 HTML을 생성합니다."""
        try:
            task_id = task['taskId']
            round_info = task['roundInfo']
            member_info = task['memberInfo']
            
            # 작업 시작 상태 업데이트
            self.update_task_status(task_id, 'PROCESSING')
            
            # AI를 통한 광고 생성 (실제 AI 로직은 여기에 구현)
            ad_content = self.generate_ad_content(round_info, member_info)
            html_content = self.generate_html(ad_content, round_info, member_info)
            
            # HTML 파일 저장
            file_path = self.save_html_file(task_id, round_info['id'], member_info['id'], html_content)
            
            # 작업 완료 상태 업데이트
            self.update_task_status(task_id, 'COMPLETED', file_path)
            
            return True
            
        except Exception as e:
            print(f"작업 처리 실패: {e}")
            self.update_task_status(task_id, 'FAILED', error_message=str(e))
            return False
    
    def generate_ad_content(self, round_info: Dict, member_info: Dict) -> str:
        """AI를 사용하여 광고 콘텐츠를 생성합니다."""
        # 여기에 AI 광고 생성 로직 구현
        # 예: OpenAI API, 로컬 AI 모델 등
        
        prompt = f"""
        다음 정보를 바탕으로 광고 콘텐츠를 생성해주세요:
        
        라운드 정보:
        - 제목: {round_info['title']}
        - 설명: {round_info['description']}
        - 카테고리: {round_info['category']}
        - 발주금액: {round_info['orderAmount']}원
        
        회사 정보:
        - 회사명: {member_info['companyName']}
        - 업종: {round_info['category']}
        
        창의적이고 매력적인 광고 문구를 만들어주세요.
        """
        
        # AI API 호출 (실제 구현 필요)
        ad_content = "AI가 생성한 광고 콘텐츠 예시"
        return ad_content
    
    def generate_html(self, ad_content: str, round_info: Dict, member_info: Dict) -> str:
        """광고 콘텐츠를 HTML로 변환합니다."""
        html_template = f"""
        <!DOCTYPE html>
        <html lang="ko">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>{round_info['title']} - {member_info['companyName']}</title>
            <style>
                body {{ font-family: Arial, sans-serif; margin: 0; padding: 20px; }}
                .ad-container {{ max-width: 800px; margin: 0 auto; border: 1px solid #ddd; padding: 20px; }}
                .round-title {{ font-size: 24px; font-weight: bold; color: #333; }}
                .company-name {{ font-size: 18px; color: #666; margin: 10px 0; }}
                .ad-content {{ font-size: 16px; line-height: 1.6; margin: 20px 0; }}
                .order-amount {{ font-size: 20px; font-weight: bold; color: #e74c3c; }}
            </style>
        </head>
        <body>
            <div class="ad-container">
                <div class="round-title">{round_info['title']}</div>
                <div class="company-name">{member_info['companyName']}</div>
                <div class="ad-content">{ad_content}</div>
                <div class="order-amount">발주금액: {round_info['orderAmount']:,}원</div>
            </div>
        </body>
        </html>
        """
        return html_template
    
    def save_html_file(self, task_id: int, round_id: int, member_id: int, html_content: str) -> str:
        """HTML 파일을 저장하고 파일 경로를 반환합니다."""
        import os
        
        # 디렉토리 생성
        base_dir = "/path/to/ads"  # 실제 경로로 변경
        round_dir = os.path.join(base_dir, f"round_{round_id}")
        os.makedirs(round_dir, exist_ok=True)
        
        # 파일 저장
        filename = f"ad_member_{member_id}_task_{task_id}.html"
        file_path = os.path.join(round_dir, filename)
        
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(html_content)
        
        return file_path
    
    def update_task_status(self, task_id: int, status: str, html_file_path: str = None, error_message: str = None):
        """Spring Boot API를 통해 작업 상태를 업데이트합니다."""
        import requests
        
        url = f"http://localhost:8080/api/ad-tasks/{task_id}/status"
        data = {
            "status": status,
            "htmlFilePath": html_file_path,
            "errorMessage": error_message
        }
        
        try:
            response = requests.put(url, json=data)
            if response.status_code == 200:
                print(f"작업 {task_id} 상태 업데이트 성공: {status}")
            else:
                print(f"작업 상태 업데이트 실패: {response.status_code}")
        except Exception as e:
            print(f"API 호출 실패: {e}")

# 워커 실행 예시
if __name__ == "__main__":
    worker = AdGenerationWorker()
    
    while True:
        task = worker.get_next_task()
        if task:
            print(f"새로운 작업 처리 시작: Task ID {task['taskId']}")
            success = worker.process_task(task)
            if success:
                print(f"작업 완료: Task ID {task['taskId']}")
            else:
                print(f"작업 실패: Task ID {task['taskId']}")
        else:
            print("큐에 작업이 없습니다. 대기 중...")
```

### 2. 필요한 Python 패키지
```bash
pip install redis requests
pip install openai  # AI 모델 사용 시
```

## API 엔드포인트

### 1. 라운드별 작업 통계 조회
```
GET /api/ad-tasks/rounds/{roundId}/statistics
```

**응답 예시:**
```json
{
  "roundId": 45,
  "roundTitle": "헬스케어 라운드 #1",
  "statistics": {
    "PENDING": 5,
    "PROCESSING": 2,
    "COMPLETED": 10,
    "FAILED": 1,
    "RETRY": 0
  },
  "totalTasks": 18
}
```

### 2. 작업 상태 업데이트
```
PUT /api/ad-tasks/{taskId}/status
```

**요청 바디:**
```json
{
  "status": "COMPLETED",
  "htmlFilePath": "/path/to/ads/round_45/ad_member_67_task_123.html",
  "errorMessage": null
}
```

### 3. 실패한 작업 재시도
```
POST /api/ad-tasks/{taskId}/retry
```

**요청 바디:**
```json
{
  "errorMessage": "Network timeout - retrying"
}
```

## 작업 상태

- `PENDING`: 대기 중
- `PROCESSING`: 처리 중
- `COMPLETED`: 완료
- `FAILED`: 실패
- `RETRY`: 재시도 대기

## 주의사항

1. **재시도 횟수**: 최대 3회까지 자동 재시도
2. **타임아웃**: Redis 큐에서 30초 대기 후 타임아웃
3. **파일 저장**: HTML 파일은 라운드별 폴더에 저장
4. **오류 처리**: 모든 오류는 로그에 기록하고 상태 업데이트
5. **동시 처리**: 여러 워커를 실행하여 병렬 처리 가능