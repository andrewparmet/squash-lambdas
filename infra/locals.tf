locals {
  function_keys        = toset(["email_parser", "monitor", "reservation"])
  inbound_email_prefix = "${trimsuffix(var.private_config.inbound_email_prefix, "/")}/"

  handlers = {
    email_parser = "com.parmet.squashlambdas.EmailNotificationHandler::handleRequest"
    monitor      = "com.parmet.squashlambdas.MonitorSlotsHandler::handleRequest"
    reservation  = "com.parmet.squashlambdas.MakeReservationHandler::handleRequest"
  }

  schedule_expressions = {
    monitor              = "cron(0/5 13-22 ? * MON-FRI *)"
    reservation_daylight = "cron(32 04 * * ? *)"
    reservation_standard = "cron(32 05 * * ? *)"
  }

  schedule_targets = {
    monitor              = "monitor"
    reservation_daylight = "reservation"
    reservation_standard = "reservation"
  }

  lambda_environment = {
    email_parser = {
      CLUB_LOCKER_TOKEN_BUCKET      = aws_s3_bucket.application.bucket
      CLUB_LOCKER_TOKEN_KEY         = var.private_config.token_key
      GOOGLE_CALENDAR_ID            = var.private_config.google_calendar_id
      GOOGLE_CAL_CREDS_BUCKET       = aws_s3_bucket.application.bucket
      GOOGLE_CAL_CREDS_KEY          = var.private_config.google_calendar_credentials_key
      MY_TOPIC_ARN                  = aws_sns_topic.notifications.arn
      PARSE_PRIMARY_RECIPIENT       = var.private_config.parse_primary_recipient
      TOKEN_UPDATE_EXPECTED_SENDER  = var.private_config.token_update_expected_sender
      TOKEN_UPDATE_EXPECTED_SUBJECT = var.private_config.token_update_expected_subject
    }
    monitor = {
      CLUB_LOCKER_EMAIL        = var.private_config.club_locker_email
      CLUB_LOCKER_TOKEN_BUCKET = aws_s3_bucket.application.bucket
      CLUB_LOCKER_TOKEN_KEY    = var.private_config.token_key
      MY_TOPIC_ARN             = aws_sns_topic.notifications.arn
      PUBLIC_TOPIC_ARN         = aws_sns_topic.public.arn
      SLOTS_MONITORING_TABLE   = aws_dynamodb_table.slots.name
    }
    reservation = {
      CLUB_LOCKER_EMAIL        = var.private_config.club_locker_email
      CLUB_LOCKER_NAME         = var.private_config.club_locker_name
      CLUB_LOCKER_TOKEN_BUCKET = aws_s3_bucket.application.bucket
      CLUB_LOCKER_TOKEN_KEY    = var.private_config.token_key
      MY_TOPIC_ARN             = aws_sns_topic.notifications.arn
      RESERVATION_BUCKET       = aws_s3_bucket.application.bucket
      RESERVATION_COURTS_KEY   = var.private_config.reservation_courts_key
      RESERVATION_SCHEDULE_KEY = var.private_config.reservation_schedule_key
      RESERVATION_TIMES_KEY    = var.private_config.reservation_times_key
    }
  }

  s3_read_resources = {
    email_parser = [
      "${aws_s3_bucket.application.arn}/${var.private_config.google_calendar_credentials_key}",
      "${aws_s3_bucket.application.arn}/${local.inbound_email_prefix}*",
    ]
    monitor = [
      "${aws_s3_bucket.application.arn}/${var.private_config.token_key}",
    ]
    reservation = [
      "${aws_s3_bucket.application.arn}/${var.private_config.reservation_courts_key}",
      "${aws_s3_bucket.application.arn}/${var.private_config.reservation_schedule_key}",
      "${aws_s3_bucket.application.arn}/${var.private_config.reservation_times_key}",
      "${aws_s3_bucket.application.arn}/${var.private_config.token_key}",
    ]
  }
}
