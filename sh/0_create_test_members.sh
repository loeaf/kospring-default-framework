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

# 멤버 1: 도시와농촌
echo "Creating member 1: 도시와농촌..."
curl -v -X POST http://localhost:8080/api/members/complete-registration \
  -H "Content-Type: multipart/form-data" \
  -F "email=cnc@dosiwa.com" \
  -F "password=password123" \
  -F "companyName=도시와농촌" \
  -F "businessRegistrationNumber=123-45-67855" \
  -F "contactNumber=044-417-6661" \
  -F "businessField=공간정보(GIS) 플랫폼 구축 및 DB 구축, 시스템 통합(SI) - 공간정보서비스·공공행정서비스·GIS기반정보서비스, 연구개발(R&D) - 스마트팜·드론·IoT·디지털트윈 기술연구, 도시계획 및 토목(URBAN&CIVIL) - 도시개발·인프라 설계·감독·품질관리, 3D 모델링 전문(지상/지하 LoD2+), 측량 및 현장조사" \
  -F "productDescription=[GIS 사업] 5cm급 드론 촬영 기반 지상 3D 모델링(LoD2+), 지하배관·시설물 3D 모델링, GIS DB 구축 및 데이터 표준화 / [SI 사업] 국토지리정보원 지구변화 모니터링 시스템, 기상청 기상기후 디지털트윈 통합 플랫폼, 스마트 항로표지 시스템, 학군교 선발 시스템 개발 / [R&D] 다카(Dhaka) 지역 디지털트윈 3D 구현, 공유재산 관리 모바일 앱, 1D-2D 하이브리드 도시 침수 시뮬레이션 모델, 현장조사 디지털화 앱 / [토목] 측량·현장조사를 통한 정밀 공간정보 수집 및 도시개발 프로젝트 기초작업 지원" \
  -F "companyDescription=4차산업혁명 시대를 선도하는 공간정보 종합 솔루션 기업으로, GIS·SI·R&D·URBAN&CIVIL 4개 사업부문을 운영하며 공간정보의 융복합 서비스를 제공합니다. 국토지리정보원, 기상청, 해양수산부, 육군 등 주요 공공기관의 대형 프로젝트 수행 실적을 보유하고 있으며, 디지털트윈 기술을 활용한 스마트시티 가상화 모델 구축 전문성을 인정받고 있습니다. 5cm급 고해상도 드론 촬영 데이터 기반 LoD2 수준 이상의 정밀 3D 모델링 기술을 보유하고 있으며, 지상 건물뿐만 아니라 맨홀·밸브 등 지하배관 및 시설물까지 입체적으로 디지털화합니다. 충남 서천군에 본사(마서면 장서로 698)를, 세종시에 지사(한누리대로 2149, 611호)를 두고 있으며, 최신 측량 기술과 현장조사 노하우를 바탕으로 신뢰성 높은 공간정보를 제공합니다. 지속적인 R&D 투자를 통해 스마트팜, 드론, IoT, 도시 침수 예측 모델 등 첨단 기술 경쟁력을 확보하고 있으며, 도시의 발전과 지속가능성을 목표로 도시계획부터 인프라 설계·감독·품질관리까지 토털 솔루션을 제공하는 공간정보 전문 기업입니다." \
  -F "businessRegistrationFile=@business_registration.pdf" \
  -F "telecommunicationSalesFile=@telecom_sales.pdf" \
  -F "advertisingRegistrationFile=@advertising_registration.pdf" \
  -F "rentalContractAgreed=true" \
  -F "serviceContractAgreed=true" \
  -F "marketingAgreed=false" \
  -F "pricingPreference=highest" \
  -F "durationYears=1" \
  -F "bankCode=088" \
  -F "bankName=신한은행" \
  -F "accountNumber=110-234-567890" \
  -F "accountHolder=도시와농촌"

echo -e "\n"

