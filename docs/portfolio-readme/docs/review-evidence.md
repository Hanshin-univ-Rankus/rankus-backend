# Rankus 검토 근거

[README로 돌아가기](../README.md)

## 판본

- 팀 GitHub 기본 브랜치의 확인 기준: `70910e0feef32f6044c03a23a997c1ebb5ea3ee7`. [기존 구현](https://github.com/Hanshin-univ-Rankus/rankus-backend/blob/70910e0feef32f6044c03a23a997c1ebb5ea3ee7/rankus/src/main/java/org/univ/rankus/application/service/command/InterviewCommandService.java#L215-L247).
- 팀 저장소의 로컬 작업 브랜치: `f2d1e809f0eddfda2a40943eeb80de76453dc28b`. 노성빈의 `04bbdbcfab87a611029acfb4028c38786ca2a283`가 식별자 보존 변경이며 확인한 원격 기본 브랜치의 조상에 없다. 공개되거나 배포된 수정으로 표기하지 않는다.
- `rankus/src/main/java/org/univ/rankus/application/service/command/InterviewCommandService.java`의 `updateInterview`, `domain/model/interview/Interview.java`의 `updateSettings`, `InterviewCommandServiceTest.UpdateInterviewTests`를 대조했다. 모의 저장소를 사용한 단위 테스트는 동일 ID, 저장, 기존 행 삭제 미호출, 캘린더에 넘긴 ID를 확인한다. 실제 DB 상태나 연쇄 삭제 결과를 증명하는 테스트는 아니다.
- 팀 종합설계 원본 영상에서 랩 목록(1:41), 면접 달력(1:47), 슬롯 관리(1:53), 달력→슬롯 관리(1:47–1:54)를 발췌했다. 주소창과 계정/QR 구간을 제외했고 앱 내용은 바꾸지 않았다. 이 로컬 수정이 포함된 영상은 아니다. 그림의 #17은 설명용이다.

## 시스템·환경 그림을 읽는 기준

- 팀 백엔드 소스의 `LabApplicationController.applyToLabWithSlot`은 경로의 연구실 ID와 본문의 슬롯 ID를 인증 주체와 함께 `LabApplicationCommandService`에 넘긴다. 서비스는 중복 신청을 확인하고 슬롯을 잠금 조회해 가능 여부를 검사한 뒤 신청을 저장한다. 이 코드 경로는 학생 GIF에서 호출하지 않았다.
- 팀 개발용 `docker-compose.yml`에는 기본 MySQL 8.0과 선택적 `test` MySQL 8.0이 별도 볼륨으로 정의되어 있다. `application.yml`과 `application-dev.yml`의 로컬 JDBC 설정을 대조했다. Compose로 Spring Boot나 React를 같이 배포한다는 뜻이 아니며, 현 운영의 AWS나 원격 DB 구성을 증명하지 않는다.

## 학생 면접 신청 시연

- 프런트엔드 기준: 팀 React 체크아웃 `c63ad0bada3418650eb727289316761d39bc1b76`. 작업 트리 변경은 포함하지 않고 Git 아카이브에서 잠금 파일로 의존성을 설치했다. React 소스 변경 없이 API 주소만 루프백으로 지정해 프로덕션 빌드를 만들었다. 별도의 시연 서버에서 목록의 세 연구실 이미지 주소를 제공하고, 상세 페이지는 시연 서버가 내보내는 번들 응답의 이미지 매핑 한 곳만 바꿔 같은 이미지를 연결했다.
- 로컬 브라우저에 가상 학생 세션을 준비했다. 앱 원래 UI에서 랩실 41의 목록·상세를 거쳐 가입 신청을 열었다. 12월 8일에는 면접 17이 자동 선택됐고 마감 슬롯 600은 비활성 상태였다. 초기 예약 가능 슬롯 601에서 14:30 슬롯 602로 직접 변경했다.
- 클릭으로 제출된 요청은 `POST /api/labs/41/applications/slot-based`, JSON 본문은 숫자형 ID `{"slotId":602}`였다. 캡처 하네스가 경로·본문·Content-Type을 확인한 뒤에만 HTTP 201을 반환했다. 실제 화면에서 ‘가입 신청이 완료되었습니다!’, ‘승인 후 랩실에 참여하실 수 있습니다.’를 확인했고, 이어 목록으로 돌아가는 동작을 별도로 확인했다. GIF에는 접수 성공 화면과 목록 복귀가 모두 담겼다.
- 브라우저는 외부 요청 차단, 서비스 워커 차단 상태에서 실행했다. 운영 API·실사용자 계정·DB는 연결하지 않았으며, 로그인·관리자 승인·실제 데이터 저장은 이 시연의 범위가 아니다. 비디오 컷과 정지 화면은 같은 실행에서 캡처했다.

요청·화면 검증 기록 · 재현 스크립트

```bash
# 위 프런트엔드 커밋의 Git 아카이브를 푼 격리 폴더에서 실행
npm ci
REACT_APP_API_URL=http://127.0.0.1:49781 npm run build
# Playwright 실행 환경에서 chromium과 FFmpeg 런타임 설치 후 실행
RANKUS_BUILD="$PWD/build" node /path/to/demo/capture-application.cjs
```

## 로컬 재검증

Java 17, Gradle Wrapper 8.13과 저장소 의존성을 사용할 수 있는 격리 환경에서 해당 작업 브랜치로 실행한다.

```bash
cd rankus
sh gradlew test \
  --tests 'org.univ.rankus.application.service.command.InterviewCommandServiceTest$UpdateInterviewTests' \
  --tests 'org.univ.rankus.domain.model.interview.InterviewTest'
```

이번에는 정확한 커밋의 Git 아카이브를 만든 뒤 명령을 시도했으나 `services.gradle.org` DNS 조회 실패로 Wrapper 다운로드에서 멈췄다. 테스트 통과·실패로 집계하지 않는다. 공개·실DB·운영 검증은 하지 않았다.

## 다른 학교 프로젝트와 구분

[Univ Group Management](https://github.com/SungKong00/univ_group_management)는 별도 Flutter/Spring 저장소이며, 채널 역할과 그룹 권한이 대표 사례다. [README PR #33](https://github.com/SungKong00/univ_group_management/pull/33)은 해당 저장소의 문서 작업으로, 팀 Rankus 서버의 변경 근거로 쓰지 않는다. [한국디지털콘텐츠학회 프로그램 E4-07](https://dcs.or.kr/homepage/boardMedia/194980#page=9)은 조직 계층과 RBAC에 관한 노성빈·이용걸 공동 저자 발표 자료의 목록이다.

## 편집 참고

[OpenProject README](https://github.com/opf/openproject#readme)에서 프로젝트 설명과 상세 사용·개발 자료를 분리하는 방식을 차용했다. 기능 나열보다 운영자 행동 하나와 코드가 바뀐 이유를 본문에 두었다.

## 녹화 검수

- 학생 신청: 연구실 소개 사진을 채운 로컬 React 브라우저에서 새로 기록한 25fps WebM으로 제작했다. 첫 화면 진입 전 기록만 덜어냈다. 제출과 완료 메시지, 목록 복귀의 순서는 바꾸지 않았다.
- 운영자 일정: 팀 기록 원본의 30fps 구간을 재사용했다. 주소창과 책갈피가 없어지도록 화면 영역을 일정하게 잘랐다. 달력에서 상세로 이동하고 슬롯 관리 모달을 여는 순서는 바꾸지 않았다.
- 파일 단위 검수 결과: 학생 신청 266프레임/10.64초(25fps), 운영자 일정 216프레임/7.21초(29.96fps).

## 일정 식별자를 유지한 근거

- 팀 저장소 docs/CALENDAR_GUIDE.md는 일반 일정과 면접 일정을 나누며 캘린더 면접 이벤트의 interviewId와 자동 연동 시점을 설명한다. 같은 판본의 CalendarEvent 모델에 interviewId가 있고, 개선 후 InterviewCommandService가 조회한 면접의 ID를 수정 성공 시 갱신 경로에 넘기는 것을 대조했다.

## 슬롯 접수의 구현 근거

- 팀 코드의 신청 서비스에서 자유 입력 면접 경로는 레거시로 남아 있고, 슬롯을 참조하는 신청 생성자를 사용한다. 서비스가 잠금 조회 이후 가용성을 검증하며, JPA 저장소의 잠금 모드는 PESSIMISTIC_WRITE다.
- LabApplication 생성자가 PENDING 상태를 설정하고 InterviewSlot.reserve()를 호출한다. InterviewSlot에서 신청 수와 정원을 비교해 FULL을 바꾼다. LabApplication 엔티티의 lab_id와 user_id 유일 제약을 대조했다. 시연은 화면 계약 확인이며, 위 트랜잭션·정원 설명은 코드 동작을 설명한다.

## 연구실 화면 사진

- 각 연구실의 주제에 맞춰 준비한 사진 3장을 원본 카드·상세의 기존 이미지 위치에 연결했다. 720×360 JPEG가 목업 API를 통해 목록 3장, 상세 1장, 목록 복귀 3장에서 모두 로드되고 브라우저에 No Image 문구가 없는 것을 검사했다. 사진과 필요한 로컬 연결은 시연용 패키지에만 들어 있다.
