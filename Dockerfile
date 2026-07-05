# Multi-stage build for optimized Docker image
FROM gradle:8.5-jdk17 AS builder

WORKDIR /app

COPY build.gradle settings.gradle ./
COPY src ./src

# Build the application
RUN gradle build --no-daemon --stacktrace

# Runtime stage
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Create a non-root user
RUN groupadd -r tourfolio && useradd -r -g tourfolio tourfolio
RUN chown -R tourfolio:tourfolio /app
USER tourfolio

# Expose port 8000
EXPOSE 8000

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8000/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