# 멤버 2: 산돌
echo "Creating member 2: 산돌..."
curl -X POST http://localhost:8080/api/members/complete-registration \
  -F "email=contact@sandoll.co.kr" \
  -F "password=password123" \
  -F "companyName=주식회사 산돌" \
  -F "businessRegistrationNumber=234-56-78901" \
  -F "contactNumber=02-2038-3600" \
  -F "businessField=폰트 플랫폼 서비스, 폰트 제작 및 판매, 크리에이터 콘텐츠 플랫폼 운영, 기업 전용 폰트 개발, 클라우드 폰트 서비스(산돌구름), 웹폰트 서비스, 폰트 이미지 검색 AI 기술(폰트폰트), 스톡 콘텐츠 플랫폼(유토이미지), 한글 디자인 문구류·이모티콘 제작(산돌티움), 타입 디자인 컨설팅, 폰트 라이선스 관리 솔루션" \
  -F "productDescription=[산돌구름 클라우드 폰트 플랫폼] 국내외 25개 폰트 회사 19,200여 종 폰트 제공, 암호형 클라우드 기술 적용으로 별도 파일 설치 없이 사용 가능, 모든 사용범위 제한 폐지(인쇄/출판/영상/웹/앱/굿즈 등 자유 사용), 폰트 이미지 검색 AI 서비스, 무료폰트 최대 규모 제공, 브랜드상품·기획상품·낱개상품·독립디자이너 폰트 서비스 운영 / [기업 전용 폰트 개발 실적] 마이크로소프트 '맑은 고딕' (전국민 사용), Apple iOS 시스템 폰트 'Sandoll 고딕Neo1', 삼성전자 '삼성체', 현대카드 전용폰트, 네이버 '나눔고딕·나눔명조·나눔스퀘어', KT '올레체neo', 토스 '토스 프로덕트 산스', 배달의민족, 여기어때 '잘난체 고딕' 등 글로벌·국내 대기업 전용 서체 다수 개발 / [폰트폰트 앱] AI 기반 폰트 이미지 검색 모바일 애플리케이션, URL 또는 이미지 업로드로 22,000여 종 폰트 매칭, 한글·영문·일본어·중국어·태국어 등 다국어 지원 / [수상실적] 2014 레드닷 어워드 타이포그래피 부문, GRANSHAN 2015·2016 한글부문 1위, FT 아시아 태평양 고성장 기업 3년 연속 선정(2023-2025)" \
  -F "companyDescription=1984년 설립된 대한민국 1호 폰트 회사이자 국내 대표 크리에이터 콘텐츠 플랫폼 기업입니다. 41년간 축적된 타입 디자인 전문성을 바탕으로 마이크로소프트, 애플, 삼성전자 등 글로벌 IT 기업부터 현대카드, 토스, 배달의민족, 여기어때 등 국내 주요 기업의 전용 폰트를 개발해왔으며, 전국민이 사용하는 '맑은 고딕'과 애플 iOS 시스템 폰트를 제작한 기술력을 보유하고 있습니다. 2014년 업계 최초로 클라우드 폰트 서비스 '산돌구름'을 런칭하여 CD 패키지 판매 중심이던 폰트 유통 방식을 완전히 혁신했으며, 현재 156만 명의 사용자가 이용하는 국내 1위 폰트 플랫폼으로 자리잡았습니다. 산돌구름은 산돌을 포함한 모노타입, 모리사와, 폰트웍스 등 국내외 25개 폰트 회사의 19,200여 종 폰트를 서비스하며, 2020년 업계 최초로 인쇄·출판·영상 등 모든 폰트 사용범위 구분을 폐지하고 법적 소송을 철폐하는 '사용범위통합 캠페인'을 전개했습니다. 또한 2016년부터 전국 초중고교를 대상으로 무상 폰트를 지원하는 '폰트안심학교' 사회공헌 캠페인을 운영하며 한글문화 발전에 기여하고 있습니다. 2022년 코스닥에 상장하였고, 2024년 국내 양대 폰트 기업인 윤디자인을 인수하며 시장 지배력을 확대했습니다. 서울 성동구 성수동(아차산로17길 49, 생각공장 데시앙플렉스 6층)에 본사를 두고 있으며, 계열사로 윤디자인, 산돌티움(한글 디자인 문구), 산돌스퀘어(기술 개발), 비비트리(스톡 콘텐츠 플랫폼 유토이미지) 등을 운영하고 있습니다. 2025년 영국 파이낸셜 타임즈 선정 아시아 태평양 고성장 기업에 3년 연속 선정되며 글로벌 타입 디자인 기업으로 성장하고 있습니다." \
  -F "businessRegistrationFile=@business_registration.pdf" \
  -F "telecommunicationSalesFile=@telecom_sales.pdf" \
  -F "advertisingRegistrationFile=@advertising_registration.pdf" \
  -F "rentalContractAgreed=true" \
  -F "serviceContractAgreed=true" \
  -F "marketingAgreed=true" \
  -F "pricingPreference=highest" \
  -F "durationYears=1" \
  -F "bankCode=004" \
  -F "bankName=KB국민은행" \
  -F "accountNumber=123-456-789012" \
  -F "accountHolder=주식회사 산돌"

