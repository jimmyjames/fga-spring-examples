# Spring Security Examples with OpenFGA

Simple repository demonstrating use cases and possible solutions to integrate FGA with Spring Security.

> NOTE: For simplicity, this sample **DOES NOT USE ANY AUTHENTICATION**, either for the Fga client or for Spring. This is NOT RECOMMENDED FOR PRODUCTION USE!

## About

This is a WIP repo demonstrating various possible ways to use FGA in a Spring application.

It demonstrates:
- Using a simple properties configuration to configure and build an `OpenFgaClient` that can be injected into any Spring component. This can be used to perform any FGA API calls, but primary use case is likely to write authorization data and is shown here.
- Using a simple Bean with `@PreAuthorize` to perform a method-level FGA check
- Using a custom annotation (`@FgaCheck`) with an `@Aspect` implementation to perform a method-level FGA check

## Goals

The goals of this repository are to:
- Show how OpenFGA can be integrated with Spring today
- Give insight into possible DX improvements, either through an FGA-owned starter/library, possible direct Spring Security integration, or customer guidance

## Usage

### Run OpenFGA

```bash
docker pull openfga/openfga:latest
docker run --rm -e OPENFGA_HTTP_ADDR=0.0.0.0:4000 -p 4000:4000 -p 8081:8081 -p 3000:3000 openfga/openfga run
```

See the [OpenFGA docs](https://openfga.dev/docs/getting-started/setup-openfga/docker#step-by-step) for more information.

### Start the app

```bash
./gradlew bootRun
```

On startup, the application will create an in-memory store with a simple authorization model, and write a tuple representing the following relation:

```
user: user:123
relation: reader
object: document:1
```

The examples hard-code a userId of `user:123`. We should consider if we can provide a default of the currently authenticated principal when authentication is added.

### Successful API call using simple FGA bean

Execute a GET request to obtain `document:1`:

`curl -X GET http://localhost:8080/docs/1`

You should see a simple success message in the console.

### Unauthorized API call using simple FGA bean

Execute a GET request to obtain `document:2`, for which `user:2` does not have a `reader` relation:

`curl -X http://localhost:8080/docs/2 -v`

You should receive a 403 response as `user:123` does not have the `reader` relation to `document:2`

### Successful API call using FGA annotation/aop

Execute a GET request to obtain `document:1`:

`curl -X GET http://localhost:8080/docsaop/1`

You should see a simple success message in the console.

### Unauthorized API call using FGA annotation/aop

Execute a GET request to obtain `document:2`, for which `user:2` does not have a `reader` relation:

`curl -X http://localhost:8080/docsaop/2 -v`

You should receive a 403 response as `user:123` does not have the `reader` relation to `document:2`

### Execute a POST request to write to FGA

Execute a POST request to create a relationship between `user:123` and `document:2`:

`curl -X POST -H "Content-Type: text/plain" -d "2" http://localhost:8080/docs`

You should see a message that document with ID `2` was created

You can now execute either of the GET requests to verify that `user:123` now has access to `document:2`
