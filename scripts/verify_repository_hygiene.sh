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
            LC_ALL=C perl -e '
                $source = do { local $/; <> };
                @characters = split //, $source;
                @output = map { $_ eq "\n" ? "\n" : " " } @characters;
                $double_quote = chr(34);
                $single_quote = chr(39);
                $backslash = chr(92);
                $dollar = chr(36);

                sub mark_code {
                    $output[$_[0]] = $characters[$_[0]];
                }

                sub scan_character_literal {
                    my ($index) = @_;
                    $index++;
                    while ($index < @characters) {
                        $character = $characters[$index++];
                        if ($character eq $backslash) {
                            $index++;
                            next;
                        }
                        return $index if $character eq $single_quote;
                    }
                    return $index;
                }

                sub scan_string {
                    my ($index, $raw_string) = @_;
                    $index += $raw_string ? 3 : 1;
                    while ($index < @characters) {
                        if ($raw_string && join("", @characters[$index .. $index + 2]) eq $double_quote x 3) {
                            return $index + 3;
                        }
                        $character = $characters[$index];
                        if (!$raw_string && $character eq $double_quote) {
                            return $index + 1;
                        }
                        if (!$raw_string && $character eq $backslash) {
                            $index += 2;
                            next;
                        }
                        if ($character eq $dollar && $characters[$index + 1] eq "{") {
                            $index = scan_code($index + 2, 1);
                            next;
                        }
                        $index++;
                    }
                    return $index;
                }

                sub scan_code {
                    my ($index, $stop_at_template_end) = @_;
                    $template_brace_depth = 0;
                    while ($index < @characters) {
                        if (join("", @characters[$index .. $index + 1]) eq "//") {
                            $index += 2;
                            $index++ while $index < @characters && $characters[$index] ne "\n";
                            next;
                        }
                        if (join("", @characters[$index .. $index + 1]) eq "/*") {
                            $index += 2;
                            $index++ while $index < @characters - 1 && join("", @characters[$index .. $index + 1]) ne "*/";
                            $index += 2 if $index < @characters - 1;
                            next;
                        }
                        if (join("", @characters[$index .. $index + 2]) eq $double_quote x 3) {
                            $index = scan_string($index, 1);
                            next;
                        }
                        $character = $characters[$index];
                        if ($character eq $double_quote) {
                            $index = scan_string($index, 0);
                            next;
                        }
                        if ($character eq $single_quote) {
                            $index = scan_character_literal($index);
                            next;
                        }
                        if ($stop_at_template_end && $character eq "}" && $template_brace_depth == 0) {
                            return $index + 1;
                        }
                        if ($stop_at_template_end && $character eq "{") {
                            $template_brace_depth++;
                        } elsif ($stop_at_template_end && $character eq "}") {
                            $template_brace_depth--;
                        }
                        mark_code($index);
                        $index++;
                    }
                    return $index;
                }

                scan_code(0, 0);
                @raw_lines = split /\n/, $source, -1;
                @code_lines = split /\n/, join("", @output), -1;
                for ($line_number = 0; $line_number < @raw_lines; $line_number++) {
                    if ($code_lines[$line_number] =~ /(?<![[:alnum:]_])(TODO|println|printStackTrace)\s*\(/) {
                        print "$ARGV:" . ($line_number + 1) . ":$raw_lines[$line_number]\n";
                    }
                }
            ' "$file"
        done)
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