echo -e "\n"

# 멤버 3: 바이브컴퍼니
echo "Creating member 3: 바이브컴퍼니..."
curl -X POST http://localhost:8080/api/members/complete-registration \
  -F "email=info@vaiv.kr" \
  -F "password=password123" \
  -F "companyName=주식회사 바이브컴퍼니" \
  -F "businessRegistrationNumber=345-67-89012" \
  -F "contactNumber=02-6204-6100" \
  -F "businessField=빅데이터 수집·분석 플랫폼, 인공지능(AI) 솔루션 개발, 자연어처리(NLP) 기술, 소셜 빅데이터 분석(트위터·인스타그램·블로그·뉴스·커뮤니티·유튜브), 초거대 AI 언어모델(LLM) 개발, 뉴럴서치 기술, AI 챗봇 솔루션, 텍스트마이닝, 로보저널리즘, 지식관리시스템(KMS), 디지털 트윈 플랫폼 구축, AI 기반 금융정보 서비스, 빅데이터 연구 및 인사이트 도출" \
  -F "productDescription=[VAIV Solution] VAIV Search(최신 뉴럴서치+언어생성 기술 결합 AI 검색, 출처 제공), VAIV Chatbot(혼합 주도형 쌍방향 대화 AI 챗봇), VAIV Q&A(문서 자동 질의응답셋 생성), VAIV Summary(텍스트 핵심 내용 자동 요약), VAIV KMS Smart Helper(상담지식관리 솔루션), VAIV Report(AI 자동 보고서 작성 - 데이터 수집·검색·분석·시각화·설명 전과정 자동화), VAIV TA(텍스트 애널리시스 - 대량 문서 인사이트 발굴 및 이슈 모니터링) / [VAIV Service] Sometrend(썸트렌드, 구 소셜메트릭스 - 트위터·블로그·인스타그램·뉴스·커뮤니티·유튜브 빅데이터 분석, 썸트렌드 클라우드·데이터플러스·어스 세부 서비스), VAIV Stock(AI 기반 금융정보 - 종목뉴스·발굴·분석, 섹터·테마·공시 분석), VAIV 생활변화관측소(빅데이터 연구원의 주간·월간 생활변화 데이터 관측 및 인사이트 제공) / [자체 AI 기술] VAIVGeM(자체 초거대 언어모델 LLM, 2023년 출시) / [공공기관 프로젝트] LH 디지털 트윈 플랫폼 구축, 해양수산부 해양 공간 디지털 트윈 사업, 식약처 데이터 융합 및 분석 플랫폼 구축, 디지털플랫폼정부 민간의 초거대 AI 활용지원 사업 공급기업 선정" \
  -F "companyDescription=2000년 설립된 대한민국 최초이자 최대 빅데이터 전문 기업으로, 다음커뮤니케이션(현 카카오)의 사내 인큐베이팅으로 출발하여 '(주)다음소프트'로 독립 법인을 설립했으며, 2020년 '(주)바이브컴퍼니'로 사명을 변경하고 코스닥에 상장했습니다. 설립 첫 해인 2000년에 NLP(자연어처리) 연구소를 설립하여 25년간 축적된 AI 및 빅데이터 기술력을 보유하고 있으며, 194명의 전문 인력(2024년 기준)이 정형 데이터뿐 아니라 소셜 빅데이터(인스타그램, 트위터, 블로그, 뉴스, 커뮤니티, 유튜브 등) 비정형 데이터까지 수집·분석하여 기업·기관·개인 고객사에 최적의 맞춤형 솔루션을 제공합니다. 2023년 자체 초거대 언어모델 'VAIVGeM'과 AI 검색 솔루션 'VAIV Search'를 출시하며 생성형 AI 시장에 본격 진출했고, 지능정보산업협회 선정 'Emerging AI+X TOP100' 기업에 3년 연속 선정되었습니다. 대표 서비스인 '썸트렌드(Sometrend, 구 소셜메트릭스)'는 국내 최대 규모의 빅데이터 분석 플랫폼으로, 기업 맞춤형 클라우드 서비스와 데이터 API를 제공하며 마케팅·PR·여론분석·트렌드 예측 등 다양한 분야에서 활용되고 있습니다. 또한 LH 디지털 트윈 플랫폼, 해양수산부 해양 공간 디지털 트윈, 식약처 데이터 융합 플랫폼 등 주요 공공기관의 대형 AI·빅데이터 프로젝트를 수행하며 공공 부문에서의 기술력을 인정받고 있습니다. 서울 용산구 한남동에 본사를, 세종시 집현동에 세종 지사를 운영하며, 2022년 고용노동부 청년친화강소기업(임금·일생활균형 우수), 2018년 대한상공회의소 '일하기 좋은 중소기업'에 선정되는 등 우수한 근무환경을 갖춘 기술 기업입니다. 2005년 AI 기반 미니홈피 '아우닷컴' 출시 이력을 보유한 국내 AI 선구자 기업으로, 자연어처리·텍스트마이닝·로보저널리즘·챗봇·검색엔진·지식관리 등 AI 전 영역에 걸친 토털 솔루션을 제공하는 빅데이터·AI 종합 기술 기업입니다." \
  -F "businessRegistrationFile=@business_registration.pdf" \
  -F "telecommunicationSalesFile=@telecom_sales.pdf" \
  -F "advertisingRegistrationFile=@advertising_registration.pdf" \
  -F "rentalContractAgreed=true" \
  -F "serviceContractAgreed=true" \
  -F "marketingAgreed=true" \
  -F "pricingPreference=highest" \
  -F "durationYears=1" \
  -F "bankCode=081" \
  -F "bankName=하나은행" \
  -F "accountNumber=345-678-901234" \
  -F "accountHolder=주식회사 바이브컴퍼니"

