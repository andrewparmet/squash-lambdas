# squash_lambdas

This project contains two AWS Lambdas. Build with `./gradlew stage`.

## Email Parser
The motivation for this Lambda was that Club Locker provides a good interface for booking and managing reservations for the various court sports at the Tennis and Racquet Club.  It fails to integrate with Google Calendar, and I don't want to be bothered to manually create events each time I make a reservation.

This Lambda uses Amazon Simple Email Service, S3, SNS, and the Google Calendar API to parse incoming reservation emails from Club Locker and manage their associated events in a Google Calendar that can then be shared with other users (such as a personal email account).

The Lambda must have S3 read permission, SNS publish permission, and a Google service account with Google Calendar permission.

### Implementation

0.  I have configured an Amazon SES inbox that receives emails.

1.  I automatically forward all emails from Club Locker to the configured SES inbox.

2.  Upon receiving an email SES copies the email content to an S3 bucket.

3.  The S3 bucket has a Lambda that triggers on object creation.

4.  The Lambda downloads the newly written object, parses the email content, and updates Google Calendar accordingly.

5.  The Lambda sends me a happy email if it succeeds, or one with the error details and some context if it fails.

## Automatic Reservation Maker
The motivation for this Lambda was that Club Locker opens slots for reservations one week in advance at midnight. Since booking slots during "prime time" (5-8 pm on week days) is competitive, most slots are gone before 9 am the morning-of one week before. To get good times I had to stay awake until 12:01 am every day when I wanted to play in a week.

This Lambda is triggered by a cron-based scheduler in CloudWatch Events to run at 12:01 am (or 1:01 am during EST) and automatically make reservations for me based on a schedule file on S3. Right now it just tries to book at preferred times on preferred courts, but I will hopefully extend it to be able to make reservations with other players or at configurable times if, say, I want to play at 6:45 instead of 6:00 on some day. I'll have to write some sort of cleaner interface rather than manually upload a new schedule to S3 to manage these preferences - perhaps another Lambda reacting to emails.

## Comments

Parsing email is a bit of a pain.
