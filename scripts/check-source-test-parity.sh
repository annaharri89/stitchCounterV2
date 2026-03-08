#!/usr/bin/env bash
set -euo pipefail

if [[ "${ALLOW_NO_TEST_CHANGES:-false}" == "true" ]]; then
  echo "[source-test-parity] Skipped because ALLOW_NO_TEST_CHANGES=true"
  exit 0
fi

BASE_REF="${1:-origin/main}"

if ! git rev-parse --verify "$BASE_REF" >/dev/null 2>&1; then
  echo "[source-test-parity] Base ref '$BASE_REF' not found; skipping check."
  exit 0
fi

changed_files="$(git diff --name-only "$BASE_REF"...HEAD)"

if [[ -z "$changed_files" ]]; then
  echo "[source-test-parity] No changed files."
  exit 0
fi

has_main_kotlin_change="false"
has_test_change="false"

while IFS= read -r file_path; do
  if [[ "$file_path" =~ ^app/src/main/.*\.kt$ ]]; then
    has_main_kotlin_change="true"
  fi

  if [[ "$file_path" =~ ^app/src/test/ ]]; then
    has_test_change="true"
  fi
done <<< "$changed_files"

if [[ "$has_main_kotlin_change" == "true" && "$has_test_change" != "true" ]]; then
  echo "[source-test-parity] Kotlin source changed in app/src/main but no app/src/test changes were detected."
  echo "[source-test-parity] Add or update unit tests, or set ALLOW_NO_TEST_CHANGES=true for exceptional cases."
  exit 1
fi

echo "[source-test-parity] Passed."
