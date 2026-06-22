# Email Events API
This is an API to recieve email events from Email Provider (e.g. IMI) and send them to the Email service.

test

## email-events
This is an application-restricted resource (hence it requires an OAuth 2.0 token in the `Authorization` header).

request: 
```
POST `/email-events/email-events`

{
    "deliveryInfoNotification": {
        "deliveryInfo": {
            "deliveryChannel": "email",
            "Description": "Submitted",
            "destinationType": "emailid",
            "timeStamp": "2016-07-21T12:44:23.644",
            "code": "7501",
            "deliveryStatus": "Submitted",
            "destination": "sample@domain.com"
        },
        "correlationid": "3bd8edf31c81-4b72d8a2-290d-49e2-993e",
        "callbackData": "return callbackdata",
        "transid": "4b72d8a2-290d-49e2-993e-3bd8edf31c81"
    }
}
```
response:
```
{
    "message":"true"
}
```

response codes
- 200 - Ok
- 400 - Bad Request
- 500 - Internal Server Error

# Run tests
```
sbt test
sbt it:test
```

## Running Integration Tests
- Running locally with service manager:
Go to a shell and start your services via `sm` or `sm2` with the DC_EMAIL_EVENTS integration test service-manager profile
```bash
sm --start DC_EMAIL_EVENTS_IT

# and to stop the services:
sm --stop DC_EMAIL_EVENTS_IT
```

Once started, you can run the integration tests `sbt> it:test` 
