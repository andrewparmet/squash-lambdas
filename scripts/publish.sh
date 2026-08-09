#!/usr/bin/env bash

set -euo pipefail

readonly aws_profile="personal"
readonly aws_region="us-east-1"
readonly alias_name="live"
readonly script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
readonly repository_dir="$(cd -- "$script_dir/.." && pwd)"
readonly jar_file="$repository_dir/build/libs/squash-lambdas-all.jar"
readonly functions=(
    "club-locker-parse-email"
    "club-locker-monitor-slots"
    "club-locker-auto-reserve"
)

command -v aws >/dev/null || { echo "AWS CLI is required" >&2; exit 1; }

echo "Authenticating AWS profile $aws_profile in $aws_region"
aws login --profile "$aws_profile" --region "$aws_region"
aws sts get-caller-identity --profile "$aws_profile" --region "$aws_region"

cd "$repository_dir"

if [[ -n "$(git status --porcelain)" ]]; then
    echo "Refusing to publish from a dirty working tree" >&2
    exit 1
fi

./gradlew clean spotlessCheck test shadowJar

wait_for_snapshot() {
    local function_name="$1"
    local version="$2"
    local function_state
    local optimization_state
    local state_reason

    for _ in {1..180}; do
        read -r function_state optimization_state <<< "$(
            aws lambda get-function-configuration \
                --function-name "$function_name:$version" \
                --region "$aws_region" \
                --profile "$aws_profile" \
                --query '[State,SnapStart.OptimizationStatus]' \
                --output text
        )"

        echo "$function_name:$version state=$function_state snapstart=$optimization_state"

        if [[ "$function_state" == "Active" && "$optimization_state" == "On" ]]; then
            return
        fi

        if [[ "$function_state" == "Failed" ]]; then
            state_reason="$(
                aws lambda get-function-configuration \
                    --function-name "$function_name:$version" \
                    --region "$aws_region" \
                    --profile "$aws_profile" \
                    --query StateReason \
                    --output text
            )"
            echo "Snapshot creation failed for $function_name:$version: $state_reason" >&2
            exit 1
        fi

        sleep 5
    done

    echo "Timed out waiting for the $function_name:$version snapshot" >&2
    exit 1
}

publish_function() {
    local function_name="$1"
    local snap_start_setting
    local version

    snap_start_setting="$(
        aws lambda get-function-configuration \
            --function-name "$function_name" \
            --region "$aws_region" \
            --profile "$aws_profile" \
            --query SnapStart.ApplyOn \
            --output text
    )"

    if [[ "$snap_start_setting" != "PublishedVersions" ]]; then
        aws lambda update-function-configuration \
            --function-name "$function_name" \
            --snap-start ApplyOn=PublishedVersions \
            --region "$aws_region" \
            --profile "$aws_profile" \
            >/dev/null

        aws lambda wait function-updated-v2 \
            --function-name "$function_name" \
            --region "$aws_region" \
            --profile "$aws_profile"
    fi

    echo "Uploading $jar_file to $function_name"
    aws lambda update-function-code \
        --function-name "$function_name" \
        --zip-file "fileb://$jar_file" \
        --region "$aws_region" \
        --profile "$aws_profile" \
        >/dev/null

    aws lambda wait function-updated-v2 \
        --function-name "$function_name" \
        --region "$aws_region" \
        --profile "$aws_profile"

    version="$(
        aws lambda publish-version \
            --function-name "$function_name" \
            --description "Git $(git rev-parse --short HEAD)" \
            --region "$aws_region" \
            --profile "$aws_profile" \
            --query Version \
            --output text
    )"

    echo "Published $function_name:$version"
    wait_for_snapshot "$function_name" "$version"

    if aws lambda get-alias \
        --function-name "$function_name" \
        --name "$alias_name" \
        --region "$aws_region" \
        --profile "$aws_profile" \
        >/dev/null 2>&1; then
        aws lambda update-alias \
            --function-name "$function_name" \
            --name "$alias_name" \
            --function-version "$version" \
            --region "$aws_region" \
            --profile "$aws_profile" \
            >/dev/null
    else
        aws lambda create-alias \
            --function-name "$function_name" \
            --name "$alias_name" \
            --function-version "$version" \
            --region "$aws_region" \
            --profile "$aws_profile" \
            >/dev/null
    fi

    echo "Updated $function_name:$alias_name to version $version"
}

for function_name in "${functions[@]}"; do
    publish_function "$function_name"
done
