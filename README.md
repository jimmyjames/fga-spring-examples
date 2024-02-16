# Spring Security Examples with OpenFGA

Repository demonstrating use cases and possible solutions to integrate FGA with Spring Security.

## Goals

The goals of this repository are to:
- Show how OpenFGA can be integrated with Spring today
- Give insight into possible DX improvements, either through an FGA-owned starter/library, possible direct Spring Security integration, or customer guidance

## Samples

- `simple-no-auth` is a sample FGA integration that has no Spring security configured. It is a simple example that makes assumptions about users and principals.
- `resource-server` and `client-restclient` demonstrate a resource server with JWT authorization using the `okta-spring-boot-starter` and a client credentials flow to obtain a JWT to make API calls. The API's in `resource-server` are protected both by JWT and FGA checks, and are called by `client-restclient`.

## Prerequisites

- Docker
- Java 17
- [OpenFGA CLI](https://github.com/openfga/cli)

## Usage

### Simple no-auth sample

To run the `simple-no-auth` sample, see the [README](./simple-no-auth/README.md).

### Client credentials sample

This sample comprises of two parts:
- A resource server configured with the `okta-spring-boot-starter` to secure endpoints with JWTs issued by Auth0. It protects APIs with JWT authorization and uses FGA to protect endpoints and write authorization data.
- A client that uses the client credentials flow to obtain a JWT to call the resource server.

#### Create Auth0 application and API

- Create a new Auth0 API and note the API identifier
- Create a new Auth0 machine-to-machine application, and note the client ID and secret

#### Start OpenFGA and create a store and authorization model

This will start an in-memory database OpenFGA server:


```bash
docker pull openfga/openfga:latest
docker run --rm -e OPENFGA_HTTP_ADDR=0.0.0.0:4000 -p 4000:4000 -p 8081:8081 -p 3000:3000 openfga/openfga run
```

Create a store:

```bash
fga store create --name "Test Store" --api-url http://localhost:4000
```

You should receive a response like this. Note the store ID value:

```json
{
  "store": {
    "created_at":"2024-02-16T16:56:21.162910175Z",
    "id":"01HPSDHYXAD9HS906YFG9CQM02",
    "name":"Test Store",
    "updated_at":"2024-02-16T16:56:21.162910175Z"
  }
}
```

Create an authorization model:

```bash
fga model write --api-url http://localhost:4000 --store-id 01HPSDHYXAD9HS906YFG9CQM02 --file ./example-auth-model.json
```

You should receive a response like this. Note the `authorization_model_id`:


```json
{
  "authorization_model_id":"01HPSDPTTC209FQ0P4AMK3AZPE"
}
```

#### Configure resource server

Configure the application properties:

```bash
cd resource-server
cp src/main/resources/application.yml.example src/main/resources/application.yml
```

In `application.yml`, replace the `oauth2` properties with the values from your Auth0 application and API.

Also replace the values for `fga-store-id` and `fga-authorization-model-id` with the values created above.

#### Run resource server

```bash
./gradlew bootRun
```

This will start the server on port 8082.

#### Configure the client

Configure the application properties:

```bash
cd client-restclient
cp src/main/resources/application.yml.example src/main/resources/application.yml
```

Replace the oauth2 values and `auth0-audience` with the values of your Auth0 application and API identifier.

#### Start the application

```
./gradlew bootRun
```

This will start the application, execute the client credentials grant to obtain a JWT, and then makes calls to the resource server:

- Attempt to `GET` a "document" for which the current principal does **not** have an FGA relation to. This request should fail with a `403`.
- A call to create a "document", which will create an FGA relationship associated with the principal.
- Another attempt to get the document, which should now return successfully as there is a `reader` relationship between the principal and the document.

You can see the results of these calls in the application logs.

