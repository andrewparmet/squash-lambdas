output "lambda_alias_arns" {
  description = "Live Lambda alias ARNs."
  sensitive   = true
  value       = { for key, alias in aws_lambda_alias.live : key => alias.arn }
}

output "slots_table_arn" {
  description = "Slot snapshot table ARN."
  sensitive   = true
  value       = aws_dynamodb_table.slots.arn
}
