# Infrastructure

Terraform manages the application AWS resources and Lambda release lifecycle. Run `scripts/publish.sh` to build, test, apply,
wait for SnapStart, update the `live` aliases, and prune old unaliased versions.

Private configuration and backend settings come from an encrypted SSM parameter. The state bucket, bootstrap parameter, and
SNS subscriptions are managed separately. Terraform owns the shared application bucket's notification configuration and SES
write policy.

Run `kotlin scripts/deploy.main.kts cleanup "$PWD"` once to remove dated slot snapshots that predate TTL.

For an urgent rollback, update the affected `live` alias to a known-good version, revert the application change, and publish
again. A later apply otherwise restores the version selected by the current source.
