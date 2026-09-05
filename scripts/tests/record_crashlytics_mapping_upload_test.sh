#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
readonly PROJECT_ROOT
readonly SCRIPT="$PROJECT_ROOT/scripts/record_crashlytics_mapping_upload.sh"
TEST_ROOT="$(mktemp -d "${TMPDIR:-/tmp}/mapping_upload_receipt_test.XXXXXX")"
readonly TEST_ROOT

cleanup() {
    chmod -R u+w "$TEST_ROOT" 2>/dev/null || true
    rm -rf "$TEST_ROOT"
}
trap cleanup EXIT

fail() {
    printf 'FAIL: %s\n' "$1" >&2
    exit 1
}

create_fixture() {
    local directory="$1"
    local requested="$2"
    mkdir -p "$directory"
    printf 'aab\n' > "$directory/earthquake-watchdog-release.aab"
    printf 'mapping\n' > "$directory/mapping.txt"
    python3 - "$directory" "$requested" <<'PY'
import hashlib
import json
import sys
from pathlib import Path

directory = Path(sys.argv[1])
requested = sys.argv[2] == "true"
mapping_hash = hashlib.sha256((directory / "mapping.txt").read_bytes()).hexdigest()
document = {
    "schemaVersion": 1,
    "source": {"commitSha": "a" * 40, "ref": "refs/tags/v3.0.0"},
    "verification": {
        "crashlyticsMappingUpload": {
            "requested": requested,
            "completedAtVerification": False,
        }
    },
    "artifacts": {"mapping.txt": mapping_hash},
}
(directory / "release-provenance.json").write_text(
    json.dumps(document, indent=2) + "\n", encoding="utf-8"
)
with (directory / "SHA256SUMS").open("w", encoding="utf-8") as checksums:
    for path in sorted(directory.iterdir()):
        if path.name == "SHA256SUMS":
            continue
        checksums.write(f"{hashlib.sha256(path.read_bytes()).hexdigest()}  {path.name}\n")
PY
}

[[ -x "$SCRIPT" ]] || fail "mapping upload receipt script is missing or not executable"

readonly VALID="$TEST_ROOT/valid"
create_fixture "$VALID" true
"$SCRIPT" \
    --verified-dir "$VALID" \
    --uploaded-mapping "$VALID/mapping.txt" \
    --firebase-project-id earthquake-task9-test \
    --firebase-app-id 1:123456789:android:abcdef \
    --github-run-id 12345 \
    --github-run-attempt 2
[[ -s "$VALID/crashlytics-mapping-upload-receipt.json" ]] || fail "successful upload receipt is missing"
(
    cd "$VALID"
    shasum -a 256 -c SHA256SUMS >/dev/null
)
python3 - "$VALID/crashlytics-mapping-upload-receipt.json" <<'PY'
import json
import sys

receipt = json.load(open(sys.argv[1], encoding="utf-8"))
assert receipt["status"] == "completed"
assert receipt["firebase"]["projectId"] == "earthquake-task9-test"
assert receipt["firebase"]["appId"] == "1:123456789:android:abcdef"
assert receipt["github"]["runId"] == "12345"
assert receipt["github"]["runAttempt"] == "2"
PY

readonly NOT_REQUESTED="$TEST_ROOT/not-requested"
create_fixture "$NOT_REQUESTED" false
if "$SCRIPT" \
    --verified-dir "$NOT_REQUESTED" \
    --uploaded-mapping "$NOT_REQUESTED/mapping.txt" \
    --firebase-project-id earthquake-task9-test \
    --firebase-app-id 1:123456789:android:abcdef \
    --github-run-id 12345 \
    --github-run-attempt 2 >/dev/null 2>&1; then
    fail "receipt was recorded when mapping upload was not requested"
fi
[[ ! -e "$NOT_REQUESTED/crashlytics-mapping-upload-receipt.json" ]] || fail "failed receipt attempt left output"

readonly TAMPERED="$TEST_ROOT/tampered"
create_fixture "$TAMPERED" true
cp "$TAMPERED/mapping.txt" "$TAMPERED/uploaded-mapping.txt"
printf 'tampered\n' >> "$TAMPERED/uploaded-mapping.txt"
if "$SCRIPT" \
    --verified-dir "$TAMPERED" \
    --uploaded-mapping "$TAMPERED/uploaded-mapping.txt" \
    --firebase-project-id earthquake-task9-test \
    --firebase-app-id 1:123456789:android:abcdef \
    --github-run-id 12345 \
    --github-run-attempt 2 >/dev/null 2>&1; then
    fail "receipt accepted a modified verified artifact"
fi
[[ ! -e "$TAMPERED/crashlytics-mapping-upload-receipt.json" ]] || fail "tampered receipt attempt left output"

printf 'PASS: mapping upload receipt is fail-closed and checksum-bound\n'
