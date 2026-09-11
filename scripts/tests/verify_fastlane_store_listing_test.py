#!/usr/bin/env python3

import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
FASTFILE = ROOT / "fastlane" / "Fastfile"
APPFILE = ROOT / "fastlane" / "Appfile"


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


if __name__ == "__main__":
    unittest.main()
