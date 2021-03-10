FROM adoptopenjdk/maven-openjdk11
LABEL maintainer=“nampi@icar-us.eu”
ARG KEYCLOAK_REALM
ARG KEYCLOAK_RESOURCE
ARG KEYCLOAK_URL
ARG LOGGING_LEVEL
ARG OTHER_OWL_URLS
ARG TRIPLE_STORE_URL
EXPOSE 8080
COPY ./ ./
RUN mvn package
ENTRYPOINT ["java","-jar","./target/backend-0.0.1-SNAPSHOT.jar"]
