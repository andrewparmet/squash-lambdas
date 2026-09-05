resource "aws_s3_bucket" "application" {
  bucket = var.resource_names.bucket

  lifecycle {
    prevent_destroy = true
  }
}

resource "aws_s3_bucket_versioning" "application" {
  bucket = aws_s3_bucket.application.id

  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "application" {
  bucket = aws_s3_bucket.application.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_public_access_block" "application" {
  bucket = aws_s3_bucket.application.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_lifecycle_configuration" "application" {
  bucket = aws_s3_bucket.application.id

  dynamic "rule" {
    for_each = local.email_tenants

    content {
      id     = "expire-inbound-email-${rule.key}"
      status = "Enabled"

      filter {
        prefix = "${trimsuffix(var.private_config.email_tenants[rule.key].inbound_email_prefix, "/")}/"
      }

      expiration {
        days = 30
      }

      noncurrent_version_expiration {
        noncurrent_days = 30
      }
    }
  }

  rule {
    id     = "expire-token-history"
    status = "Enabled"

    filter {
      prefix = var.private_config.token_key
    }

    noncurrent_version_expiration {
      noncurrent_days = 30
    }
  }
}

resource "aws_s3_object" "email_routing_config" {
  bucket                 = aws_s3_bucket.application.id
  key                    = local.email_config_key
  content                = jsonencode(local.email_routing_config)
  server_side_encryption = "AES256"
}

resource "aws_dynamodb_table" "slots" {
  name                        = var.resource_names.table
  billing_mode                = "PROVISIONED"
  read_capacity               = 5
  write_capacity              = 5
  hash_key                    = "filename"
  deletion_protection_enabled = true

  attribute {
    name = "filename"
    type = "S"
  }

  ttl {
    attribute_name = "ttl"
    enabled        = true
  }

  lifecycle {
    prevent_destroy = true
  }
}
