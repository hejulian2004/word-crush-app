FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app/server

COPY server/.mvn .mvn
COPY server/mvnw server/pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw -q -DskipTests dependency:go-offline

COPY server/src src
COPY shared/api-contract/src/main/java /app/shared/api-contract/src/main/java
RUN ./mvnw -q -DskipTests clean package

FROM eclipse-temurin:17-jre
WORKDIR /app/server

COPY --from=builder /app/server/target/word-crush-server-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
