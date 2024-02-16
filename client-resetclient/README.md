# Client

## Configure

Configure the application properties:

```bash
cd client-restclient
cp src/main/resources/application.yml.example src/main/resources/application.yml
```

Replace the oauth2 values and `auth0-audience` with the values of your Auth0 application and API identifier.

## Start the application

```
./gradlew bootRun
```