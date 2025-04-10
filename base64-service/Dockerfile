# Dockerfile for base64-service
# Use an official Tomcat base image with the correct JDK version
# Consistent with Phase 1 backend
#FROM tomcat:11-jdk21-openjdk
FROM tomcat:11.0.0-jdk21-openjdk

# Set the working directory inside the container
WORKDIR /usr/local/tomcat

# --- Application Artifact ---
# Copy the built WAR file from the Maven target directory into Tomcat's webapps directory.
# Renaming it to ROOT.war makes it deploy to the root context path (/).
# Ensure your Maven build produces 'base64-service-0.0.1-SNAPSHOT.war' in the 'target' directory.
COPY target/base64-service-0.0.1-SNAPSHOT.war webapps/ROOT.war

# --- Port Exposure ---
# Expose the port Tomcat listens on (default is 8080)
EXPOSE 8080

# --- Runtime Configuration Comments ---
# The following configurations are expected to be provided at runtime (e.g., via docker-compose environment variables):
#
# Required Environment Variables:
#   - SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI: URL of the Keycloak issuer (e.g., https://idp:8864/realms/sso)
#
# Optional Environment Variables:
#   - SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_AUDIENCES: Expected audience claim value (e.g., devdemo-base64)
#   - Other Spring Boot properties if needed

# --- Startup Command ---
# The base tomcat image already has a suitable CMD ["catalina.sh", "run"]
# No need to override unless custom startup logic is required.
# CMD ["catalina.sh", "run"] 