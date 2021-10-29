# NAMPI Backend

This is the server backend and API for NAMPI.

The NAMPI backend is a [Spring Boot](https://spring.io/projects/spring-boot) application, the data is stored in [Apache Fuseki](https://jena.apache.org/documentation/fuseki2/) and cached by [Redis](https://redis.io/). The identity management is handled by [Keycloak](https://www.keycloak.org/).

## Prerequisites

The following prerequisites have to be met if the server is self hosted without the provided Docker and docker-compose configuration.

### Java

The server only runs on Java > version 11 which is the version the included Docker configuration uses.

### Keycloak

- A new keycloak [realm](https://www.keycloak.org/docs/latest/server_admin/index.html#_create-realm) needs to be created using the default settings.
- A new [client](https://www.keycloak.org/docs/latest/server_admin/index.html#_clients) has to be created using the default settings. _(For the development environment `Valid Redirect URIs` can be set to `*`)_
- A role `user` for authenticated authors.
- A composite role `author` for authenticated authors that includes `user` has to be created.
- A mapper that maps a custom user attribute - to be used for RDF-ID overrides - into the keycloak access token. This needs to be configured for each used client. This field can be used to specify a different UUID than the keycloak user id to connect to the `core:author` individual in the database.

#### Optional users

Some users can be created for a development environment. They need to get at least the role `user` and a password assigned, but to be able to actually edit content they need have the `author` role.

### Redis

A Redis instance has to be pre-configured and made available to the Spring Boot application. It will be used to cache database queries as long as the `PROFILE` environment variable isn't set to `dev`. The cache will be cleared on each app restart automatically to make sure that no stale cache is served in case the code or the used ontologies change.

### Fuseki

- A stand alone fuseki instance has to be created.

## Configuring the application

A number of command line parameters are available to configure the application.

| Parameter                 | Mandatory | Default Value                               | Example                                           | Description                                                                                                                                  |
| ------------------------- | --------- | ------------------------------------------- | ------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------- |
| APPLICATION_PORT          |           | 8080                                        |                                                   | The port the application will run on                                                                                                         |
| CORE_OWL_URL              |           | http://purl.org/nampi/owl/core              |                                                   | The location of the NAMPI-Core ontology                                                                                                      |
| DATA_BASE_URL             |           | The URL used in the current Servlet Request | https://example.com/nampi/data                    | The URL to use when creating internal links or new database individuals. When omitted, the current servlet request is used                   |
| DATA_URL                  | \*        |                                             | http://localhost:3030/data                        | The URL to the data dataset where the original data will be stored                                                                           |
| DEFAULT_LIMIT             |           | 20                                          |                                                   | The default result number to return when requesting from a collection endpoint like '/persons                                                |
| INF_CACHE_URL             | \*        |                                             | http://localhost:3030/inf_cache                   | The URL to the joint inference dataset where the cached inference data will be stored                                                        |
| KEYCLOAK_FRONTEND_URL     | \*        |                                             | http://localhost:8080/auth                        | The base URL for frontend requests (see [official documentation](https://hub.docker.com/r/jboss/keycloak/))                                  |
| KEYCLOAK_RDF_ID_ATTRIBUTE |           | rdf-id                                      |                                                   | The name of the RDF-ID override attribute configured in the Keycloak client mapper settings                                                  |
| KEYCLOAK_REALM            | \*        |                                             | nampi                                             | The name of the Keycloak realm                                                                                                               |
| KEYCLOAK_RESOURCE         | \*        |                                             | nampi-client                                      | The name of the Keycloak client                                                                                                              |
| KEYCLOAK_URL              | \*        |                                             | http://localhost:8080/auth/                       | The URL of the Keycloak authentication endpoint                                                                                              |
| LOGGING_LEVEL             |           | INFO                                        | DEBUG                                             | The Spring Boot [logging level](https://docs.spring.io/spring-boot/docs/1.2.1.RELEASE/reference/htmlsingle/#boot-features-custom-log-levels) |
| OTHER_OWL_URLS            |           |                                             | http://example.com/owl/1,http://example.com/owl/2 | A comma separated list of ontologies that will be used for inference                                                                         |
| PROFILE                   |           | prod                                        |                                                   | The app profile to use, can be either "prod" or "dev"                                                                                        |
| REDIS_PORT                |           | 6379                                        |                                                   | The port on which the Redis instance is available                                                                                            |
| REDIS_URL                 | \*        |                                             | http://example.com/redis                          | The url under which the Redis instance is available                                                                                          |

### Custom ontologies

Using the `OTHER_OWL_URLS` environment parameter, custom ontologies can be added to the inference of the database. For performance and data integrity reasons they need to conform to the following rules:

1. Build on the [NAMPI core ontology](http://purl.org/nampi/owl/core) with regards to the core entities like `person`, `event`, `act` or `aspect`
2. Only use rules compatible with the OWL Micro reasoner as described in the [Apache Jena inference documentation](https://jena.apache.org/documentation/inference/#OWLcoverage)

## Deploying as a standalone Spring Boot application

The application can be run from the command line using Maven, the environment parameters can be appended to the command.

### Example

`mvn spring-boot:run "'-Dspring-boot.run.arguments=--DATA_BASE_URL=https://example.com/nampi/data,--KEYCLOAK_FRONTEND_URL=http://localhost:8080/auth,--KEYCLOAK_URL=http://localhost:8080/auth,--KEYCLOAK_REALM=nampi,--KEYCLOAK_RESOURCE=nampi-client,--LOGGING_LEVEL=DEBUG,--REDIS_PORT=6379,--REDIS_URL=http://localhost,--INF_CACHE_URL=http://localhost:3030/inf-cache,--DATA_URL=http://localhost:3030/data'"`

#### Windows

`mvn spring-boot:run "-Dspring-boot.run.arguments=--DATA_BASE_URL=https://example.com/nampi/data --KEYCLOAK_FRONTEND_URL=http://localhost:8080/auth --KEYCLOAK_URL=http://localhost:8080/auth --KEYCLOAK_REALM=nampi --KEYCLOAK_RESOURCE=nampi-client --LOGGING_LEVEL=DEBUG --REDIS_PORT=6379 --REDIS_URL=http://localhost --INF_CACHE_URL=http://localhost:3030/inf-cache --DATA_URL=http://localhost:3030/data"`

## Deploying as standalone Docker container

The application can be run as a standalone Docker container connected to pre-existing Fuseki and Keycloak containers or external services. The environment parameters shown in [the configuration section](#configuring-the-application) can be added with `--build-arg` (see [documentation](https://docs.docker.com/engine/reference/commandline/build/#set-build-time-variables---build-arg)). Default values still apply as documented above.

Example:

```
docker build --build-arg DATA_BASE_URL=https://example.com/nampi/data --build-arg KEYCLOAK_FRONTEND_URL=http://localhost:8080/auth --build-arg KEYCLOAK_REALM=nampi --build-arg KEYCLOAK_RESOURCE=nampi-client --build-arg KEYCLOAK_URL=http://example.com/keycloak/auth --build-arg LOGGING_LEVEL=TRACE --build-arg OTHER_OWL_URLS=http://purl.org/nampi/owl/monastic-life --build-arg REDIS_PORT=6379 --build-arg REDIS_URL=http://example.com/redis --build-arg INF_CACHE_URL=http://example.com/fuseki/inf-cache --build-arg DATA_URL=http://example.com/fuseki/data .
```

## Deploying with `docker-compose`

In addition to the pure Docker container approach, a complete environment for NAMPI can be set up with the provided `docker-compose.yml` file and an accompanying `docker-compose.override.yml` to specify exposed ports, reverse proxy settings or similar configurations. The compose-file has been kept as slim as possible to make it better adaptable to different environments.

If used as-is it will start containers for Fuseki, Redis and Keycloak, including a Postgres Container to store the user data, in addition to the actual backend application. The environment parameter mentioned in [the configuration section](#configuring-the-application) still apply with a change: the full `TRIPLE_STORE_URL` gets replaced by `DATASET_NAME` as the rest of the URL is already set to using the included Fuseki container. Environment parameters can be set in `docker-compose.override.yml` or in a separate `.env` file. Please see the following example:

`.env`

```
DATA_BASE_URL=https://example.com/nampi/data
FUSEKI_ADMIN_PASSWORD=[fuseki admin password]
KEYCLOAK_FRONTEND_URL=http://localhost:8080/auth
KEYCLOAK_PASSWORD=[keycloak password]
KEYCLOAK_PG_PASSWORD=[keycloak pg password]
KEYCLOAK_REALM=nampi
KEYCLOAK_RESOURCE=nampi-client
OTHER_OWL_URLS=http://purl.org/nampi/owl/monastic-life
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

- None (Defaults to JSON-LD)
- `application/ld+json`
- `application/n-triples`
- `application/rdf+xml`
- `text/turtle`

### Authentication

Protected endpoints like `/users/search/current` need to be accessed using a valid `OAuth 2` Bearer Token Header `Authorization: Bearer [Token]`. The token can be acquired / refreshed in all ways configured in Keycloak. Examples are:

### Resource Owner Credentials

Access Token Endpoint: `[Keycloak URL]/realms/nampi/protocol/openid-connect/token`

### Authorization Code

Authorization Endpoint: `[Keycloak URL]/auth/realms/nampi/protocol/openid-connect/auth`
Access Token Endpoint: `[Keycloak URL]/auth/realms/nampi/protocol/openid-connect/token`
