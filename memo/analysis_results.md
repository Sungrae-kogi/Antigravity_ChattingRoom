SKThemeChattingRoom 프로젝트 분석 결과
1. 프로젝트 개요
이 프로젝트는 Spring Boot와 WebSocket을 활용하여 구축된 실시간 익명 웹 채팅 서비스입니다. 단일 채팅방에서 여러 사용자가 실시간으로 대화를 나눌 수 있으며, 대규모 트래픽과 실시간성을 보장하기 위해 여러 성능 최적화 기법이 돋보이는 프로젝트입니다.

2. 사용된 프레임워크 및 기술 스택
Backend Core: Java 17, Spring Boot 3.5.11
Communication Layer: Spring WebSocket (실시간 통신), Spring WebFlux / WebClient (비동기 HTTP API 통신)
Data & Persistence: MyBatis, MariaDB(관계형 DB), Spring Data Redis(캐싱/버퍼링)
Frontend: JSP, JSTL, HTML/CSS, Vanilla JavaScript (DOM 최적화 및 IntersectionObserver 활용)
Test & DevOps: JUnit5, Testcontainers, Docker, GitHub Actions CI
3. 핵심 아키텍처 및 동작 방식
가. 실시간 통신과 미디어 처리 분리 (Two-Track Architecture)
텍스트 채팅: 가벼운 데이터는 WebSocket 채널을 통해 지연 없는 실시간 브로드캐스팅(1:N)을 수행합니다.
이미지 업로드: 무거운 이미지 파일 전송 시 WebSocket망에 부하를 주지 않기 위해, 일반 HTTP(Multipart) 엔드포인트로 파일을 업로드하여 서버 외부 경로에 저장합니다. 이후 프론트엔드에서는 이미지 주소(URL)만 넘겨받아 WebSocket으로 전파합니다.
나. 대규모 트래픽 대비 (Redis Write-Back)
초당 수천 건의 채팅이 발생할 경우 DB 커넥션 병목을 막기 위해 **Redis를 완충 지대(Buffer)**로 활용합니다.
채팅이 입력되면 일단 0.001초 만에 Redis List에 임시 적재하고, 스프링 스케줄러(@Scheduled)가 10초 주기로 데이터를 긁어모아 MariaDB에 다중 삽입(Bulk Insert) 하는 방식으로 RDBMS의 부하를 극적으로 낮췄습니다.
다. 데이터 유실 제로망 (Graceful Shutdown)
버퍼링 구조(Write-Back)의 가장 큰 약점인 '메모리 데이터 유실'을 막기 위해, 스프링 컨테이너 소멸 직전에 발동하는 @PreDestroy 센서를 부착했습니다. 서버가 종료되거나 재배포될 때 스케줄러를 기다리지 않고 강제로 잔여 채팅을 DB로 이관합니다.
라. 프론트엔드 최적화 (No-Offset Paging)
무한 스크롤(현대 메신저 라이크)로 과거 대화 내역을 조회할 때 성능이 느린 OFFSET 방식 대신 lastId 값 기반의 No-Offset 쿼리를 사용합니다.
<template> 태그를 활용한 DOM 렌더링 최적화와 화면 깜빡임/널뛰기(Scroll Jump)를 방지하는 프론트엔드 로직이 적용되어 UX 완성도를 높였습니다.
마. 유니캐스트(Unicast) AI 봇 동작 방식
!요약 명령어를 입력하면 WebClient를 통해 비동기로 Gemini API에 채팅 컨텍스트를 전송합니다. API 호출 동안 메인 스레드가 막히는 현상(Blocking)이 없으며, 요약된 결과는 전체 방에 쏘는 것이 아니라 명령어를 호출한 본인에게만 시스템 노티 구조로 응답합니다.
4. 디렉토리 구조 요약
config/: WebSocket, Redis, WebMvc 등 핵심적인 인프라스트럭처 설정
controller/: REST 컨트롤러 클래스와 웹소켓 메시지 라우터 역할의 클래스들 존재
service/: 비즈니스 로직 집중. 스케줄러 동작 및 AI 웹클라이언트 호출부와 서비스 계층
mapper/: MyBatis 쿼리를 매핑하는 인터페이스 계층
dto/: 클라이언트-서버 및 Redis 간 데이터 송수신 규격
interfaces/: 외부 클라우드 S3 같은 스토리지로 확장이 쉽게끔 OCP 원칙을 지키기 위한 업로더 인터페이스 계층
💡 분석 결론
기존 단순히 기능만 동작하는 MVP 형태의 프로젝트가 아니라 트래픽 병목 방지(Redis, 비동기 스레드), 예외 처리(ErrorResponse 규격), 모듈화(OCP 인터페이스 분리) 등 전체적인 구조와 흐름이 이미 실무 수준의 안정적인 백엔드 보일러플레이트에 가깝게 훌륭히 설계되어 있습니다. 앞으로 어떤 기능을 더 확장하거나 개선하려고 하시는지 명확히 정하신다면 수월하게 함께 작업할 수 있을 것입니다.


진행간 이슈

결론부터 아주 솔직하고 명확하게 말씀드릴게요. 네, 사용 한도가 존재하며, 현재 결제해서 사용 중이신 웹 버전(Gemini 앱)의 한도와 완벽하게 공유됩니다.

최근 개발자 커뮤니티(Reddit 등)에서도 안티그래비티의 이 '한도 문제'가 아주 뜨거운 이슈입니다. 왜 이런 현상이 발생하는지 원리를 파악하고 실무적인 대처법을 정리해 드릴게요.

🧠 원리 설명: 에이전트의 폭발적인 토큰 소모량과 요금제 구조
현재 결제하신 'Google One AI Premium(월 29,000원)' 요금제는 2TB 클라우드, 웹 버전 Gemini, 그리고 Antigravity의 3.1 Pro 모델 접근 권한을 모두 하나의 바구니(공유 쿼터)로 묶어서 제공합니다.

웹에서 채팅할 때는 내가 질문 하나를 던지면 AI가 답변 하나를 주니까 한도에 도달하기가 쉽지 않습니다. 하지만 Antigravity의 '자율 에이전트'는 작동 방식이 전혀 다릅니다.

에이전트의 숨은 소모량: 우리가 기능을 하나 추가해 달라고 지시하면, 에이전트 내부에서는 전체 파일 스캔 -> 계획 수립 -> 코드 작성 -> 터미널 에러 확인 -> 스스로 코드 수정 -> 재검증이라는 수십 번의 대화(프롬프트 체인)가 우리 눈에 보이지 않는 백그라운드에서 순식간에 오고 갑니다.

결과: 웹에서 한 달 쓸 토큰(글자 수)을 안티그래비티 에이전트는 단 5~10시간의 집중적인 코딩만으로 다 써버릴 수 있습니다. 이 한도를 다 쓰면 일정 시간 동안 강력한 3.1 Pro 모델이 잠기고 가벼운 모델로 제한됩니다. (당연히 웹 버전 Gemini 사용도 같이 제한됩니다.)