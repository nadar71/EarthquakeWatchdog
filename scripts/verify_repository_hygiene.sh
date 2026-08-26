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

    if matches=$(git ls-files | grep -E '(^|/)(google-services\.json|google_maps_api\.xml|admob_key\.xml|ads_key_ids\.xml|keystore\.properties|[^/]*(service[-_]?account|serviceaccount)[^/]*\.json|benchmarkData\.json|output-metadata\.json)$|(\.(jks|keystore|p12|apk|aab|apks|ap_|dm|trace|perfetto-trace))$'); then
        report_failure "tracked secret or generated artifact" "$matches"
    fi
}

check_executable_source() {
    local matches

    # Strip comments and quoted literals before checking executable Kotlin/Java calls.
    matches=$(rg --files -g '*.kt' -g '*.java' "$SOURCE_DIRECTORY" \
        | while IFS= read -r file; do
            LC_ALL=C perl -ne '
                BEGIN {
                    $block_comment = 0;
                    $triple_quote = 0;
                    $line_number = 0;
                    $double_quote = chr(34);
                    $single_quote = chr(39);
                    $backslash = chr(92);
                }
                $line_number++;
                $raw_line = $_;
                $code = "";
                for ($index = 0; $index < length($raw_line); ) {
                    if ($block_comment) {
                        if (substr($raw_line, $index, 2) eq "*/") {
                            $block_comment = 0;
                            $index += 2;
                        } else {
                            $index++;
                        }
                        next;
                    }
                    if ($triple_quote) {
                        if (substr($raw_line, $index, 3) eq $double_quote x 3) {
                            $triple_quote = 0;
                            $index += 3;
                        } else {
                            $index++;
                        }
                        next;
                    }
                    if (substr($raw_line, $index, 2) eq "//") {
                        last;
                    }
                    if (substr($raw_line, $index, 2) eq "/*") {
                        $block_comment = 1;
                        $index += 2;
                        next;
                    }
                    if (substr($raw_line, $index, 3) eq $double_quote x 3) {
                        $triple_quote = 1;
                        $index += 3;
                        next;
                    }
                    $character = substr($raw_line, $index, 1);
                    if ($character eq $double_quote || $character eq $single_quote) {
                        $quote = $character;
                        $index++;
                        while ($index < length($raw_line)) {
                            $character = substr($raw_line, $index, 1);
                            $index++;
                            if ($character eq $backslash) {
                                $index++;
                                next;
                            }
                            last if $character eq $quote;
                        }
                        next;
                    }
                    $code .= $character;
                    $index++;
                }
                if ($code =~ /(?<![[:alnum:]_])(TODO|println|printStackTrace)\s*\(/) {
                    print "$ARGV:$line_number:$raw_line";
                }
            ' "$file"
        done || true)
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
