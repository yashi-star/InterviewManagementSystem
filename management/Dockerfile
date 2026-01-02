# ============================================
# STAGE 1: Build Stage
# ============================================
# This stage compiles your Java application
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set working directory inside container
WORKDIR /app

# Copy Maven files first (for better caching)
# Docker will cache this layer if pom.xml doesn't change
COPY pom.xml .

# Download dependencies (this layer is cached)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
# -DskipTests: Skip tests during Docker build (we'll test before building)
RUN mvn clean package -DskipTests

# ============================================
# STAGE 2: Runtime Stage
# ============================================
# This stage creates the final lightweight image
FROM eclipse-temurin:21-jre-alpine

# Install curl for healthcheck
RUN apk add --no-cache curl

# Set working directory
WORKDIR /app

# Copy the built JAR from build stage
# The JAR name comes from your pom.xml <artifactId>
COPY --from=build /app/target/*.jar app.jar

# Create directory for resume uploads
RUN mkdir -p /app/resumes && chmod 755 /app/resumes

# Expose port 8087 (from application.properties)
EXPOSE 8087

# Health check - verify app is running
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8087/actuator/health || exit 1

# Run the application
# Use exec form to ensure proper signal handling
ENTRYPOINT ["java", "-jar", "app.jar"]

# Optional: JVM options for production
# Uncomment and adjust based on your server resources
# ENTRYPOINT ["java", \
#   "-Xms512m", \
#   "-Xmx1024m", \
#   "-XX:+UseG1GC", \
#   "-jar", "app.jar"]