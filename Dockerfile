# Stage 1: Builder with Gradle 8.5 and JDK 17
FROM gradle:8.5-jdk17 AS builder

WORKDIR /app

COPY gradlew build.gradle settings.gradle ./
COPY gradle ./gradle
COPY src ./src

# Build the application with Gradle 8.5
RUN chmod +x ./gradlew && ./gradlew bootJar -x test --no-daemon --stacktrace

# Stage 2: Runtime with JRE 17
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Create a non-root user
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd -r tourfolio \
    && useradd -r -g tourfolio tourfolio
RUN chown -R tourfolio:tourfolio /app
USER tourfolio

# Expose port 8000 for Spring Boot application
EXPOSE 8000

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8000/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
