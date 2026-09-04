#!/usr/bin/env bash
set -euo pipefail

readonly PROJECT_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
readonly HYGIENE_SCRIPT="$PROJECT_ROOT/scripts/verify_repository_hygiene.sh"
readonly TEMP_ROOT="$(mktemp -d "${TMPDIR:-/tmp}/verify_repository_hygiene.XXXXXX")"

case_directory=""
tests_run=0

cleanup() {
    rm -rf "$TEMP_ROOT"
}

trap cleanup EXIT

fail() {
    printf 'FAIL: %s\n' "$1" >&2
    exit 1
}

create_case() {
    case_directory="$(mktemp -d "$TEMP_ROOT/case.XXXXXX")"
    mkdir -p "$case_directory/scripts" "$case_directory/app/src/main/java"
    cp "$HYGIENE_SCRIPT" "$case_directory/scripts/verify_repository_hygiene.sh"
    chmod +x "$case_directory/scripts/verify_repository_hygiene.sh"
    (
        cd "$case_directory"
        git init -q
        git add scripts/verify_repository_hygiene.sh
    )
}

track_file() {
    local relative_path="$1"
    local contents="$2"

    mkdir -p "$case_directory/$(dirname "$relative_path")"
    printf '%s' "$contents" > "$case_directory/$relative_path"
    (
        cd "$case_directory"
        git add -f "$relative_path"
    )
}

assert_hygiene_result() {
    local name="$1"
    local expected_result="$2"
    local status

    if (cd "$case_directory" && bash scripts/verify_repository_hygiene.sh) > "$case_directory/output" 2>&1; then
        status=0
    else
        status=$?
    fi

    tests_run=$((tests_run + 1))
    if [[ "$expected_result" == "pass" && "$status" -ne 0 ]]; then
        cat "$case_directory/output" >&2
        fail "$name should pass"
    fi
    if [[ "$expected_result" == "fail" && "$status" -eq 0 ]]; then
        cat "$case_directory/output" >&2
        fail "$name should fail"
    fi
    printf 'PASS: %s\n' "$name"
}

assert_hygiene_result_without_ripgrep() {
    local status

    if (cd "$case_directory" && PATH=/usr/bin:/bin bash scripts/verify_repository_hygiene.sh) \
        > "$case_directory/output" 2>&1; then
        status=0
    else
        status=$?
    fi

    tests_run=$((tests_run + 1))
    if [[ "$status" -ne 0 ]]; then
        cat "$case_directory/output" >&2
        fail "repository hygiene should not require ripgrep"
    fi
    printf 'PASS: repository hygiene without ripgrep\n'
}

create_case
track_file "app/src/main/java/Fixture.kt" $'package fixture\nval safe = "clean"\n'
assert_hygiene_result_without_ripgrep

create_case
track_file "app/src/main/java/Fixture.java" $'package fixture;\n/* outer /* inner */ TODO("code after Java comment"); */\n'
assert_hygiene_result "Java block comments end at first terminator" "fail"

create_case
track_file "app/src/main/java/Fixture.kt" $'package fixture\n// TODO(\n/* println( */\n/* outer\n   /* inner */\n   TODO("documentation")\n*/\nval ordinary = "TODO("\nval raw = """printStackTrace("""\nval normalEscaped = "\\${TODO(\\"literal\\")}"\nval rawEscaped = """${\047$\047}{TODO("literal")}"""\n'
assert_hygiene_result "string and comment lookalikes" "pass"

create_case
track_file "app/src/main/java/Fixture.kt" $'package fixture\nfun normalCalls() {\n    TODO("pending")\n    println("debug")\n    Exception().printStackTrace()\n}\n'
assert_hygiene_result "normal executable calls" "fail"

create_case
track_file "app/src/main/java/Fixture.kt" $'package fixture\nval standardTemplate = "Status: ${TODO("pending")}"\nval rawTemplate = """Status: ${println("debug")}"""\nval nestedTemplate = "Status: ${run { Exception().printStackTrace() }}"\n'
assert_hygiene_result "standard raw and nested templates" "fail"

create_case
track_file "app/src/debug/res/values/google_maps_api.xml" "<resources/>\n"
track_file "app/src/debug/res/values/admob_key.xml" "<resources/>\n"
track_file "app/src/debug/res/values/ads_key_ids.xml" "<resources/>\n"
assert_hygiene_result "tracked credential names" "fail"

create_case
track_file "app/release/output-metadata.json" "{}\n"
assert_hygiene_result "tracked release metadata" "fail"

create_case
track_file "app/schemas/com.example.Database/6.json" "{}\n"
track_file "app/src/main/java/Fixture.kt" $'package fixture\nval safe = "schema fixture"\n'
assert_hygiene_result "tracked Room schema" "pass"

printf 'All %s hygiene fixture cases passed.\n' "$tests_run"
