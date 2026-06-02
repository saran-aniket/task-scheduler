# Multi-stage build for efficient containerization
FROM eclipse-temurin:21 as builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Install Maven
RUN apt-get update && apt-get install -y maven

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

# Add labels for documentation
LABEL maintainer="Task Scheduler"
LABEL description="Scheduling different tasks"
LABEL version="1.0.0"

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]