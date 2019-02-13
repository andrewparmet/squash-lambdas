# squash-lambdas

This project contains two AWS Lambdas. Build with `./gradlew stage`.

## Email Parser
The motivation for this Lambda was that Club Locker provides a good interface for booking and managing reservations for the various court sports at the Tennis and Racquet Club.  It fails to integrate with Google Calendar, and I don't want to be bothered to manually create events each time I make a reservation. Club Locker sends predictable and easily parseable emails that provide an easy integration point for this Lambda.

This Lambda uses Amazon Simple Email Service, S3, SNS, and the Google Calendar API to parse incoming reservation emails from Club Locker and manage their associated events in a Google Calendar that can then be shared with other users (such as a personal email account).

The Lambda must have S3 read permission, SNS publish permission, and a Google service account with Google Calendar permission.

### Implementation

1.  I have configured an Amazon SES inbox that receives emails.
2.  I automatically forward all emails from Club Locker to the configured SES inbox.
3.  Upon receiving an email SES copies the email content to an S3 bucket.
4.  The S3 bucket has a Lambda that triggers on object creation.
5.  The Lambda downloads the newly written object, parses the email content, and updates Google Calendar accordingly.
6.  The Lambda sends me a happy email if it succeeds, or one with the error details and some context if it fails.

## Automatic Reservation Maker
The motivation for this Lambda was that Club Locker opens slots for reservations one week in advance at midnight. Since booking slots during "prime time" (5-8 pm on weekdays) is competitive, most slots are gone before 9 am the morning-of one week before. To get good times I had to stay awake until 12:01 am every day when I wanted to make a reservation a week in advance.

This Lambda is triggered by a cron-based scheduler in CloudWatch Events to automatically make reservations for me based on a schedule file on S3. Right now it just tries to book at preferred times on preferred courts, but I will hopefully extend it to be able to make reservations with other players or at configurable times if, say, I want to play at 6:45 instead of 6:00 on some day. I'll write some sort of cleaner interface rather than manually upload a new schedule to S3 to manage these preferences - perhaps another Lambda reacting to emails.

This Lambda is configured to run at 4:01 and 5:01 GMT, with the code inferring from the current date whether or not it is running during EST or EDT and making a reservation always and only at 12:01 am in Boston. See [TimeFilter](https://github.com/andrewparmet/squash_lambdas/blob/master/src/main/kotlin/com/parmet/squashlambdas/reserve/TimeFilter.kt) and [TimeFilterTest](https://github.com/andrewparmet/squash_lambdas/blob/master/src/test/kotlin/com/parmet/squashlambdas/reserve/TimeFilterTest.kt) for examples.

I deduced the necessary US Squash REST API by examining the behavior of the web interface in the Chrome debugger.

## Comments

Parsing email is a bit of a pain, as is daylight savings time.
