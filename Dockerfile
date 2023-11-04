# Use an official OpenJDK runtime as a parent image
FROM openjdk:11-jre-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file into the container
COPY target/user_info_service-0.0.1-SNAPSHOT.jar user_info_service.jar

# Expose the port your application will listen on
EXPOSE 8085

# Define the command to run your application when the container starts
CMD ["java", "-jar", "user_info_service.jar"]