echo -e "\n"

# 멤버 4: 지오공감테크
echo "Creating member 4: 지오공감테크..."
curl -X POST http://localhost:8080/api/members/complete-registration \
  -F "email=info@geomind.co.kr" \
  -F "password=password123" \
  -F "companyName=주식회사 지오공감테크" \
  -F "businessRegistrationNumber=456-78-90123" \
  -F "contactNumber=070-4866-2597" \
  -F "businessField=GIS(Geographic Information System) 전문 SW 개발, 디지털트윈 플랫폼 구축, 3D 공간정보 시각화, 통합관제플랫폼 개발, 스마트시티 솔루션, 국토 관리 시스템, 환경 모니터링 시스템, 빅데이터 기반 시설물 관리, 드론 촬영 영상 처리, Cesium 엔진 기반 3차원 모델링(LoD2+), 센서 데이터 연계 실시간 모니터링, 웹 기반 공간정보 플랫폼, 교육정보 시스템, 철도시설 종합정보 시스템, 지구변화 모니터링 시스템" \
  -F "productDescription=[GIS & Digital Twin] GIS와 디지털트윈 기술 융합을 통한 스마트시티·국토관리·환경모니터링 최적화 솔루션, 공간정보 기반 실시간 의사결정 지원 / [3D 공간정보 시각화] 드론 촬영 영상과 Cesium 엔진을 활용한 LoD2 이상 3차원 모델 구축, 웹 기반 효율적 3D 시각화 기술, 고해상도 공간정보 렌더링 / [통합관제플랫폼] 디지털트윈 기반 통합관제 시스템 구축, IoT 센서 데이터 연계 실시간 모니터링, 현장 대응 및 의사결정 지원, 이벤트 알람 및 대시보드 제공 / [주요 수행 실적] 학생군사학교 교육정보종합시스템(실시간 교육 지원 기반 조성), 철도시설 종합정보 시스템(빅데이터+GIS 기반 시설물 이력·상태 체계 관리, 사용자 친화적 웹 시스템), 지구변화 모니터링 시스템(우주측지·지구물리 데이터 융합, 지구변화·지각변동 과학적 분석), 건설기술 연구원 '물 수요공급 생태통합 물 배분 의사결정지원 시스템' 개발(2025년 수주) / [기술 특징] 공간정보 기반 빅데이터 분석, 시설물 이력 및 상태 관리, 우주측지·지구물리 데이터 처리, 웹 기반 사용자 친화적 인터페이스, 드론 영상 기반 정밀 3D 모델링" \
  -F "companyDescription=2024년 2월 설립된 GIS 및 전문 SW 개발 전문 기업으로, 공간정보 기반의 전문 프로젝트를 수행하며 빠르게 성장하고 있습니다. '최고의 기술', '최상의 품질', '최적의 서비스'를 목표로 늘 연구하고 고민하는 건전하고 알찬 기업을 지향하며, 설립 1년여 만에 연구개발전담부서 인정(2024년 6월)과 소기업(소상공인) 인정(2024년 4월)을 받았습니다. GIS와 디지털트윈 기술을 융합하여 스마트시티, 국토 관리, 환경 모니터링 등 다양한 분야에 최적의 솔루션을 제공하고 있으며, 드론 촬영 영상과 Cesium 엔진을 활용한 LoD2 이상의 3차원 모델 구축 및 웹 기반 시각화 기술을 보유하고 있습니다. 주요 실적으로는 학생군사학교 교육정보종합시스템의 안정적 운영 지원, 철도시설 종합정보 시스템 구축(빅데이터+GIS 기반 시설물 이력·상태 관리), 지구변화 모니터링 시스템(우주측지·지구물리 데이터 융합) 개발 등이 있으며, 2025년 5월에는 건설기술 연구원의 '물 수요공급 생태통합 물 배분 의사결정지원 시스템' 개발 프로젝트를 수주하며 공공기관 프로젝트 수행 역량을 입증했습니다. 디지털트윈 기반의 통합관제플랫폼을 구축하고 IoT 센서 데이터와 연계하여 실시간 모니터링 및 현장 대응을 지원하는 기술력을 보유하고 있으며, 공간정보 빅데이터 분석, 시설물 관리, 환경·기상 모니터링 등 공간정보가 활용되는 모든 분야에서 사용자 친화적인 웹 시스템을 제공합니다. 대전광역시 유성구 테크노2로 187, 230호에 본사를 두고 있으며, 신생 기업임에도 불구하고 공공기관 및 연구기관 프로젝트를 연이어 수주하며 GIS·디지털트윈 분야에서 빠르게 기술력을 인정받고 있는 성장형 공간정보 전문 기업입니다." \
  -F "businessRegistrationFile=@business_registration.pdf" \
  -F "telecommunicationSalesFile=@telecom_sales.pdf" \
  -F "advertisingRegistrationFile=@advertising_registration.pdf" \
  -F "rentalContractAgreed=true" \
  -F "serviceContractAgreed=true" \
  -F "marketingAgreed=false" \
  -F "pricingPreference=highest" \
  -F "durationYears=1" \
  -F "bankCode=020" \
  -F "bankName=우리은행" \
  -F "accountNumber=567-890-123456" \
  -F "accountHolder=주식회사 지오공감테크"

