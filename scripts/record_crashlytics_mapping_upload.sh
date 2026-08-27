#!/usr/bin/env bash
set -euo pipefail

fail() {
    printf 'ERROR: %s\n' "$1" >&2
    exit 1
}

usage() {
    cat >&2 <<'EOF'
Usage: record_crashlytics_mapping_upload.sh \
  --verified-dir DIRECTORY --uploaded-mapping FILE \
  --firebase-project-id ID --firebase-app-id ID \
  --github-run-id NUMBER --github-run-attempt NUMBER
EOF
    exit 2
}

verified_dir=""
uploaded_mapping=""
firebase_project_id=""
firebase_app_id=""
github_run_id=""
github_run_attempt=""

while [[ $# -gt 0 ]]; do
    case "$1" in
        --verified-dir) verified_dir="${2:-}"; shift 2 ;;
        --uploaded-mapping) uploaded_mapping="${2:-}"; shift 2 ;;
        --firebase-project-id) firebase_project_id="${2:-}"; shift 2 ;;
        --firebase-app-id) firebase_app_id="${2:-}"; shift 2 ;;
        --github-run-id) github_run_id="${2:-}"; shift 2 ;;
        --github-run-attempt) github_run_attempt="${2:-}"; shift 2 ;;
        *) usage ;;
    esac
done

[[ -d "$verified_dir" && ! -L "$verified_dir" ]] || fail "verified release directory is missing"
[[ -f "$uploaded_mapping" && ! -L "$uploaded_mapping" ]] || fail "uploaded mapping file is missing"
[[ "$firebase_project_id" =~ ^[a-z][a-z0-9-]{4,28}[a-z0-9]$ ]] \
    || fail "Firebase project id has an unsupported format"
[[ "$firebase_app_id" =~ ^1:[0-9]+:android:[0-9A-Za-z]+$ ]] \
    || fail "Firebase app id has an unsupported format"
[[ "$github_run_id" =~ ^[1-9][0-9]*$ ]] || fail "GitHub run id is invalid"
[[ "$github_run_attempt" =~ ^[1-9][0-9]*$ ]] || fail "GitHub run attempt is invalid"

readonly provenance="$verified_dir/release-provenance.json"
readonly checksums="$verified_dir/SHA256SUMS"
readonly receipt="$verified_dir/crashlytics-mapping-upload-receipt.json"
[[ -f "$provenance" && ! -L "$provenance" ]] || fail "release provenance is missing"
[[ -f "$checksums" && ! -L "$checksums" ]] || fail "release checksums are missing"
[[ ! -e "$receipt" ]] || fail "mapping upload receipt already exists"

(
    cd "$verified_dir"
    shasum -a 256 -c SHA256SUMS >/dev/null
) || fail "verified release artifacts changed before mapping upload receipt creation"
cmp -s "$verified_dir/mapping.txt" "$uploaded_mapping" \
    || fail "the mapping used by Crashlytics differs from the verified release mapping"

umask 077
temp_receipt="$(mktemp "$verified_dir/.mapping-upload-receipt.XXXXXX")"
readonly temp_receipt
temp_checksums="$(mktemp "$verified_dir/.mapping-upload-checksums.XXXXXX")"
readonly temp_checksums
receipt_published=false
cleanup() {
    rm -f -- "$temp_receipt" "$temp_checksums"
    if [[ "$receipt_published" != "true" ]]; then
        rm -f -- "$receipt"
    fi
}
trap cleanup EXIT HUP INT TERM

python3 - "$provenance" "$temp_receipt" "$firebase_project_id" "$firebase_app_id" \
    "$github_run_id" "$github_run_attempt" <<'PY'
import hashlib
import json
import sys
from pathlib import Path

provenance_path, receipt_path, project_id, app_id, run_id, run_attempt = sys.argv[1:]
with open(provenance_path, encoding="utf-8") as source:
    provenance = json.load(source)

state = provenance.get("verification", {}).get("crashlyticsMappingUpload", {})
if state != {"requested": True, "completedAtVerification": False}:
    raise SystemExit("ERROR: verified provenance does not authorize a mapping upload receipt")

mapping_path = Path(provenance_path).parent / "mapping.txt"
mapping_hash = hashlib.sha256(mapping_path.read_bytes()).hexdigest()
if provenance.get("artifacts", {}).get("mapping.txt") != mapping_hash:
    raise SystemExit("ERROR: mapping checksum differs from verified provenance")

receipt = {
    "schemaVersion": 1,
    "status": "completed",
    "source": provenance["source"],
    "mappingSha256": mapping_hash,
    "firebase": {"projectId": project_id, "appId": app_id},
    "github": {"runId": run_id, "runAttempt": run_attempt},
}
with open(receipt_path, "w", encoding="utf-8") as destination:
    json.dump(receipt, destination, indent=2, sort_keys=True)
    destination.write("\n")
PY

chmod 600 "$temp_receipt"
mv -- "$temp_receipt" "$receipt"

(
    cd "$verified_dir"
    python3 - <<'PY' > "$temp_checksums"
import hashlib
from pathlib import Path

for path in sorted(Path(".").iterdir()):
    if path.name == "SHA256SUMS" or path.name.startswith(".mapping-upload-") or not path.is_file():
        continue
    print(f"{hashlib.sha256(path.read_bytes()).hexdigest()}  {path.name}")
PY
)
chmod 600 "$temp_checksums"
mv -- "$temp_checksums" "$checksums"
receipt_published=true
trap - EXIT HUP INT TERM
printf 'Crashlytics mapping upload completion receipt recorded.\n'
