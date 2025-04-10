# Dockerfile for backend
FROM tomcat:11.0.0-jdk21-openjdk
WORKDIR /usr/local/tomcat
COPY target/backend-0.0.1-SNAPSHOT.war webapps/ROOT.war
EXPOSE 8080