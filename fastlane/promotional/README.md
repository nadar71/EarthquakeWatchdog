# Google Play promotional videos

Generate the localized videos with:

```bash
scripts/generate_play_store_videos.sh
```

The resulting `en-US` and `it-IT` MP4 files are silent, approximately 30 seconds long,
and encoded as 1920 x 1080 H.264 video for YouTube.

Google Play accepts a YouTube URL rather than an uploaded video file. Upload each MP4
to YouTube as public or unlisted, disable monetization, allow embedding, and do not set
an age restriction. Then place each plain video URL (without playlist, timestamp, or
tracking parameters) in the matching locale's `video.txt` before running Fastlane.
