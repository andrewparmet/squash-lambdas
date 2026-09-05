# squash-lambdas

This project contains several AWS Lambdas. Build with `./gradlew :app:shadowJar`.

## Email Parser

The motivation for this Lambda was that Club Locker provides a good interface for booking and managing reservations for the
various court sports at the Tennis and Racquet Club. It fails to integrate with Google Calendar, and I don't want to be
bothered to manually create events each time I make a reservation. Club Locker sends predictable and easily parseable emails
that provide an easy integration point for this Lambda.

This Lambda uses Amazon Simple Email Service, S3, SNS, and the Google Calendar API to parse incoming reservation emails from
Club Locker and manage their associated events in Google Calendars that can be shared with other users.

### Architecture overview

1. Club Locker sends the user an email.
2. The user forwards that email to SES.
3. SES stores the email in S3 for the Lambda to process.
4. The Lambda updates the user's Google Calendar.
5. The Lambda sends a notification indicating whether it succeeded or failed.

Provision another user with:

```shell
./gradlew :infra:provisionUser \
    -PforwardedRecipient=receiver@example.com \
    -PshareWith=editor@example.com,another-editor@example.com
```

## Automatic Reservation Maker

The motivation for this Lambda was that Club Locker opens slots for reservations one week in advance at midnight. Since
booking slots during "prime time" (5-8 pm on weekdays) is competitive, most slots are gone before 9 am the morning-of one
week before. To get good times I had to stay awake until 12:01 am every day when I wanted to make a reservation a week in
advance.

This Lambda is triggered by EventBridge schedules to automatically make reservations based on a schedule file on S3. Right
now it just tries to book at preferred times on preferred courts, but I will hopefully extend it to be able to make
reservations with other players or at configurable times if, say, I want to play at 6:45 instead of 6:00 on some day. I'll
write some sort of cleaner interface rather than manually upload a new schedule to S3 to manage these preferences, perhaps
another Lambda reacting to emails.

Separate daylight and standard time schedules run the Lambda at the same local time in Boston throughout the year. See
[TimeFilter](app/src/main/kotlin/com/parmet/squashlambdas/reserve/TimeFilter.kt) and
[TimeFilterTest](app/src/test/kotlin/com/parmet/squashlambdas/reserve/TimeFilterTest.kt) for examples.

I deduced the necessary US Squash REST API by examining the behavior of the web interface in the Chrome debugger.

## Reservation Monitor

The motivation for this Lambda was the desire of a talented squash player friend of mine to have some of the convenience of
my code available without the supposed "moral cost" of eagerly booking reservations at midnight. He and another skilled
player often pick up slots by watching the reservations page throughout the day and swooping in to take slots others have
dropped. This happens frequently enough to sate their squash-playing hunger. I offered to help automate this process in
exchange for an occasional lesson.

A listener API would be ideal here but I can still emulate their work by polling and maintaining state myself. This Lambda
runs every five minutes on weekday afternoons and sends a notification with details of newly opened prime-time slots on good
courts. DynamoDB records reported slots and expires them through TTL so a slot can be reported again after its suppression
window.

Future directions for this Lambda are to send a text message that my friend can reply to and have the reservation maker book
the slot for him automatically without requiring any extra action from him. This requires two-way SMS messaging, so I've
postponed it for now. Some sort of temporary unsubscription API, such as a "Not today" response text message, would be
another nice extension requiring two-way SMS.

## Comments

Parsing email is a bit of a pain, as is daylight saving time.

## Build and deploy

Deploy Terraform and the Lambdas with `./gradlew :infra:deploy`.
