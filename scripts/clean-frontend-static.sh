#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
STATIC="${ROOT}/backend/src/main/resources/static"
rm -rf "${STATIC}"
echo "Removed staged static resources (if any)"
