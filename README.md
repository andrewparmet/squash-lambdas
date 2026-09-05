# squash-lambdas

Kotlin AWS Lambdas for managing Tennis and Racquet Club reservations.

## Email calendar

Each user gets an isolated S3 prefix, email-processing Lambda, Club Locker token, and service-account-owned Google Calendar.

- Forwarded recipient: the address that received the original Club Locker message. Pass it as `-PforwardedRecipient`; it is
  used to route forwarded messages and does not receive calendar access automatically.
- SES receiver: the generated forwarding destination printed by the provisioning command.
- Calendar editors: addresses that receive calendar edit access. Pass them as a comma-separated `-PshareWith` value; they do
  not affect email routing. Include the forwarded recipient here if it also needs calendar access.

The processing flow is:

1. Club Locker sends a reservation message to the forwarded recipient.
2. That account forwards the message to its SES receiver.
3. SES writes the message to the user's S3 prefix.
4. S3 invokes the user's Lambda.
5. The Lambda parses the action, activity type, court, and time from the message.
6. For matches, the Lambda reconciles the event with Club Locker's current slot and player roster.
7. The Lambda updates the user's Google Calendar.

Provision another receiver with:

```shell
./gradlew :infra:provisionUser \
    -PforwardedRecipient=receiver@example.com \
    -PshareWith=editor@example.com,another-editor@example.com
```

## Automatic reservation maker

This Lambda runs shortly after midnight in Boston and attempts to book configured slots one week ahead. Separate daylight
and standard time schedules ensure it runs at the same local time throughout the year. See
[TimeFilter](app/src/main/kotlin/com/parmet/squashlambdas/reserve/TimeFilter.kt) and
[TimeFilterTest](app/src/test/kotlin/com/parmet/squashlambdas/reserve/TimeFilterTest.kt).

## Reservation monitor

This Lambda polls Club Locker on weekday afternoons and reports newly available prime-time slots. DynamoDB records previously
reported slots and expires them through TTL.

## Build and deploy

Build the application with `./gradlew :app:shadowJar`. Deploy Terraform and the Lambdas with `./gradlew :infra:deploy`.
