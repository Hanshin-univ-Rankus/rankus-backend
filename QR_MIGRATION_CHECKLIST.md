# QR 리팩터 & 출석 흐름 개선 체크리스트

상태 표기: [ ] 예정 / [~] 진행중 / [x] 완료 / [!] 추가 검토

## 1단계: 인프라 & 구성 (Backend)
- [x] (1) application.yml 에 rankus.qr.secret-key 프로퍼티 추가 (환경변수로 오버라이드 가능)
- [x] (2) QRProperties (@ConfigurationProperties) 추가 및 길이(32바이트) 검증
- [x] (3) QRTokenCryptoService (SecureQRToken 생성/복호화 캡슐화) 추가
- [x] (4) AttendanceSessionCommandService 에서 하드코딩 secret 제거 및 QRTokenCryptoService 주입
- [x] (5) Secure QR 기본 흐름 문서화 및 legacy /qr @Deprecated 명시 (코드 + 문서)

## 2단계: 신규 공용 API 추가
- [x] (6) GET /api/attendance/qr/resolve 구현 (공개, Secure/Legacy 지원)
- [x] (7) POST /api/attendance/check 구현 (글로벌 토큰 단일 파라미터 기반)
- [x] (8) 기존 POST /api/labs/{labId}/attendance/sessions/check @Deprecated 처리

## 3단계: 컨트롤러/DTO/문서
- [x] (9) Secure QR 생성 응답 DTO (SecureQRTokenResponseDto) 적용
- [x] (10) Swagger / docs/api-documentation.md 업데이트 (새 플로우 추가)
- [x] (11) 에러 reason 표 (OK / INVALID / EXPIRED / SESSION_INACTIVE / SESSION_NOT_FOUND) 문서화 완료

## 4단계: 서비스/도메인 보강
- [x] (12) resolve API 비활성 세션 valid=false 정책 반영
- [x] (13) checkAttendance 내부 token-only 파싱 로직 private 메서드로 분리
- [~] (14) 레거시 QRToken 제거 전략: QRProperties.legacyEnabled 플래그 도입 (기본 true) → false 시 legacy 파싱 비활성 (문서 반영 예정)

## 5단계: 테스트
- [~] (15) Secure 토큰 생성/복호화 단위 테스트 (초안 클래스 추가 예정)
- [~] (16) resolve API 통합(WebMvc) 테스트 (기본 valid/invalid 케이스 추가 예정)
- [ ] (17) global check API 통합 테스트 (성공/중복/세션종료/만료) – TODO
- [ ] (18) 기존 컨트롤러 리그레션 테스트 수정 (Deprecated 경로 호환성) – TODO

## 6단계: 프론트 연동 가이드 (docs/frontend-reference.md 갱신)
- [x] (19) /attend?qt= 흐름 + 로그인 리다이렉트 가이드 추가
- [~] (20) UX 상태 다이어그램 (텍스트 형태 1차 반영, 도식화 추후)

## 7단계: 운영/보안
- [~] (21) secret-key 운영 환경 변수 가이드 (README/ops 문서 초안 예정)
- [ ] (22) secret rotate 전략 초안 (이중 키 정책) – TODO
- [ ] (23) Rate limiting 정책 메모 (resolve 엔드포인트 대상) – TODO

## 8단계: 후속 개선(옵션)
- [ ] (24) WebSocket 실시간 출석 현황
- [ ] (25) 토큰 Replay 해시 블랙리스트 PoC
- [ ] (26) JWE/JWS 비교 분석 문서

---
진행 로그:
- 2025-10-02: 1~4 완료 (secure key 외부화 및 서비스 분리)
- 2025-10-02: 5~11 완료, 글로벌 API/문서 적용
- 2025-10-02: 12~13 적용, 14 플래그 도입, 15~16 테스트 초안 진행 준비
