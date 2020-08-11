# NAMPI Backend

This is the server backend and API for NAMPI.

## Prerequesite

The NAMPI backend is a [Spring Boot](https://spring.io/projects/spring-boot) app, the data is stored in [Apache Fuseki](https://jena.apache.org/documentation/fuseki2/) and the identity management is handled by [Keycloak](https://www.keycloak.org/).

*Note: Currently the server only runs on Java 13, Java 14 doesn't work yet with Keycloak, see this [issue](https://issues.redhat.com/browse/KEYCLOAK-13690).*

### Keycloak

* A new keycloak [realm](https://www.keycloak.org/docs/latest/server_admin/index.html#_create-realm) needs to be created using the default settings.
* A new [client](https://www.keycloak.org/docs/latest/server_admin/index.html#_clients) has to be created using the default settings. _(For the development environment `Valid Redirect URIs` can be set to `*`)_
* A role `user` for authenticated users has to be created.

#### Optional

Some users can be created for a development environment. They need to get at least the role `user` and a password assigned.

### Fuseki

* A stand alone fuseki instance has to be created.
* A persistent new dataset needs to be created to store data.
* _In a development environment, some test data can be imported in the dataset._

## Configuring the application

A number of command line parameters are available to configure the application.

| Parameter         | Mandatory | Default Value                                                       | Example                          | Description                                                                                                                                  |
|-------------------|-----------|---------------------------------------------------------------------|----------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------|
| APPLICATION_PORT  |           | 8080                                                                |                                  | The port the application will run on                                                                                                         |
| CORE_OWL_URL      |           | https://raw.githubusercontent.com/nam-pi/ontologies/master/core.owl |                                  | The location of the NAMPI-Core ontology                                                                                                      |
| KEYCLOAK_URL      | *         |                                                                     | http://localhost:8080/auth/      | The Keycloak URL                                                                                                                             |
| KEYCLOAK_REALM    | *         |                                                                     | nampi                            | The name of the Keycloak realm                                                                                                               |
| KEYCLOAK_RESOURCE | *         |                                                                     | nampi-client                     | The name of the Keycloak client                                                                                                              |
| LOGGING_LEVEL     |           | INFO                                                                | DEBUG                            | The Spring Boot [logging level](https://docs.spring.io/spring-boot/docs/1.2.1.RELEASE/reference/htmlsingle/#boot-features-custom-log-levels) |
| TRIPLE_STORE_URL  | *         |                                                                     | http://localhost:3030/nampi-data | The Fuseki URL including the path to the dataset                                                                                             |

## Running the Spring Boot application

The application can be run from the command line using Maven, the environment parameters can be appended to the command.

### Example

`mvn spring-boot:run "'-Dspring-boot.run.arguments=--KEYCLOAK_URL=http://localhost:8081/auth,--KEYCLOAK_REALM=nampi,--KEYCLOAK_RESOURCE=nampi-client,--LOGGING_LEVEL=DEBUG,--TRIPLE_STORE_URL=http://localhost:3030/nampi-data'"`

#### Windows

`mvn spring-boot:run "-Dspring-boot.run.arguments=--KEYCLOAK_URL=http://keycloak.dev.local:8080/auth --KEYCLOAK_REALM=nampi --KEYCLOAK_RESOURCE=nampi-client --LOGGING_LEVEL=DEBUG --TRIPLE_STORE_URL=http://localhost:3030/nampi-data"`

Note: To work on Windows, Keycloak must be reachable with a domain, this can be configured in the hosts file:

Location: `C:\Windows\System32\Drivers\etc\hosts`

Added mapping: `127.0.0.1 keycloak.dev.local`



## Requesting data

The application runs on the configured port, requests can be made using standard HTTP requests

### Possible Accept Headers

* None (Defaults to JSON-LD)
* `application/ld+json`
* `application/n-triples`
* `application/rdf+xml`
* `text/turtle`

### Authentication

Protected endpoints like `/users/search/current` need to be accessed using a valid `OAuth 2` Bearer Token Header `Authorization: Bearer [Token]`. The token can be acquired / refreshed in all ways configured in Keycloak. Examples are:

### Resource Owner Credentials

Access Token Endpoint: `[Keycloak URL]/realms/nampi/protocol/openid-connect/token` 

### Authorization Code

Authorization Endpoint: `[Keycloak URL]/auth/realms/nampi/protocol/openid-connect/auth`
Access Token Endpoint: `[Keycloak URL]/auth/realms/nampi/protocol/openid-connect/token`
