#!/usr/bin/env bash
set -euo pipefail

readonly EXPECTED_PACKAGE="com.indiewalk.watchdog.earthquake"
readonly REQUIRED_ENV=(
    RELEASE_KEYSTORE_BASE64
    GOOGLE_SERVICES_JSON_BASE64
    release_keyAlias
    release_keyPassword
    release_storePassword
    MAPS_API_KEY_RELEASE
    FIREBASE_PROJECT_ID
    FIREBASE_APP_ID
    ADMOB_APP_ID_RELEASE
    ADMOB_BANNER_ID_RELEASE
    PRIVACY_POLICY_URL_RELEASE
)

fail() {
    printf 'ERROR: %s\n' "$1" >&2
    exit 1
}

usage() {
    printf 'Usage: %s <prepare|cleanup> --output-dir ABSOLUTE_PATH --firebase-destination ABSOLUTE_PATH\n' "$0" >&2
    exit 2
}

canonical_path() {
    python3 - "$1" <<'PY'
import os
import sys

print(os.path.realpath(sys.argv[1]))
PY
}

mode="${1:-}"
[[ "$mode" == "prepare" || "$mode" == "cleanup" ]] || usage
shift

output_dir=""
firebase_destination=""
while [[ $# -gt 0 ]]; do
    case "$1" in
        --output-dir)
            [[ $# -ge 2 ]] || usage
            output_dir="$2"
            shift 2
            ;;
        --firebase-destination)
            [[ $# -ge 2 ]] || usage
            firebase_destination="$2"
            shift 2
            ;;
        *) usage ;;
    esac
done

[[ "$output_dir" == /* && "$firebase_destination" == /* ]] || fail "output paths must be absolute"
[[ "$output_dir" != *$'\n'* && "$firebase_destination" != *$'\n'* ]] || fail "output paths must not contain newlines"

output_dir="$(canonical_path "$output_dir")"
firebase_destination="$(canonical_path "$firebase_destination")"
readonly marker_file="$output_dir/.release-secrets"
readonly firebase_hash_file="$output_dir/app-google-services.sha256"

cleanup_material() {
    if [[ ! -f "$marker_file" || -L "$marker_file" ]]; then
        fail "refusing cleanup because the release-secret marker is missing"
    fi

    local recorded_destination
    recorded_destination="$(sed -n 's/^firebase_destination=//p' "$marker_file")"
    if [[ "$recorded_destination" != "$firebase_destination" ]]; then
        fail "refusing cleanup because the Firebase destination does not match the marker"
    fi

    if [[ -e "$firebase_destination" ]]; then
        [[ -f "$firebase_destination" && ! -L "$firebase_destination" ]] \
            || fail "refusing cleanup because the Firebase destination is not a regular file"
        [[ -f "$firebase_hash_file" && ! -L "$firebase_hash_file" ]] \
            || fail "refusing cleanup because the Firebase hash sidecar is missing"
        local expected_hash actual_hash
        expected_hash="$(cat "$firebase_hash_file")"
        [[ "$expected_hash" =~ ^[0-9a-f]{64}$ ]] \
            || fail "refusing cleanup because the Firebase hash sidecar is invalid"
        actual_hash="$(python3 - "$firebase_destination" <<'PY'
import hashlib
import sys

with open(sys.argv[1], "rb") as source:
    print(hashlib.sha256(source.read()).hexdigest())
PY
)"
        if [[ "$actual_hash" != "$expected_hash" ]]; then
            fail "refusing cleanup because the Firebase file changed after preparation"
        fi
        rm -f -- "$firebase_destination"
    elif [[ -e "$firebase_hash_file" ]]; then
        fail "refusing cleanup because Firebase cleanup metadata exists without its destination file"
    fi

    rm -rf -- "$output_dir"
}

if [[ "$mode" == "cleanup" ]]; then
    cleanup_material
    printf 'Release credentials removed.\n'
    exit 0
fi

for name in "${REQUIRED_ENV[@]}"; do
    [[ -n "${!name:-}" ]] || fail "missing required environment variable: $name"
done

[[ ! -e "$output_dir" ]] || fail "release-secret output directory already exists"
[[ ! -e "$firebase_destination" ]] || fail "Firebase destination already exists"
[[ -d "$(dirname "$firebase_destination")" ]] || fail "Firebase destination parent directory does not exist"

umask 077
mkdir -p "$(dirname "$output_dir")"
mkdir "$output_dir"
chmod 700 "$output_dir"

prepared=false
cleanup_on_failure() {
    if [[ "$prepared" != "true" ]]; then
        if [[ -f "$firebase_destination" ]]; then
            rm -f -- "$firebase_destination"
        fi
        rm -rf -- "$output_dir"
    fi
}
trap cleanup_on_failure EXIT HUP INT TERM

export RELEASE_KEYSTORE_PATH="$output_dir/release.keystore"
export RELEASE_PROPERTIES_PATH="$output_dir/release.properties"
export RELEASE_FIREBASE_DESTINATION="$firebase_destination"
export RELEASE_MARKER_PATH="$marker_file"
export RELEASE_FIREBASE_HASH_PATH="$firebase_hash_file"
export RELEASE_EXPECTED_PACKAGE="$EXPECTED_PACKAGE"
export RELEASE_EXPECTED_FIREBASE_PROJECT_ID="$FIREBASE_PROJECT_ID"
export RELEASE_EXPECTED_FIREBASE_APP_ID="$FIREBASE_APP_ID"
export RELEASE_ADMOB_APP_ID="$ADMOB_APP_ID_RELEASE"
export RELEASE_ADMOB_BANNER_ID="$ADMOB_BANNER_ID_RELEASE"
export RELEASE_PRIVACY_POLICY_URL="$PRIVACY_POLICY_URL_RELEASE"

python3 <<'PY'
import base64
import hashlib
import json
import os
import re
import urllib.parse


def decode_env(name: str) -> bytes:
    try:
        return base64.b64decode(os.environ[name], validate=True)
    except Exception as exc:
        raise SystemExit(f"ERROR: {name} is not valid base64") from exc


def exclusive_write(path: str, payload: bytes) -> None:
    flags = os.O_WRONLY | os.O_CREAT | os.O_EXCL
    if hasattr(os, "O_NOFOLLOW"):
        flags |= os.O_NOFOLLOW
    descriptor = os.open(path, flags, 0o600)
    with os.fdopen(descriptor, "wb") as destination:
        destination.write(payload)


def escape_property(value: str) -> str:
    escaped = []
    for index, char in enumerate(value):
        replacements = {
            "\\": "\\\\",
            "\t": "\\t",
            "\n": "\\n",
            "\r": "\\r",
            "\f": "\\f",
            "=": "\\=",
            ":": "\\:",
            "#": "\\#",
            "!": "\\!",
        }
        if char == " " and index == 0:
            escaped.append("\\ ")
        else:
            escaped.append(replacements.get(char, char))
    return "".join(escaped)


keystore = decode_env("RELEASE_KEYSTORE_BASE64")
firebase = decode_env("GOOGLE_SERVICES_JSON_BASE64")
if not keystore:
    raise SystemExit("ERROR: decoded release keystore is empty")

try:
    firebase_json = json.loads(firebase)
except (UnicodeDecodeError, json.JSONDecodeError) as exc:
    raise SystemExit("ERROR: decoded Firebase configuration is not valid JSON") from exc

packages = {
    client.get("client_info", {})
    .get("android_client_info", {})
    .get("package_name")
    for client in firebase_json.get("client", [])
}
if os.environ["RELEASE_EXPECTED_PACKAGE"] not in packages:
    raise SystemExit("ERROR: Firebase configuration does not contain the release package")

expected_project_id = os.environ["RELEASE_EXPECTED_FIREBASE_PROJECT_ID"]
expected_app_id = os.environ["RELEASE_EXPECTED_FIREBASE_APP_ID"]
if not re.fullmatch(r"[a-z][a-z0-9-]{4,28}[a-z0-9]", expected_project_id):
    raise SystemExit("ERROR: FIREBASE_PROJECT_ID has an unsupported format")
if not re.fullmatch(r"1:[0-9]+:android:[0-9A-Za-z]+", expected_app_id):
    raise SystemExit("ERROR: FIREBASE_APP_ID has an unsupported format")

actual_project_id = firebase_json.get("project_info", {}).get("project_id")
if actual_project_id != expected_project_id:
    raise SystemExit("ERROR: Firebase project id does not match the approved production project")

matching_clients = [
    client
    for client in firebase_json.get("client", [])
    if client.get("client_info", {})
    .get("android_client_info", {})
    .get("package_name") == os.environ["RELEASE_EXPECTED_PACKAGE"]
]
if not any(
    client.get("client_info", {}).get("mobilesdk_app_id") == expected_app_id
    for client in matching_clients
):
    raise SystemExit("ERROR: Firebase app id does not match the approved production Android app")

admob_app_id = os.environ["RELEASE_ADMOB_APP_ID"]
admob_banner_id = os.environ["RELEASE_ADMOB_BANNER_ID"]
test_publisher = "ca-app-pub-3940256099942544"
if not re.fullmatch(r"ca-app-pub-[0-9]{16}~[0-9]{10}", admob_app_id):
    raise SystemExit("ERROR: ADMOB_APP_ID_RELEASE has an unsupported format")
if not re.fullmatch(r"ca-app-pub-[0-9]{16}/[0-9]{10}", admob_banner_id):
    raise SystemExit("ERROR: ADMOB_BANNER_ID_RELEASE has an unsupported format")
if admob_app_id.startswith(test_publisher) or admob_banner_id.startswith(test_publisher):
    raise SystemExit("ERROR: release AdMob configuration must not use Google test ids")
if admob_app_id.split("~", 1)[0] != admob_banner_id.split("/", 1)[0]:
    raise SystemExit("ERROR: release AdMob app and banner ids must use the same publisher")

privacy_url = os.environ["RELEASE_PRIVACY_POLICY_URL"]
parsed_policy_url = urllib.parse.urlsplit(privacy_url)
if (
    parsed_policy_url.scheme != "https"
    or not parsed_policy_url.hostname
    or parsed_policy_url.username
    or parsed_policy_url.password
    or parsed_policy_url.fragment
):
    raise SystemExit("ERROR: PRIVACY_POLICY_URL_RELEASE must be a public HTTPS URL")

exclusive_write(os.environ["RELEASE_KEYSTORE_PATH"], keystore)
exclusive_write(os.environ["RELEASE_FIREBASE_DESTINATION"], firebase)

properties = {
    "release_keyAlias": os.environ["release_keyAlias"],
    "release_keyPassword": os.environ["release_keyPassword"],
    "release_storeFile": os.environ["RELEASE_KEYSTORE_PATH"],
    "release_storePassword": os.environ["release_storePassword"],
    "MAPS_API_KEY_RELEASE": os.environ["MAPS_API_KEY_RELEASE"],
    "ADMOB_APP_ID_RELEASE": admob_app_id,
    "ADMOB_BANNER_ID_RELEASE": admob_banner_id,
    "PRIVACY_POLICY_URL_RELEASE": privacy_url,
}
properties_payload = "".join(
    f"{key}={escape_property(value)}\n" for key, value in properties.items()
).encode("utf-8")
exclusive_write(os.environ["RELEASE_PROPERTIES_PATH"], properties_payload)

firebase_hash = hashlib.sha256(firebase).hexdigest()
exclusive_write(os.environ["RELEASE_FIREBASE_HASH_PATH"], f"{firebase_hash}\n".encode())
marker = f"version=1\nfirebase_destination={os.environ['RELEASE_FIREBASE_DESTINATION']}\n"
exclusive_write(os.environ["RELEASE_MARKER_PATH"], marker.encode("utf-8"))
PY

# shellcheck disable=SC2154 # Validated through REQUIRED_ENV before this command.
keytool -list \
    -keystore "$RELEASE_KEYSTORE_PATH" \
    -storepass:env release_storePassword \
    -alias "$release_keyAlias" >/dev/null

chmod 600 \
    "$RELEASE_KEYSTORE_PATH" \
    "$RELEASE_PROPERTIES_PATH" \
    "$RELEASE_FIREBASE_DESTINATION" \
    "$RELEASE_MARKER_PATH" \
    "$RELEASE_FIREBASE_HASH_PATH"

prepared=true
trap - EXIT HUP INT TERM
printf 'Release credentials prepared in private temporary files.\n'
