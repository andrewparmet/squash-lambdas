data "aws_iam_policy_document" "lambda_assume_role" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "lambda" {
  for_each = local.function_keys

  name               = "${var.resource_names.functions[each.key]}-execution"
  assume_role_policy = data.aws_iam_policy_document.lambda_assume_role.json
}

data "aws_iam_policy_document" "lambda" {
  for_each = local.function_keys

  statement {
    actions = [
      "logs:CreateLogStream",
      "logs:PutLogEvents",
    ]
    resources = [
      "arn:${data.aws_partition.current.partition}:logs:${var.aws_region}:${data.aws_caller_identity.current.account_id}:log-group:/aws/lambda/${var.resource_names.functions[each.key]}:*",
    ]
  }

  statement {
    actions   = ["sns:Publish"]
    resources = each.key == "monitor" ? [aws_sns_topic.notifications.arn, aws_sns_topic.public.arn] : [aws_sns_topic.notifications.arn]
  }

  dynamic "statement" {
    for_each = each.key == "monitor" ? [1] : []

    content {
      actions = [
        "dynamodb:GetItem",
        "dynamodb:PutItem",
      ]
      resources = [aws_dynamodb_table.slots.arn]
    }
  }

  statement {
    actions   = ["s3:GetObject"]
    resources = local.s3_read_resources[each.key]
  }

  dynamic "statement" {
    for_each = each.key == "email_parser" || each.key == "monitor" ? [1] : []

    content {
      actions   = ["s3:PutObject"]
      resources = ["${aws_s3_bucket.application.arn}/${var.private_config.token_key}"]
    }
  }
}

resource "aws_iam_role_policy" "lambda" {
  for_each = local.function_keys

  name   = "application-access"
  role   = aws_iam_role.lambda[each.key].id
  policy = data.aws_iam_policy_document.lambda[each.key].json
}
