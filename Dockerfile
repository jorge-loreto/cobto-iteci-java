# 1️⃣ Build stage: compile the app
FROM maven:3.9.3-eclipse-temurin-17 AS build
WORKDIR /app

# Copy Maven config and pom.xml first to leverage caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code
COPY src ./src

# Build the Spring Boot jar
RUN mvn clean package -DskipTests

# 2️⃣ Run stage: lightweight Java runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port your Spring Boot app uses
EXPOSE 8080

# Run the jar
ENTRYPOINT ["java","-jar","app.jar"]

