# 1. base image
FROM eclipse-temurin:21-jre

# 2. working dir
WORKDIR /llm-report-service

# 3. copy generated jar
COPY target/*.jar app.jar

# 4. expose port
EXPOSE 8080

# 5. docker run command
CMD ["java", "-jar", "app.jar"]