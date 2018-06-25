# squash_lambdas

Uses Amazon Simple Email Service, S3, SNS, and the Google Calendar API to parse incoming reservation emails from Club Locker and manage their associated events in a Google Calendar that can then be shared with other users (such as a personal email account).

Build with `gradle stage`.

The lambda must have S3 read permission, SNS publish permission, and a Google service account with Google Calendar permission.

## Implementation

0.  I have configured an Amazon SES inbox that receives emails.

1.  I automatically forward all emails from Club Locker to the configured SES inbox.

2.  Upon receiving an email SES copies the email content to an S3 bucket.

3.  The S3 bucket has a Lambda that triggers on object creation.

4.  The Lambda downloads the newly written object, parses the email content, and updates Google Calendar accordingly.

5.  The Lambda sends me a happy email if it succeeds, or one with the error details and some context if it fails.

## Comments

Parsing email is a bit of a pain.
