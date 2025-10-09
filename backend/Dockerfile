FROM eclipse-temurin:21-jdk

WORKDIR /app

# Install Node.js and npm
RUN apt-get update && apt-get install -y curl \
    && curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs \
    && npm install -g npm@latest

# Maven Wrapper
COPY .mvn/ ./.mvn/
COPY mvnw mvnw.cmd pom.xml ./
RUN chmod +x ./mvnw

COPY package*.json ./
RUN npm install


# Copy source
COPY src ./src
# Include test directory for MCP filesystem server
COPY mcptest ./mcptest

# Build
RUN ./mvnw --version && ./mvnw clean package -DskipTests

# Expose port
EXPOSE 8080

# Copy JAR
RUN cp target/*.jar app.jar
CMD ["java", "-jar", "app.jar"]
