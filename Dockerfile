FROM eclipse-temurin:17-jre
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8000
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8000/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "-Duser.timezone=Asia/Seoul", "app.jar"]
