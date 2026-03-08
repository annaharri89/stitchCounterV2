#!/usr/bin/env bash
set -euo pipefail

repo_root="$(git rev-parse --show-toplevel)"

git config core.hooksPath "$repo_root/.githooks"
chmod +x "$repo_root/.githooks/pre-commit"

echo "Git hooks installed. core.hooksPath -> $repo_root/.githooks"
