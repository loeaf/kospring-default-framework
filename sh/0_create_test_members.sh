#!/bin/bash

# 현재 디렉토리 확인
echo "Current directory: $(pwd)"

# 테스트용 더미 파일 생성
echo "Creating dummy files..."
echo "Business Registration Document" > business_registration.pdf
echo "Telecommunication Sales Report" > telecom_sales.pdf
echo "Advertising Registration Certificate" > advertising_registration.pdf

# 파일 존재 확인
if [ ! -f "business_registration.pdf" ]; then
    echo "Error: business_registration.pdf not found"
    exit 1
fi

if [ ! -f "telecom_sales.pdf" ]; then
    echo "Error: telecom_sales.pdf not found"
    exit 1
fi

if [ ! -f "advertising_registration.pdf" ]; then
    echo "Error: advertising_registration.pdf not found"
    exit 1
fi

# 멤버 1: 테크이노베이션
echo "Creating member 1: 테크이노베이션..."
curl -v -X POST http://localhost:8080/api/members/complete-registration \
  -H "Content-Type: multipart/form-data" \
  -F "email=contact@techinnovation.co.kr" \
  -F "password=password123" \
  -F "companyName=테크이노베이션" \
  -F "businessRegistrationNumber=1443-45-67890" \
  -F "contactNumber=02-1234-5678" \
  -F "businessField=IT 기술혁신" \
  -F "productDescription=혁신적인 IT 솔루션 및 기술 컨설팅" \
  -F "companyDescription=최신 기술로 혁신을 주도하는 IT 전문기업" \
  -F "businessRegistrationFile=@business_registration.pdf" \
  -F "telecommunicationSalesFile=@telecom_sales.pdf" \
  -F "advertisingRegistrationFile=@advertising_registration.pdf" \
  -F "rentalContractAgreed=true" \
  -F "serviceContractAgreed=true" \
  -F "marketingAgreed=true" \
  -F "durationYears=1"

echo -e "\n"

# 멤버 2: 크리에이티브스튜디오
echo "Creating member 2: 크리에이티브스튜디오..."
curl -X POST http://localhost:8080/api/members/complete-registration \
  -F "email=hello@creativestudio.kr" \
  -F "password=password123" \
  -F "companyName=크리에이티브스튜디오" \
  -F "businessRegistrationNumber=234-56-78901" \
  -F "contactNumber=02-2345-6789" \
  -F "businessField=크리에이티브 디자인" \
  -F "productDescription=브랜딩, 웹디자인, 광고 크리에이티브 제작" \
  -F "companyDescription=창의적인 디자인으로 브랜드 가치를 높이는 스튜디오" \
  -F "businessRegistrationFile=@./business_registration.pdf" \
  -F "telecommunicationSalesFile=@./telecom_sales.pdf" \
  -F "advertisingRegistrationFile=@./advertising_registration.pdf" \
  -F "rentalContractAgreed=true" \
  -F "serviceContractAgreed=true" \
  -F "marketingAgreed=false" \
  -F "durationYears=1"

echo -e "\n"

# 멤버 3: 디지털마케팅솔루션
echo "Creating member 3: 디지털마케팅솔루션..."
curl -X POST http://localhost:8080/api/members/complete-registration \
  -F "email=info@digitalmarketing.com" \
  -F "password=password123" \
  -F "companyName=디지털마케팅솔루션" \
  -F "businessRegistrationNumber=345-67-89012" \
  -F "contactNumber=02-3456-7890" \
  -F "businessField=디지털 마케팅" \
  -F "productDescription=온라인 마케팅, SNS 관리, 퍼포먼스 마케팅" \
  -F "companyDescription=데이터 기반 디지털 마케팅 전문 에이전시" \
  -F "businessRegistrationFile=@./business_registration.pdf" \
  -F "telecommunicationSalesFile=@./telecom_sales.pdf" \
  -F "advertisingRegistrationFile=@./advertising_registration.pdf" \
  -F "rentalContractAgreed=true" \
  -F "serviceContractAgreed=true" \
  -F "marketingAgreed=true" \
  -F "durationYears=2"

echo -e "\n"

# 멤버 4: 스마트솔루션
echo "Creating member 4: 스마트솔루션..."
curl -X POST http://localhost:8080/api/members/complete-registration \
  -F "email=team@smartsolutions.co.kr" \
  -F "password=password123" \
  -F "companyName=스마트솔루션" \
  -F "businessRegistrationNumber=456-78-90123" \
  -F "contactNumber=02-4567-8901" \
  -F "businessField=스마트 기술" \
  -F "productDescription=IoT, 스마트홈, 자동화 솔루션 개발" \
  -F "companyDescription=스마트 기술로 편리한 생활을 만드는 기업" \
  -F "businessRegistrationFile=@./business_registration.pdf" \
  -F "telecommunicationSalesFile=@./telecom_sales.pdf" \
  -F "advertisingRegistrationFile=@./advertising_registration.pdf" \
  -F "rentalContractAgreed=true" \
  -F "serviceContractAgreed=true" \
  -F "marketingAgreed=false" \
  -F "durationYears=1"

echo -e "\n"

# 멤버 5: 퓨처테크
echo "Creating member 5: 퓨처테크..."
curl -X POST http://localhost:8080/api/members/complete-registration \
  -F "email=contact@futuretech.kr" \
  -F "password=password123" \
  -F "companyName=퓨처테크" \
  -F "businessRegistrationNumber=567-89-01234" \
  -F "contactNumber=02-5678-9012" \
  -F "businessField=소프트웨어 개발" \
  -F "productDescription=AI 기반 솔루션 개발 및 제공" \
  -F "companyDescription=미래 기술을 선도하는 혁신 기업" \
  -F "businessRegistrationFile=@./business_registration.pdf" \
  -F "telecommunicationSalesFile=@./telecom_sales.pdf" \
  -F "advertisingRegistrationFile=@./advertising_registration.pdf" \
  -F "rentalContractAgreed=true" \
  -F "serviceContractAgreed=true" \
  -F "marketingAgreed=true" \
  -F "durationYears=3"

echo -e "\n"

# 멤버 6: 테스트주식회사 (CNC Test)
echo "Creating member 6: 테스트주식회사..."
#curl -X POST http://localhost:8080/api/members/complete-registration \
#  -F "email=test@cnc.com" \
#  -F "password=test123!@#" \
#  -F "companyName=테스트주식회사" \
#  -F "businessRegistrationNumber=123-45-67890" \
#  -F "contactNumber=02-1234-5678" \
#  -F "businessRegistrationFile=@./business_registration.pdf" \
#  -F "telecommunicationSalesFile=@./telecom_sales.pdf" \
#  -F "advertisingRegistrationFile=@./advertising_registration.pdf" \
#  -F "rentalContractAgreed=true" \
#  -F "serviceContractAgreed=true" \
#  -F "marketingAgreed=true" \
#  -F "durationYears=1"

echo -e "\n"
echo "All members created successfully!"


echo -e "\n"

# 더미 파일 정리
rm business_registration.pdf telecom_sales.pdf advertising_registration.pdf