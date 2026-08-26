#!/usr/bin/env bash
set -euo pipefail

readonly SOURCE_DIRECTORY="app/src/main/java"
failures=0

report_failure() {
    printf 'Repository hygiene failure: %s\n' "$1" >&2
    printf '%s\n' "$2" >&2
    failures=1
}

check_tracked_files() {
    local matches

    if matches=$(git ls-files | grep -E '(^|/)(google-services\.json|keystore\.properties|[^/]*(service[-_]?account|serviceaccount)[^/]*\.json|benchmarkData\.json)$|(\.(jks|keystore|p12|apk|aab|apks|dm|trace|perfetto-trace))$'); then
        report_failure "tracked secret or generated artifact" "$matches"
    fi
}

check_executable_source() {
    local matches

    # Only full-line comments are exempt; stale code inside block comments remains visible.
    matches=$(rg -n --glob '*.kt' --glob '*.java' 'TODO\(|println\(|printStackTrace\(' "$SOURCE_DIRECTORY" \
        | grep -Ev ':[0-9]+:[[:space:]]*(//|/\*|\*|\*/)' || true)
    if [[ -n "$matches" ]]; then
        report_failure "executable TODO, println, or printStackTrace" "$matches"
    fi
}

check_verbose_network_logging() {
    local matches

    # Existing INFO-level diagnostics are intentionally deferred to Task 3.
    matches=$(rg -n --glob '*.kt' --glob '*.java' 'LogLevel\.(ALL|BODY|HEADERS)|HttpLoggingInterceptor\.Level\.(BODY|HEADERS)' "$SOURCE_DIRECTORY" || true)
    if [[ -n "$matches" ]]; then
        report_failure "unapproved verbose network logging" "$matches"
    fi
}

check_tracked_files
check_executable_source
check_verbose_network_logging

if (( failures != 0 )); then
    exit 1
fi

printf 'Repository hygiene verification passed.\n'
