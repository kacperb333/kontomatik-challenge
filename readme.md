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
Returns ```x-session``` header that can be used to fetch accounts information.

## Fetch accounts information
```
GET /session/accounts
x-session: ***
```
If accounts data has not been imported yet returns 
```
200 OK
```

If accounts data import failed returns:
```
200 OK
```
```json
{
  "data": "Import failed"
}
```
After accounts data has been imported successfully returns (example):
```
200 OK
```
```json
{
  "data": [
    {
      "name": "Account name",
      "balance": "100.00",
      "currency": "USD"
    }
  ]
}
```

# How to setup database
Configure mongodb port (default `spring.data.mongodb.port: 27017`) in `service/src/main/resources/application.yml`

# How to build and run?
```./gradlew clean build``` in order to perform clean build of application.

```./gradlew bootRun``` in order to start application (runs on 8080 port by default).  
