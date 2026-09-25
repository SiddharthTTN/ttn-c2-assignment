#!/usr/bin/env bash
# Stages Vite build output into the backend classpath static directory for JAR packaging.
# Generated content is gitignored; safe to remove after packaging.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DIST="${ROOT}/frontend/dist"
STATIC="${ROOT}/backend/src/main/resources/static"

if [[ ! -d "${DIST}" ]]; then
  echo "error: ${DIST} not found — run frontend build first (make frontend-build)" >&2
  exit 1
fi

rm -rf "${STATIC}"
mkdir -p "${STATIC}"
cp -a "${DIST}/." "${STATIC}/"
echo "Staged frontend dist -> ${STATIC}"
