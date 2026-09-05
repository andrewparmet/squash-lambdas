resource "aws_cloudwatch_log_group" "lambda" {
  for_each = local.function_keys

  name              = "/aws/lambda/${local.function_names[each.key]}"
  retention_in_days = 30

  lifecycle {
    prevent_destroy = true
  }
}

resource "aws_lambda_function" "application" {
  for_each = local.function_keys

  function_name    = local.function_names[each.key]
  filename         = "${path.module}/../app/build/libs/squash-lambdas-all.jar"
  source_code_hash = filebase64sha256("${path.module}/../app/build/libs/squash-lambdas-all.jar")
  handler          = local.handlers[local.function_kinds[each.key]]
  role             = aws_iam_role.lambda[each.key].arn
  runtime          = "java25"
  architectures    = ["arm64"]
  memory_size      = 512
  timeout          = 45
  publish          = true

  environment {
    variables = local.lambda_environment[each.key]
  }

  snap_start {
    apply_on = "PublishedVersions"
  }

  depends_on = [
    aws_cloudwatch_log_group.lambda,
    aws_iam_role_policy.lambda,
    aws_s3_object.email_routing_config,
  ]
}

resource "terraform_data" "await_snap_start" {
  for_each = local.function_keys

  triggers_replace = [aws_lambda_function.application[each.key].version]

  provisioner "local-exec" {
    command = "java -cp \"${abspath("${path.module}/build/libs/squash-infra-all.jar")}\" com.parmet.squashlambdas.infra.WaitForSnapStartKt"

    environment = {
      AWS_REGION                 = var.aws_region
      FUNCTION_LABEL             = each.key
      FUNCTION_NAME              = aws_lambda_function.application[each.key].function_name
      FUNCTION_VERSION           = aws_lambda_function.application[each.key].version
      SNAPSTART_DIAGNOSTICS_FILE = abspath("${path.module}/build/snapstart-diagnostics.log")
    }
  }
}

resource "aws_lambda_alias" "live" {
  for_each = local.function_keys

  name             = "live"
  description      = "Managed by Terraform"
  function_name    = aws_lambda_function.application[each.key].function_name
  function_version = aws_lambda_function.application[each.key].version

  depends_on = [terraform_data.await_snap_start]
}
