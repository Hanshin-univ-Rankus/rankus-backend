# 1. Java 17 slim 이미지 사용
FROM openjdk:17-jdk-slim

# 2. JAR 파일 복사 (정확한 파일명 사용)
COPY rankus/build/libs/app.jar /app.jar

# 3. 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]
