resource "aws_sns_topic" "notifications" {
  name = var.resource_names.topics.notifications

  lifecycle {
    prevent_destroy = true
  }
}

resource "aws_sns_topic" "public" {
  name = var.resource_names.topics.public

  lifecycle {
    prevent_destroy = true
  }
}

data "aws_iam_policy_document" "ses_bucket" {
  statement {
    sid       = "AllowSesReceiptRuleSet"
    actions   = ["s3:PutObject"]
    resources = ["${aws_s3_bucket.application.arn}/*"]

    principals {
      type        = "Service"
      identifiers = ["ses.amazonaws.com"]
    }

    condition {
      test     = "StringEquals"
      variable = "AWS:SourceAccount"
      values   = [data.aws_caller_identity.current.account_id]
    }

    condition {
      test     = "ArnLike"
      variable = "AWS:SourceArn"
      values = [
        "arn:${data.aws_partition.current.partition}:ses:${var.aws_region}:${data.aws_caller_identity.current.account_id}:receipt-rule-set/${var.resource_names.ses_receipt_rule_set}:receipt-rule/*",
      ]
    }
  }
}

resource "aws_s3_bucket_policy" "application" {
  bucket = aws_s3_bucket.application.id
  policy = data.aws_iam_policy_document.ses_bucket.json
}

resource "aws_ses_receipt_rule" "application" {
  for_each = local.email_tenants

  name          = var.resource_names.ses_receipt_rules[each.key].name
  rule_set_name = var.resource_names.ses_receipt_rule_set
  after         = var.resource_names.ses_receipt_rules[each.key].after
  enabled       = true
  scan_enabled  = true
  tls_policy    = "Optional"
  recipients    = var.private_config.email_tenants[each.key].inbound_recipients

  s3_action {
    bucket_name       = aws_s3_bucket.application.bucket
    object_key_prefix = "${trimsuffix(var.private_config.email_tenants[each.key].inbound_email_prefix, "/")}/"
    position          = 1
  }

  depends_on = [aws_s3_bucket_policy.application]
}

resource "aws_lambda_permission" "email_bucket" {
  for_each = local.email_tenants

  statement_id   = "AllowExecutionFromS3-${each.key}"
  action         = "lambda:InvokeFunction"
  function_name  = aws_lambda_function.application["email_parser:${each.key}"].function_name
  qualifier      = aws_lambda_alias.live["email_parser:${each.key}"].name
  principal      = "s3.amazonaws.com"
  source_arn     = aws_s3_bucket.application.arn
  source_account = data.aws_caller_identity.current.account_id
}

resource "aws_s3_bucket_notification" "email" {
  bucket = aws_s3_bucket.application.id

  dynamic "lambda_function" {
    for_each = local.email_tenants

    content {
      id                  = lambda_function.key
      lambda_function_arn = aws_lambda_alias.live["email_parser:${lambda_function.key}"].arn
      events              = ["s3:ObjectCreated:*"]
      filter_prefix       = "${trimsuffix(var.private_config.email_tenants[lambda_function.key].inbound_email_prefix, "/")}/"
    }
  }

  depends_on = [aws_lambda_permission.email_bucket]
}
