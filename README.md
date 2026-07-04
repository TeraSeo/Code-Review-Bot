# AI Code Review Bot

A backend system that automatically analyzes code and posts review comments using Claude AI whenever a GitHub Pull Request is created.

## Tech Stack
- **Spring Boot 4.0.6** / Java 21 / Maven
- **Spring Security** — blocks access to all endpoints except `/webhook`
- **Spring WebFlux WebClient** — asynchronous calls to the GitHub API and Claude API
- **Claude API** (`claude-3-5-haiku`) — code analysis and review generation
- **JUnit5 + Mockito** — unit testing

## Workflow
```
Developer creates a PR
  → GitHub sends a webhook
  → HMAC-SHA256 signature verification
  → Fetch changed .java files via the GitHub API
  → Analyze code via the Claude API
  → Automatically post review comments on the GitHub PR
```

## Project Structure
```
src/main/java/com/codebot/review/
├── config/
│   ├── GithubProperties.java       # GitHub configuration values (@ConfigurationProperties)
│   ├── AnthropicProperties.java    # Claude configuration values (@ConfigurationProperties)
│   ├── WebClientConfig.java        # WebClient bean configuration
│   └── SecurityConfig.java         # Endpoint access control
├── controller/
│   └── WebhookController.java      # Handles POST /webhook
├── service/
│   ├── WebhookVerificationService.java  # HMAC-SHA256 signature verification
│   ├── GitHubService.java               # GitHub API calls
│   ├── ClaudeService.java               # Claude API calls
│   └── ReviewService.java               # Orchestrates the overall review flow
└── model/
    ├── PullRequestEvent.java
    └── ChangedFile.java
```

## Running Locally

### 1. Set Environment Variables
Add the following in IntelliJ under `Run > Edit Configurations > Environment variables`:
```
GITHUB_TOKEN=ghp_xxxxxxxxxxxx
WEBHOOK_SECRET=random_string
ANTHROPIC_API_KEY=sk-ant-xxxxxxxxxxxx
```
Generate `WEBHOOK_SECRET`:
```bash
openssl rand -hex 32
```

### 2. Run the Server
```bash
./mvnw spring-boot:run
```

### 3. Expose Locally via ngrok
```bash
ngrok http 8080
```

### 4. Register a GitHub Webhook
GitHub Repository → Settings → Webhooks → Add webhook

| Field | Value |
|------|-----|
| Payload URL | `https://{ngrok-url}/webhook` |
| Content type | `application/json` |
| Secret | Value of `WEBHOOK_SECRET` |
| Events | Pull requests |

## Testing
```bash
./mvnw test
```
Jacoco coverage report:
```bash
open target/site/jacoco/index.html
```

## Key Implementation Highlights
- **HMAC-SHA256 signature verification**: prevents tampering with GitHub webhook requests
- **Java 21 Records**: reduces boilerplate by using immutable model objects
- **@ConfigurationProperties**: type-safe configuration management
- **Structured prompts**: ensures consistent review quality across bug, performance, security, and readability perspectives
