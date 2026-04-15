#!/usr/bin/env bash
# Bump VERSION_CODE and set VERSION_NAME last segment to match (e.g. 1.0.0.3 + code 3 -> 1.0.0.4 + code 4).
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROPS="${ROOT_DIR}/gradle/version.properties"

if [[ ! -f "$PROPS" ]]; then
  echo "bump-version-properties: missing ${PROPS}" >&2
  exit 1
fi

# shellcheck source=/dev/null
while IFS='=' read -r key value; do
  [[ -z "${key// }" || "$key" =~ ^# ]] && continue
  case "$key" in
    VERSION_CODE) VERSION_CODE="$value" ;;
    VERSION_NAME) VERSION_NAME="$value" ;;
  esac
done <"$PROPS"

if [[ -z "${VERSION_CODE:-}" || -z "${VERSION_NAME:-}" ]]; then
  echo "bump-version-properties: VERSION_CODE and VERSION_NAME must be set in ${PROPS}" >&2
  exit 1
fi

if ! [[ "$VERSION_CODE" =~ ^[0-9]+$ ]]; then
  echo "bump-version-properties: VERSION_CODE must be an integer, got: ${VERSION_CODE}" >&2
  exit 1
fi

NEW_CODE=$((VERSION_CODE + 1))
BASE_NAME="${VERSION_NAME%.*}"
NEW_NAME="${BASE_NAME}.${NEW_CODE}"

{
  echo "VERSION_CODE=${NEW_CODE}"
  echo "VERSION_NAME=${NEW_NAME}"
} >"$PROPS"

echo "bump-version-properties: VERSION_CODE ${VERSION_CODE} -> ${NEW_CODE}, VERSION_NAME ${VERSION_NAME} -> ${NEW_NAME}"
