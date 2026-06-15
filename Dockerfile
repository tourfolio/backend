FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build.gradle .
COPY settings.gradle .
COPY src ./src

RUN apt-get update && \
    apt-get install -y gradle && \
    gradle build --no-daemon || true

RUN gradle build --no-daemon

EXPOSE 8000

CMD ["java", "-jar", "build/libs/tourfolio-0.0.1-SNAPSHOT.jar"]
