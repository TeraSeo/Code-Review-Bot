# AI Code Review Bot

GitHub Pull Request가 생성되면 Claude AI가 자동으로 코드를 분석하고 리뷰 코멘트를 등록하는 백엔드 시스템입니다.

## 기술 스택

- **Spring Boot 4.0.6** / Java 21 / Maven
- **Spring Security** — `/webhook` 외 엔드포인트 접근 차단
- **Spring WebFlux WebClient** — GitHub API, Claude API 비동기 호출
- **Claude API** (`claude-3-5-haiku`) — 코드 분석 및 리뷰 생성
- **JUnit5 + Mockito** — 단위 테스트

## 동작 흐름

```
개발자 PR 생성
  → GitHub Webhook 발송
  → HMAC-SHA256 서명 검증
  → GitHub API로 변경된 .java 파일 조회
  → Claude API로 코드 분석
  → GitHub PR에 리뷰 코멘트 자동 등록
```

## 프로젝트 구조

```
src/main/java/com/codebot/review/
├── config/
│   ├── GithubProperties.java       # GitHub 설정값 (@ConfigurationProperties)
│   ├── AnthropicProperties.java    # Claude 설정값 (@ConfigurationProperties)
│   ├── WebClientConfig.java        # WebClient 빈 설정
│   └── SecurityConfig.java         # 엔드포인트 접근 제어
├── controller/
│   └── WebhookController.java      # POST /webhook 처리
├── service/
│   ├── WebhookVerificationService.java  # HMAC-SHA256 서명 검증
│   ├── GitHubService.java               # GitHub API 호출
│   ├── ClaudeService.java               # Claude API 호출
│   └── ReviewService.java               # 전체 리뷰 흐름 오케스트레이션
└── model/
    ├── PullRequestEvent.java
    └── ChangedFile.java
```

## 로컬 실행

### 1. 환경변수 설정

IntelliJ `Run > Edit Configurations > Environment variables`에 추가:

```
GITHUB_TOKEN=ghp_xxxxxxxxxxxx
WEBHOOK_SECRET=랜덤_문자열
ANTHROPIC_API_KEY=sk-ant-xxxxxxxxxxxx
```

WEBHOOK_SECRET 생성:
```bash
openssl rand -hex 32
```

### 2. 서버 실행

```bash
./mvnw spring-boot:run
```

### 3. ngrok으로 외부 노출

```bash
ngrok http 8080
```

### 4. GitHub Webhook 등록

GitHub Repository → Settings → Webhooks → Add webhook

| 항목 | 값 |
|------|-----|
| Payload URL | `https://{ngrok-url}/webhook` |
| Content type | `application/json` |
| Secret | WEBHOOK_SECRET 값 |
| Events | Pull requests |

## 테스트

```bash
./mvnw test
```

Jacoco 커버리지 리포트:
```bash
open target/site/jacoco/index.html
```

## 주요 구현 포인트

- **HMAC-SHA256 서명 검증**: GitHub Webhook 요청 위변조 방지
- **Java 21 Record**: 불변 모델 객체로 보일러플레이트 제거
- **@ConfigurationProperties**: 타입 안전한 설정값 관리
- **구조화된 프롬프트**: 버그·성능·보안·가독성 관점의 일관된 리뷰 품질 확보
