locals {
  shared_function_keys = toset(["monitor", "reservation"])
  email_tenants        = toset(keys(nonsensitive(var.private_config.email_tenants)))
  function_keys        = setunion(local.shared_function_keys, ["email_parser"])
  email_config_key     = "config/email-routing.json"
  function_names = {
    email_parser = var.resource_names.functions.email_parser
    monitor      = var.resource_names.functions.monitor
    reservation  = var.resource_names.functions.reservation
  }

  handlers = {
    email_parser = "com.parmet.squashlambdas.EmailNotificationHandler::handleRequest"
    monitor      = "com.parmet.squashlambdas.MonitorSlotsHandler::handleRequest"
    reservation  = "com.parmet.squashlambdas.MakeReservationHandler::handleRequest"
  }

  function_kinds = { for function_key in local.function_keys : function_key => function_key }

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
      EMAIL_CONFIG_BUCKET  = aws_s3_bucket.application.bucket
      EMAIL_CONFIG_KEY     = local.email_config_key
      EMAIL_CONFIG_VERSION = sha256(jsonencode(local.email_routing_config))
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
    email_parser = concat(
      [
        "${aws_s3_bucket.application.arn}/${local.email_config_key}",
        "${aws_s3_bucket.application.arn}/${var.private_config.google_calendar_credentials_key}",
      ],
      [for tenant in values(var.private_config.email_tenants) : "${aws_s3_bucket.application.arn}/${trimsuffix(tenant.inbound_email_prefix, "/")}/*"],
      ["${aws_s3_bucket.application.arn}/${var.private_config.token_key}"]
    )
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

  email_routing_config = {
    bucket                       = aws_s3_bucket.application.bucket
    clubLockerEmail              = var.private_config.club_locker_email
    clubLockerTokenKey           = var.private_config.token_key
    googleCalendarCredentialsKey = var.private_config.google_calendar_credentials_key
    notificationTopicArn         = aws_sns_topic.notifications.arn
    calendarExpectedSender       = var.private_config.calendar_expected_sender
    tokenUpdateExpectedSender    = var.private_config.token_update_expected_sender
    tokenUpdateExpectedSubject   = var.private_config.token_update_expected_subject
    tenants = {
      for tenant_id, tenant in var.private_config.email_tenants : tenant_id => {
        googleCalendarId   = tenant.google_calendar_id
        inboundEmailPrefix = tenant.inbound_email_prefix
        inboundRecipients  = tenant.inbound_recipients
        primaryRecipient   = tenant.parse_primary_recipient
      }
    }
  }
}
