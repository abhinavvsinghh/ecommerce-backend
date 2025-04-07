# Build stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
# Download all dependencies
RUN mvn dependency:go-offline -B
COPY src/ /app/src/
RUN mvn package -DskipTests -P cloud

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=cloud

# Create a non-root user
RUN addgroup -S amcart && adduser -S amcart -G amcart

# Copy JAR file
COPY --from=build /app/target/*.jar app.jar

# Change ownership of the application to non-root user
RUN chown -R amcart:amcart /app
USER amcart

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]