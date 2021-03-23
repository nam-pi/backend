# NAMPI Backend

This is the server backend and API for NAMPI.

## Prerequisite

The NAMPI backend is a [Spring Boot](https://spring.io/projects/spring-boot) app, the data is stored in [Apache Fuseki](https://jena.apache.org/documentation/fuseki2/) and cached by [Redis](https://redis.io/) and the identity management is handled by [Keycloak](https://www.keycloak.org/).

*Note: Currently the server only runs on Java <14 because Java 14 doesn't work yet with Keycloak, see this [issue](https://issues.redhat.com/browse/KEYCLOAK-13633).*

### Keycloak

* A new keycloak [realm](https://www.keycloak.org/docs/latest/server_admin/index.html#_create-realm) needs to be created using the default settings.
* A new [client](https://www.keycloak.org/docs/latest/server_admin/index.html#_clients) has to be created using the default settings. _(For the development environment `Valid Redirect URIs` can be set to `*`)_
* A role `user` for authenticated users has to be created.

#### Optional

Some users can be created for a development environment. They need to get at least the role `user` and a password assigned.

### Redis

A Redis instance has to be pre-configured and made available to the Spring Boot application.

### Fuseki

* A stand alone fuseki instance has to be created.
* A persistent new dataset needs to be created to store data.
* _In a development environment, some test data can be imported in the dataset._

## Configuring the application

A number of command line parameters are available to configure the application.

| Parameter         | Mandatory | Default Value                   | Example                                           | Description                                                                                                                                  |
|-------------------|-----------|---------------------------------|---------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------|
| APPLICATION_PORT  |           | 8080                            |                                                   | The port the application will run on                                                                                                         |
| CORE_OWL_URL      |           | https://purl.org/nampi/owl/core |                                                   | The location of the NAMPI-Core ontology                                                                                                      |
| DEFAULT_LIMIT     |           | 25                              |                                                   | The default result number to return when requesting from a collection endpoint like '/persons                                                |
| KEYCLOAK_URL      | *         |                                 | http://localhost:8080/auth/                       | The URL  of the Keycloak authentication endpoint                                                                                             |
| KEYCLOAK_REALM    | *         |                                 | nampi                                             | The name of the Keycloak realm                                                                                                               |
| KEYCLOAK_RESOURCE | *         |                                 | nampi-client                                      | The name of the Keycloak client                                                                                                              |
| LOGGING_LEVEL     |           | INFO                            | DEBUG                                             | The Spring Boot [logging level](https://docs.spring.io/spring-boot/docs/1.2.1.RELEASE/reference/htmlsingle/#boot-features-custom-log-levels) |
| OTHER_OWL_URLS    |           |                                 | http://example.com/owl/1,http://example.com/owl/2 | A comma separated list of ontologies that will be used for inference                                                                         |
| REDIS_PORT        |           | 6379                            |                                                   | The port on which the Redis instance is available                                                                                            |
| REDIS_URL         | *         |                                 | http://example.com/redis                          | The url under which the Redis instance is available                                                                                          |
| TRIPLE_STORE_URL  | *         |                                 | http://localhost:3030/nampi-data                  | The Fuseki URL including the path to the dataset                                                                                             |

## Deploying as a standalone Spring Boot application

The application can be run from the command line using Maven, the environment parameters can be appended to the command.

### Example

`mvn spring-boot:run "'-Dspring-boot.run.arguments=--KEYCLOAK_URL=http://localhost:8081/auth,--KEYCLOAK_REALM=nampi,--KEYCLOAK_RESOURCE=nampi-client,--LOGGING_LEVEL=DEBUG,--REDIS_URL=http://localhost,--TRIPLE_STORE_URL=http://localhost:3030/nampi-data'"`

#### Windows

`mvn spring-boot:run "-Dspring-boot.run.arguments=--KEYCLOAK_URL=http://keycloak.dev.local:8080/auth --KEYCLOAK_REALM=nampi --KEYCLOAK_RESOURCE=nampi-client --LOGGING_LEVEL=DEBUG --REDIS_URL=http://localhost --TRIPLE_STORE_URL=http://localhost:3030/nampi-data"`

Note: To work on Windows, Keycloak must be reachable with a domain, this can be configured in the hosts file:

    Location: `C:\Windows\System32\Drivers\etc\hosts`
    Added mapping: `127.0.0.1 keycloak.dev.local`

## Deploying as standalone Docker container

The application can be run as a standalone Docker container connected to pre-existing Fuseki and Keycloak containers or external services. The environment parameters shown in [the configuration section](#configuring-the-application) can be added with `--build-arg` (see [documentation](https://docs.docker.com/engine/reference/commandline/build/#set-build-time-variables---build-arg)). Default values still apply as documented above.

Example:

```
docker build --build-arg KEYCLOAK_REALM=nampi --build-arg KEYCLOAK_RESOURCE=nampi-client --build-arg KEYCLOAK_URL=http://example.com/keycloak/auth --build-arg LOGGING_LEVEL=TRACE --build-arg OTHER_OWL_URLS=https://purl.org/nampi/owl/monastic-life --build-arg REDIS_URL=http://example.com/redis --build-arg TRIPLE_STORE_URL=http://example.com/fuseki/data .
```

## Deploying with `docker-compose`

In addition to the pure Docker container approach, a complete environment for NAMPI can be set up with the provided `docker-compose.yml` file and an accompanying `docker-compose.override.yml` to specify exposed ports, reverse proxy settings or similar configurations. The compose-file has been kept as slim as possible to make it better adaptable to different environments.

If used as-is it will start containers for Fuseki and Keycloak, including a Postgres Container to store the user data, in addition to the actual backend application. The environment parameter mentioned in [the configuration section](#configuring-the-application) still apply with a change: the full `TRIPLE_STORE_URL` gets replaced by `DATASET_NAME` as the rest of the URL is already set to using the included Fuseki container. Environment parameters can be set in `docker-compose.override.yml` or in a separate `.env` file. Please see the following example:

`.env`

```
DATASET_NAME=data
FUSEKI_ADMIN_PASSWORD=[fuseki admin password]
KEYCLOAK_PASSWORD=[keycloak password]
KEYCLOAK_PG_PASSWORD=[keycloak pg password]
KEYCLOAK_REALM=nampi
KEYCLOAK_RESOURCE=nampi-client
OTHER_OWL_URLS=https://purl.org/nampi/owl/monastic-life
REDIS_URL=http://example.com/redis
```

To directly expose the containers (for example to use the Fuseki and Keycloak admin interfaces) to the web, the following docker-compose.override.yml file can be used as a starting point:

`docker-compose.override.yml`

```
version: "3"

services:
  fuseki:
    ports:
      - 3030:3030
  keycloak:
    ports:
      - 8080:8080
  web:
    ports:
      - 4000:8080
```

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
