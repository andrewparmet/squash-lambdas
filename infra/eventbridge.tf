resource "aws_cloudwatch_event_rule" "schedule" {
  for_each = local.schedule_expressions

  name                = var.resource_names.schedules[each.key]
  schedule_expression = each.value
  state               = "ENABLED"

  lifecycle {
    ignore_changes = [description]
  }
}

resource "aws_cloudwatch_event_target" "schedule" {
  for_each = local.schedule_expressions

  rule = aws_cloudwatch_event_rule.schedule[each.key].name
  arn  = aws_lambda_alias.live[local.schedule_targets[each.key]].arn
}

resource "aws_lambda_permission" "schedule" {
  for_each = local.schedule_expressions

  statement_id  = "AllowExecutionFromEventBridge-${each.key}"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.application[local.schedule_targets[each.key]].function_name
  qualifier     = aws_lambda_alias.live[local.schedule_targets[each.key]].name
  principal     = "events.amazonaws.com"
  source_arn    = aws_cloudwatch_event_rule.schedule[each.key].arn
}
