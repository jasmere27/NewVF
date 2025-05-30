# Start with a base OpenJDK 24 image
FROM openjdk:17-jdk-alpine

# Set working directory inside the container
WORKDIR /app

# Copy your jar file into the container
COPY target/verifact-0.0.1-SNAPSHOT.jar app.jar

# Expose the port your app runs on (default 8080)
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]