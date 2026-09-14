#!/usr/bin/env bash
set -euo pipefail

mkdir -p ci-logs
./gradlew :app:connectedDebugAndroidTest --stacktrace 2>&1 \
    | tee ci-logs/instrumentation.log
