# ─── 1단계: 소스 → 실행 JAR 빌드 (JDK 포함) ─────────
FROM gradle:8.5.0-jdk17 AS builder
WORKDIR /home/gradle/project

# 프로젝트 전체 복사 후, clean build 실행
COPY . .
RUN chmod +x gradlew \
 && ./gradlew clean bootJar --no-daemon

# ─── 2단계: 런타임 이미지 (멀티아치 지원 JRE) ──────
FROM eclipse-temurin:17-jre
WORKDIR /app

# 빌드 결과물 복사
COPY --from=builder /home/gradle/project/build/libs/app.jar ./app.jar

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
