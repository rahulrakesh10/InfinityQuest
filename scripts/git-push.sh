#!/bin/bash

set -euo pipefail

if [[ ! -d .git ]]; then
  echo "Error: run this script from the project root (where the .git folder lives)." >&2
  exit 1
fi

default_message="updated code and fixes"
commit_message="${1:-$default_message}"

echo "Adding all tracked and untracked files..."
git add -A

echo "Committing with message: \"$commit_message\""
if git diff --cached --quiet; then
  echo "No changes to commit."
else
  git commit -m "$commit_message"
fi

echo "Pushing to origin $(git rev-parse --abbrev-ref HEAD)..."
git push

echo "Done."

