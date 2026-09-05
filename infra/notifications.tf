resource "aws_sns_topic" "notifications" {
  name              = var.resource_names.topics.notifications
  kms_master_key_id = "alias/aws/sns"

  lifecycle {
    prevent_destroy = true
  }
}

resource "aws_sns_topic" "public" {
  name              = var.resource_names.topics.public
  kms_master_key_id = "alias/aws/sns"

  lifecycle {
    prevent_destroy = true
  }
}

data "aws_iam_policy_document" "ses_bucket" {
  statement {
    sid     = "AllowSesReceiptRuleSet"
    actions = ["s3:PutObject"]
    resources = [
      for tenant in values(var.private_config.email_tenants) :
      "${aws_s3_bucket.application.arn}/${trimsuffix(tenant.inbound_email_prefix, "/")}/*"
    ]

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
  tls_policy    = "Require"
  recipients    = var.private_config.email_tenants[each.key].inbound_recipients

  s3_action {
    bucket_name       = aws_s3_bucket.application.bucket
    object_key_prefix = "${trimsuffix(var.private_config.email_tenants[each.key].inbound_email_prefix, "/")}/"
    position          = 1
  }

  lambda_action {
    function_arn    = aws_lambda_alias.live["email_parser"].arn
    invocation_type = "Event"
    position        = 2
  }

  depends_on = [aws_lambda_permission.email_receipt, aws_s3_bucket_policy.application]
}

resource "aws_lambda_permission" "email_receipt" {
  for_each = local.email_tenants

  statement_id   = "AllowExecutionFromSes-${each.key}"
  action         = "lambda:InvokeFunction"
  function_name  = aws_lambda_function.application["email_parser"].function_name
  qualifier      = aws_lambda_alias.live["email_parser"].name
  principal      = "ses.amazonaws.com"
  source_arn     = "arn:${data.aws_partition.current.partition}:ses:${var.aws_region}:${data.aws_caller_identity.current.account_id}:receipt-rule-set/${var.resource_names.ses_receipt_rule_set}:receipt-rule/${var.resource_names.ses_receipt_rules[each.key].name}"
  source_account = data.aws_caller_identity.current.account_id
}
