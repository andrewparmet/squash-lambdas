locals {
  shared_function_keys = toset(["monitor", "reservation"])
  email_tenants        = toset(keys(nonsensitive(var.private_config.email_tenants)))
  email_function_keys  = toset([for tenant in local.email_tenants : "email_parser:${tenant}"])
  function_keys        = setunion(local.shared_function_keys, local.email_function_keys)
  email_functions = {
    for tenant in local.email_tenants : "email_parser:${tenant}" => var.resource_names.functions.email_parsers[tenant]
  }
  function_names = merge(
    {
      for function_key in local.shared_function_keys : function_key => var.resource_names.functions[function_key]
    },
    local.email_functions
  )
  function_tenants = {
    for tenant in local.email_tenants : "email_parser:${tenant}" => tenant
  }

  handlers = {
    email_parser = "com.parmet.squashlambdas.EmailNotificationHandler::handleRequest"
    monitor      = "com.parmet.squashlambdas.MonitorSlotsHandler::handleRequest"
    reservation  = "com.parmet.squashlambdas.MakeReservationHandler::handleRequest"
  }

  function_kinds = {
    for function_key in local.function_keys : function_key => startswith(function_key, "email_parser:") ? "email_parser" : function_key
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

  lambda_environment = merge(
    {
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
    },
    {
      for function_key, function_name in local.email_functions : function_key => {
        CLUB_LOCKER_TOKEN_BUCKET      = aws_s3_bucket.application.bucket
        CLUB_LOCKER_TOKEN_KEY         = var.private_config.email_tenants[local.function_tenants[function_key]].token_key
        GOOGLE_CALENDAR_ID            = var.private_config.email_tenants[local.function_tenants[function_key]].google_calendar_id
        GOOGLE_CAL_CREDS_BUCKET       = aws_s3_bucket.application.bucket
        GOOGLE_CAL_CREDS_KEY          = var.private_config.google_calendar_credentials_key
        MY_TOPIC_ARN                  = aws_sns_topic.notifications.arn
        PARSE_PRIMARY_RECIPIENT       = var.private_config.email_tenants[local.function_tenants[function_key]].parse_primary_recipient
        TOKEN_UPDATE_EXPECTED_SENDER  = var.private_config.token_update_expected_sender
        TOKEN_UPDATE_EXPECTED_SUBJECT = var.private_config.token_update_expected_subject
      }
    }
  )

  s3_read_resources = merge(
    {
      monitor = [
        "${aws_s3_bucket.application.arn}/${var.private_config.token_key}",
      ]
      reservation = [
        "${aws_s3_bucket.application.arn}/${var.private_config.reservation_courts_key}",
        "${aws_s3_bucket.application.arn}/${var.private_config.reservation_schedule_key}",
        "${aws_s3_bucket.application.arn}/${var.private_config.reservation_times_key}",
        "${aws_s3_bucket.application.arn}/${var.private_config.token_key}",
      ]
    },
    {
      for function_key, function_name in local.email_functions : function_key => [
        "${aws_s3_bucket.application.arn}/${trimsuffix(var.private_config.email_tenants[local.function_tenants[function_key]].inbound_email_prefix, "/")}/*",
        "${aws_s3_bucket.application.arn}/${var.private_config.google_calendar_credentials_key}",
        "${aws_s3_bucket.application.arn}/${var.private_config.email_tenants[local.function_tenants[function_key]].token_key}",
      ]
    }
  )
}
