# 2026-04-01 작업 메모 (내일 이어서 진행)

## 오늘 합의한 핵심

- `Mapper`와 `DTO`는 역할이 다르므로 둘 다 필요하다.
  - Mapper: DB 접근 인터페이스 + XML SQL 매핑
  - DTO: 계층 간 데이터 전달 객체
- 다만, 파라미터가 늘어날 때마다 인터페이스와 XML을 고치는 부담을 줄이기 위해
  **"파라미터를 DTO(조회 조건 객체)로 묶는 방식"**으로 리팩터링하기로 함.

## 이미 반영된 것

- `.cursor/rules/` 규칙 파일 생성 완료
  - `00-safety-secrets.mdc`
  - `10-spring-architecture.mdc`
  - `20-mybatis-sync.mdc`
  - `30-websocket-chat-flow.mdc`
  - `40-jsp-static-resources.mdc`

## 다음 작업 대상 (Chat History)

현재:
- `ChatMapper.getChatHistory(@Param("lastId") Long lastId, @Param("limit") int limit)`

리팩터링 목표:
- `ChatHistoryQuery` DTO 생성 후 단일 파라미터로 전달
- 예: `ChatMapper.getChatHistory(ChatHistoryQuery query)`

수정 예정 파일:
- `src/main/java/com/example/chattingroom/dto/ChatHistoryQuery.java` (신규)
- `src/main/java/com/example/chattingroom/mapper/ChatMapper.java`
- `src/main/java/com/example/chattingroom/service/ChatService.java`
- `src/main/java/com/example/chattingroom/controller/ChatApiController.java`
- `src/main/resources/mapper/ChatMapper.xml` (필드명/파라미터 참조 점검)

## 기대 효과

- 메서드 시그니처 안정화 (조건 추가 시 영향 최소화)
- 가독성/유지보수성 개선
- 인자 순서 실수 방지

## 주의 사항

- API 요청 형식(`/api/chat/history?lastId=...&limit=...`)은 당분간 유지 가능
- `limit` 기본값/상한 검증 위치(Controller or Service)를 명확히 정해야 함
- MyBatis XML과 Java Mapper 시그니처는 항상 동기화 필요

## 내일 시작 순서 추천

1. `ChatHistoryQuery` DTO 생성
2. `ChatMapper` 시그니처 변경
3. `ChatService` 변경
4. `ChatApiController`에서 query 객체 조립
5. `ChatMapper.xml` 참조 점검
6. 실행 테스트 (`mvnw.cmd spring-boot:run`) 후 `/api/chat/history` 동작 확인
