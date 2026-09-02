# Build Stage
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build jar
COPY src ./src
RUN mvn clean package -DskipTests

# Run Stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Create data directory for H2 persistent storage
RUN mkdir -p /app/data

# Copy built JAR from build stage
COPY --from=build /app/target/intellierp-1.0.0.jar app.jar

# Expose default port
EXPOSE 3000

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
