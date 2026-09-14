#!/usr/bin/env bash

set -euo pipefail

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
metadata_dir="$root_dir/fastlane/metadata/android"
output_dir="$root_dir/fastlane/promotional/videos"
work_dir="${TMPDIR:-/tmp}/earthquake-watchdog-promo"

command -v magick >/dev/null || { echo "ImageMagick is required (brew install imagemagick)." >&2; exit 1; }
command -v ffmpeg >/dev/null || { echo "FFmpeg is required (brew install ffmpeg)." >&2; exit 1; }

mkdir -p "$output_dir" "$work_dir"

for locale in en-US it-IT; do
  source_dir="$metadata_dir/$locale/images/phoneScreenshots"
  locale_work_dir="$work_dir/$locale"
  mkdir -p "$locale_work_dir"

  index=0
  inputs=()
  filters=()
  for screenshot in "$source_dir"/*.png; do
    slide="$locale_work_dir/slide-$(printf '%02d' "$index").png"

    # Keep the localized feature headline prominent while showing real app UI.
    magick -size 1920x1080 gradient:'#102630-#2d292d' \
      \( "$screenshot" -crop 1080x390+0+0 +repage -resize 1120x404 \) \
      -gravity northwest -geometry +70+155 -composite \
      \( "$screenshot" -crop 780x1700+150+350 +repage -resize x1000 \
         -bordercolor '#70818a' -border 2 \) \
      -gravity northeast -geometry +70+40 -composite \
      \( "$metadata_dir/$locale/images/icon.png" -resize 92x92 \) \
      -gravity southwest -geometry +76+64 -composite \
      -alpha off -colorspace sRGB "PNG24:$slide"

    inputs+=( -loop 1 -t 4.2 -i "$slide" )
    filters+=( "[$index:v]fps=30,format=yuv420p,zoompan=z='min(max(zoom,pzoom)+0.00028,1.035)':d=1:s=1920x1080:fps=30[v$index]" )
    index=$((index + 1))
  done

  if [[ "$index" -ne 8 ]]; then
    echo "Expected 8 $locale phone screenshots, found $index." >&2
    exit 1
  fi

  filter_graph="$(IFS=';'; echo "${filters[*]}")"
  previous="v0"
  for transition in 1 2 3 4 5 6 7; do
    offset=$(awk -v n="$transition" 'BEGIN { printf "%.1f", n * 3.6 }')
    output="x$transition"
    filter_graph+=";[$previous][v$transition]xfade=transition=fade:duration=0.6:offset=$offset[$output]"
    previous="$output"
  done

  ffmpeg -hide_banner -loglevel error -y "${inputs[@]}" \
    -filter_complex "$filter_graph" -map "[$previous]" \
    -c:v libx264 -preset fast -crf 21 -maxrate 8M -bufsize 16M \
    -pix_fmt yuv420p -movflags +faststart \
    "$output_dir/earthquake-watchdog-$locale.mp4"
done

echo "Promotional videos created in $output_dir"
