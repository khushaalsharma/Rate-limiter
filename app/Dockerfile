FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy maven wrapper
COPY .mvn/ .mvn/
COPY mvnw .
COPY pom.xml .
COPY src ./src

RUN chmod +x mvnw && ./mvnw clean package -DskipTests -X

FROM maven:3.9-eclipse-temurin-21-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]