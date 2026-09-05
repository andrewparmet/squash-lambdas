# Infrastructure

Terraform manages the application AWS resources and Lambda release lifecycle. Run `./gradlew :infra:deploy` to deploy.

Private configuration and backend settings come from an encrypted SSM parameter.

For an urgent rollback, update the affected `live` alias to a known-good version, revert the application change, and publish
again. A later apply otherwise restores the version selected by the current source.
