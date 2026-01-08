# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .

# Download dependencies separately to leverage Docker cache
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the jar file from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]