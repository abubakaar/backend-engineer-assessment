# Stage 1: Build the application
FROM apache/solr-nightly:9.5.1-SNAPSHOT-java21 AS builder

WORKDIR /app

# Copy Gradle files
COPY build.gradle .
COPY settings.gradle .
COPY gradlew .
COPY gradle ./gradle

# Set environment variables for database connection
ENV DB_URL=jdbc:postgresql://postgres:5432/mydatabase
ENV DB_USERNAME=myuser
ENV DB_PASSWORD=secret

# Copy application source code
COPY . .

# Stage 2: Run the application
FROM apache/solr-nightly:9.5.1-SNAPSHOT-java21

WORKDIR /app

# Copy the built JAR file from the previous stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose the port your application listens on
EXPOSE 8080

# Command to run the application
CMD ["java", "-jar", "app.jar"]
