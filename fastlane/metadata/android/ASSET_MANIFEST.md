# Google Play asset package

This directory follows the Fastlane Supply metadata layout for the `en-US` and `it-IT` Google Play listings.

## Included assets

| Asset | File | Dimensions / limit |
| --- | --- | --- |
| Store icon | `en-US/images/icon.png` | 512 x 512 px |
| Feature graphic | `en-US/images/featureGraphic.png` | 1024 x 500 px |
| Phone screenshots | `en-US/images/phoneScreenshots/*.png` | 1080 x 2160 px |
| App title | `en-US/title.txt` | Up to 30 characters |
| Short description | `en-US/short_description.txt` | Up to 80 characters |
| Full description | `en-US/full_description.txt` | Up to 4,000 characters |
| Release notes | `en-US/changelogs/11.txt` | Version code 11 |

The feature graphic was generated specifically for this listing using the original launcher icon as its visual reference. The store icon is a simplified waveform-and-epicenter redesign optimized for small-size readability.

The eight promotional phone screenshots use verified emulator captures for the earthquake list, world map, event actions, filters, statistics, manual location picker, map options, and app settings. Each capture is presented in a branded frame with concise English feature copy.

The `it-IT` package contains the same eight-feature sequence captured with the Italian app locale and localized Italian promotional copy, listing text, and release notes.

The preceding icon and screenshot set is archived under `en-US/images/previous/`.

Tablet, Chromebook, Wear OS, Android TV, and automotive screenshots are not included because the repository contains no verified captures for those device types.
