#!/usr/bin/env python3

import struct
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
FASTFILE = ROOT / "fastlane" / "Fastfile"
APPFILE = ROOT / "fastlane" / "Appfile"
METADATA_ROOT = ROOT / "fastlane" / "metadata" / "android"


class FastlaneStoreListingContractTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        cls.fastfile = FASTFILE.read_text(encoding="utf-8") if FASTFILE.exists() else ""
        cls.appfile = APPFILE.read_text(encoding="utf-8") if APPFILE.exists() else ""

    def test_package_and_metadata_path_are_explicit(self) -> None:
        self.assertIn('package_name("com.indiewalk.watchdog.earthquake")', self.appfile)
        self.assertIn('metadata_path: "fastlane/metadata/android"', self.fastfile)

    def test_credentials_come_only_from_environment(self) -> None:
        self.assertIn('json_key_file(ENV.fetch("GOOGLE_PLAY_JSON_KEY"))', self.appfile)
        self.assertIn('ENV.fetch("GOOGLE_PLAY_JSON_KEY")', self.appfile)
        self.assertNotIn("json_key(", self.appfile)
        self.assertNotIn("service-account.json", self.fastfile + self.appfile)

    def test_validate_and_upload_lanes_are_separate(self) -> None:
        self.assertIn("lane :validate_store_listing", self.fastfile)
        self.assertIn("validate_only: true", self.fastfile)
        self.assertIn("lane :upload_store_listing", self.fastfile)

    def test_listing_lanes_never_upload_binaries_or_stale_changelogs(self) -> None:
        for option in (
            "skip_upload_apk: true",
            "skip_upload_aab: true",
            "skip_upload_changelogs: true",
        ):
            self.assertEqual(self.fastfile.count(option), 2)

        self.assertIn("sync_image_upload: true", self.fastfile)

    def test_all_managed_titles_fit_google_play_limit(self) -> None:
        title_files = list(METADATA_ROOT.glob("*/title.txt"))
        self.assertGreater(len(title_files), 0)
        for title_file in title_files:
            title = title_file.read_text(encoding="utf-8").strip()
            self.assertLessEqual(len(title), 30, title_file.as_posix())

    def test_localized_tablet_screenshot_sets_are_complete(self) -> None:
        for locale in ("en-US", "it-IT"):
            for device_type in ("sevenInchScreenshots", "tenInchScreenshots"):
                directory = METADATA_ROOT / locale / "images" / device_type
                screenshots = sorted(directory.glob("*.png"))
                self.assertEqual(len(screenshots), 8, directory.as_posix())
                for screenshot in screenshots:
                    with screenshot.open("rb") as stream:
                        self.assertEqual(stream.read(8), b"\x89PNG\r\n\x1a\n")
                        length = struct.unpack(">I", stream.read(4))[0]
                        self.assertEqual(stream.read(4), b"IHDR")
                        ihdr = stream.read(length)
                    width, height = struct.unpack(">II", ihdr[:8])
                    self.assertEqual((width, height), (1080, 1920), screenshot.as_posix())
                    self.assertEqual(ihdr[9], 2, f"Expected RGB PNG: {screenshot}")

    def test_localized_promotional_videos_exist(self) -> None:
        video_root = ROOT / "fastlane" / "promotional" / "videos"
        for locale in ("en-US", "it-IT"):
            video = video_root / f"earthquake-watchdog-{locale}.mp4"
            self.assertTrue(video.is_file(), video.as_posix())
            self.assertGreater(video.stat().st_size, 1_000_000, video.as_posix())


if __name__ == "__main__":
    unittest.main()
