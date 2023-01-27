# What's this?
A simple application that asynchronously fetches info about PKO BP bank accounts for given credentials.
Username with password and additional sms one time password sign-in flow is assumed.

Mongodb is used as a database.

Fetched account information is available for 24h.

# How?
## Sign in to a PKO BP bank account
```
POST /session
```
```json
{
  "credentials": {
    "login": "***",
    "password": "***"
  }
}
```
Returns ```x-session``` header, that has to be used in following one time password request.
```
200 OK
x-session: ***
```

## Confirm login with one time password (sms)
```
POST /session/otp
x-session: ***
```
```json
{
  "otp": {
    "code": "***"
  }
}
```
Returns ```importId``` that can be used to fetch accounts information.
```
200 OK
```
```json
{
  "importId": "***"
}
```

## Fetch accounts information
```
GET /accounts?importId=***
```
If accounts data has not been imported yet returns 
```
204 NO CONTENT
```

If accounts data import failed returns:
```
200 OK
```
```json
{
  "message": "Import failed."
}
```
After accounts data has been imported successfully returns (example):
```
200 OK
```
```json
{
  "accounts": [
    {
      "name": "Account name",
      "balance": "100.00",
      "currency": "USD"
    }
  ]
}
```

# How to configure custom database
Configure mongodb connection in `service/src/main/resources/application.yml`

# How to build and run?
1. Install `docker`.
2. ```./gradlew clean build``` in order to perform clean build of application. 
3. ```docker-compose up``` in order to start application (runs on 8080 port by default).
