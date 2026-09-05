variable "aws_region" {
  description = "AWS region containing the application infrastructure."
  type        = string
  default     = "us-east-1"
}

variable "resource_names" {
  description = "Private deployed resource names supplied through SSM Parameter Store."
  sensitive   = true

  type = object({
    bucket = string
    functions = object({
      email_parsers = map(string)
      monitor       = string
      reservation   = string
    })
    schedules = object({
      monitor              = string
      reservation_daylight = string
      reservation_standard = string
    })
    ses_receipt_rules = map(object({
      name  = string
      after = optional(string)
    }))
    ses_receipt_rule_set = string
    table                = string
    topics = object({
      notifications = string
      public        = string
    })
  })
}

variable "private_config" {
  description = "Private runtime configuration supplied through SSM Parameter Store."
  sensitive   = true

  type = object({
    club_locker_email               = string
    club_locker_name                = string
    google_calendar_credentials_key = string
    email_tenants = map(object({
      google_calendar_id      = string
      inbound_email_prefix    = string
      inbound_recipients      = list(string)
      parse_primary_recipient = string
      token_key               = string
    }))
    reservation_courts_key        = string
    reservation_schedule_key      = string
    reservation_times_key         = string
    token_key                     = string
    token_update_expected_sender  = string
    token_update_expected_subject = string
  })
}
