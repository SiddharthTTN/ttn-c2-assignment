#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

cd "$ROOT"
make install
cd "$ROOT/backend"
./mvnw -q -DskipTests dependency:go-offline