echo -e "\n"

# 멤버 5: 한컴인스페이스
echo "Creating member 5: 한컴인스페이스..."
curl -X POST http://localhost:8080/api/members/complete-registration \
  -F "email=sales@inspace.re.kr" \
  -F "password=password123" \
  -F "companyName=주식회사 한컴인스페이스" \
  -F "businessRegistrationNumber=567-89-01234" \
  -F "contactNumber=042-862-2735" \
  -F "businessField=상용 지구관측위성 개발·운영, 위성/드론 영상 분석 솔루션, AI 기반 이미지 분석 플랫폼, 무인 드론 개발 및 자동 운영 시스템, 위성 지상국 기술, 웨어러블 로봇 개발, 원격탐사 데이터 처리, 3D 지형 분석 및 측량, 통합 감시 솔루션, 딥러닝 모델 데이터셋 구축, 변화 탐지 시스템(도로·건물), 불법 구조물 탐지, 소나무 고사목 탐지, 해상 기름 유출 감시(SAR), 선박 탐지, 화재 모니터링, 농업 원격 탐사 서비스" \
  -F "productDescription=[Sejong 상용위성 시리즈] Sejong-1(한국 최초 상용 지구관측위성, 2022년 5월 발사, GSD 5M, 7밴드 VNIR, Red Edge 밴드 탑재로 작물·산림 식생지수 분석 최적화), Sejong-2(해양·농업 모니터링 및 도시 변화 탐지 특화, 8밴드 멀티스펙트럴, 2025년 6월 발사 예정), Sejong-3(산불 피해 분석·대기오염·하천 수질 평가 특화, 442밴드 하이퍼스펙트럴, 2025년 10월 발사), Sejong-4(국산 OBC 탑재 실시간 운영체제 NEOS+FS, 2025년 11월 누리호 발사), Sejong-5(공공분야 하이퍼스펙트럴 이미징, 2026년 6월 발사) / [영상 분석 솔루션] InExplorer(영상 카탈로깅 솔루션, GS 1등급 인증, 위성·항공 영상 고속 카탈로깅 및 메타데이터 관리), InViewer(영상 처리 및 시각화, GS 1등급, 위성·항공 영상 밴드 병합·향상·처리), InSight(AI 기반 영상 분석, GS 1등급, EO·IR·SAR 영상 분석·보고서 생성·좌표 추출), InMotionImage(비디오·스트리밍 객체 탐지, GS 1등급, 플러그인 기반 AI 모델 관리, 드론·위성 실시간 모니터링) / [AI Profiler] 이미지 분석 AI 플랫폼(데이터셋 관리·어노테이션, AI 모델 학습·평가·예측, 객체 탐지·세그먼테이션, KOMPSAT·PlanetScope·LANDSAT-8·항공사진 지원, 클라우드 기반 프로젝트 호스팅) / [드론 시스템] HD-300(군용 소형 고성능, 1.5kg, 25분 비행, EO 카메라, KCMVP 인증), HD-550(민·군·정찰용, 4.95kg, 30분 비행, EO/IR 듀얼 카메라, 10배 광학 줌, IP43), HD-850(민·군·정찰용, 10kg, 30분 비행, EO/IR 듀얼, IP43), SPIRIT(극한환경 전천후 무인기, 6.1kg, 32분 비행, IP56, 18m/s 내풍) / [DroneSAT] 완전 자동 무인 드론 운영 솔루션(자동 이착륙, 무선 충전, 광역 주기적 모니터링, 실시간 영상 분석 및 객체 탐지, 3D 지형 분석 및 체적·면적·고도 측량, DEM/DSM 생성) / [InView] AI 기반 다목적 통합 감시 솔루션(지상·이동 카메라+DroneSAT+Sejong위성 결합, 중장거리 모니터링, 이상 탐지 시 즉각 대응) / [웨어러블 로봇] LEXO-W, LEXO-V 시리즈 / [AI 분석 기술] 드론·위성 영상 기반 목표물 탐지(차량·굴삭기·항공기·군함), 변화 탐지(도로·건물), 불법 구조물 탐지, 소나무 고사목 탐지, 논 재배면적 자동 추출 및 수확량 예측, 해상 기름 유출 감지·확산 모델(SAR), 선박 탐지(SAR), 화재·연기·온도 탐지, 딥러닝 데이터셋 구축(논·건물·도로 라벨링)" \
  -F "companyDescription=2012년 설립된 위성·드론·AI 융합 기술 전문 기업으로, 2022년 5월 한국 최초 상용 지구관측위성 'Sejong-1'을 발사하며 민간 우주 기업으로서의 입지를 확립했습니다. Sejong-1은 Red Edge 밴드를 탑재하여 작물 및 산림의 식생지수 분석에 최적화되어 있으며, 2025-2026년에 걸쳐 Sejong-2(해양·농업), Sejong-3(하이퍼스펙트럴 442밴드), Sejong-4(국산 OBC 탑재, 누리호 발사), Sejong-5(공공 부문)를 연이어 발사하여 총 5기의 상용위성 군집(Constellation)을 구축할 예정입니다. 위성 지상국 기술을 기반으로 위성 영상 수신·처리부터 AI 기반 분석까지 End-to-End 솔루션을 제공하며, GS 1등급 인증을 받은 InExplorer, InViewer, InSight, InMotionImage 등 4개의 전문 영상 분석 솔루션을 보유하고 있습니다. 드론 분야에서는 군용 소형 드론(HD-300)부터 극한환경 전천후 무인기(SPIRIT, IP56)까지 자체 개발한 4종의 드론 라인업을 갖추고 있으며, 자동 이착륙·무선 충전·실시간 영상 분석이 가능한 'DroneSAT' 완전 자동 무인 드론 운영 솔루션으로 광역 모니터링 서비스를 제공합니다. AI 기반 이미지 분석 플랫폼 'AI Profiler'는 KOMPSAT, PlanetScope, LANDSAT-8 등 다양한 위성 영상에 대한 데이터셋 관리, 어노테이션, AI 모델 학습·평가·예측을 클라우드 기반으로 제공하며, 객체 탐지 및 세그먼테이션 기능을 지원합니다. 주요 AI 분석 기술로는 도로·건물 변화 탐지, 불법 구조물 탐지, 소나무 고사목 탐지, 논 재배면적 자동 분류 및 수확량 예측, SAR 영상 기반 해상 기름 유출 감지·선박 탐지, 화재·연기·온도 탐지 시스템 등이 있으며, 딥러닝 모델 데이터셋 구축 전문성을 보유하고 있습니다. 지상·이동 카메라, DroneSAT, Sejong위성을 결합한 'InView' AI 기반 다목적 통합 감시 솔루션은 중장거리 모니터링과 이상 탐지 시 즉각 대응이 가능한 통합 플랫폼입니다. 또한 웨어러블 로봇(LEXO 시리즈) 개발을 통해 우주·국방·산업 분야로 사업 영역을 확장하고 있습니다. 대전광역시 유성구 엑스포로 1, 12층에 본사를 두고 있으며, 위성·드론·AI 기술을 융합한 원격탐사 데이터 처리 및 3D 지형 분석(체적·면적·고도 측량, DEM/DSM 생성) 전문성을 바탕으로 국방·환경·농업·해양·도시 관리 등 다양한 분야에 고부가가치 맞춤형 서비스를 제공하는 우주·AI 융합 기술 선도 기업입니다." \
  -F "businessRegistrationFile=@business_registration.pdf" \
  -F "telecommunicationSalesFile=@telecom_sales.pdf" \
  -F "advertisingRegistrationFile=@advertising_registration.pdf" \
  -F "rentalContractAgreed=true" \
  -F "serviceContractAgreed=true" \
  -F "marketingAgreed=true" \
  -F "pricingPreference=highest" \
  -F "durationYears=1" \
  -F "bankCode=003" \
  -F "bankName=기업은행" \
  -F "accountNumber=789-012-345678" \
  -F "accountHolder=주식회사 한컴인스페이스"

echo -e "\n"
echo "All members created successfully!"

echo -e "\n"

# 더미 파일 정리
rm business_registration.pdf telecom_sales.pdf advertising_registration.pdf