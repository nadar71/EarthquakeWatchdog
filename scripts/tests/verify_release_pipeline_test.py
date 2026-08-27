#!/usr/bin/env python3

import re
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
WORKFLOW = ROOT / ".github" / "workflows" / "android-release.yml"
PREPARE = ROOT / "scripts" / "prepare_release_secrets.sh"
VERIFY = ROOT / "scripts" / "verify_aab.sh"
RECORD_UPLOAD = ROOT / "scripts" / "record_crashlytics_mapping_upload.sh"


class ReleasePipelineContractTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        cls.workflow = WORKFLOW.read_text(encoding="utf-8")
        cls.prepare = PREPARE.read_text(encoding="utf-8")
        cls.verify = VERIFY.read_text(encoding="utf-8")
        cls.record_upload = RECORD_UPLOAD.read_text(encoding="utf-8")

    def test_release_has_only_protected_manual_and_version_tag_entrypoints(self) -> None:
        self.assertRegex(cls_text := self.workflow, r"(?m)^  push:\n    tags:\n      - ['\"]v\*['\"]$")
        self.assertIn("workflow_dispatch:", cls_text)
        self.assertNotIn("pull_request:", cls_text)
        self.assertNotIn("pull_request_target:", cls_text)
        self.assertNotRegex(cls_text, r"(?m)^    branches:")
        self.assertIn('expected_ref = f"refs/tags/v{version_name}"', cls_text)
        self.assertIn('os.environ["RELEASE_REF"] != expected_ref', cls_text)

    def test_release_job_is_approval_gated_read_only_and_concurrency_safe(self) -> None:
        self.assertRegex(self.workflow, r"(?m)^permissions:\n  contents: read$")
        self.assertRegex(self.workflow, r"(?m)^    environment: production-release$")
        self.assertIn("cancel-in-progress: false", self.workflow)
        self.assertIn("github.event_name", self.workflow)
        self.assertIn("github.ref", self.workflow)
        self.assertNotIn("write-all", self.workflow)

    def test_release_uses_exact_gradle_secret_names_and_private_transport_secrets(self) -> None:
        for name in (
            "release_keyAlias",
            "release_keyPassword",
            "release_storePassword",
            "MAPS_API_KEY_RELEASE",
            "RELEASE_KEYSTORE_BASE64",
            "GOOGLE_SERVICES_JSON_BASE64",
            "FIREBASE_PROJECT_ID",
            "FIREBASE_APP_ID",
        ):
            self.assertIn(name, self.workflow)
            self.assertIn(name, self.prepare)
        self.assertIn("release_storeFile", self.prepare)
        self.assertNotRegex(self.workflow, r"(?i)echo .*secrets\.")
        self.assertIn(
            "unset RELEASE_KEYSTORE_BASE64 GOOGLE_SERVICES_JSON_BASE64",
            self.workflow,
        )

    def test_quality_gates_precede_signed_bundle_and_mapping_upload_is_explicit(self) -> None:
        quality = self.workflow.index(":app:testDebugUnitTest")
        bundle = self.workflow.index(":app:bundleRelease")
        self.assertLess(quality, bundle)
        self.assertIn("scripts/verify_repository_hygiene.sh", self.workflow)
        self.assertIn("scripts/tests/verify_github_actions_test.py", self.workflow)
        self.assertIn("scripts/tests/verify_release_pipeline_test.py", self.workflow)
        self.assertIn("name: release-instrumentation-api-35", self.workflow)
        self.assertIn("uses: ./.github/actions/enable-kvm", self.workflow)
        self.assertIn(":app:connectedDebugAndroidTest", self.workflow)
        self.assertRegex(
            self.workflow,
            r"(?m)^    needs: \[quality, instrumentation\]$",
        )
        self.assertEqual(self.workflow.count("environment: production-release"), 1)
        self.assertIn("upload_crashlytics_mapping", self.workflow)
        self.assertIn("crashlyticsMappingUploadEnabled", self.workflow)
        self.assertRegex(
            self.workflow,
            r"(?s)upload_crashlytics_mapping:.*?type: boolean.*?default: false",
        )

    def test_bundle_is_verified_before_explicit_mapping_upload(self) -> None:
        bundle = self.workflow.index(":app:bundleRelease")
        verify = self.workflow.index("scripts/verify_aab.sh")
        upload = self.workflow.index(":app:uploadCrashlyticsMappingFileRelease")
        self.assertLess(bundle, verify)
        self.assertLess(verify, upload)
        build_block = self.workflow[bundle - 300:verify]
        self.assertIn("-PcrashlyticsMappingUploadEnabled=false", build_block)
        self.assertNotIn("mapping_upload=true", build_block)
        self.assertIn("scripts/record_crashlytics_mapping_upload.sh", self.workflow[upload:])
        self.assertIn("crashlytics-mapping-upload-receipt.json", self.record_upload)

    def test_firebase_identity_comes_from_protected_environment_variables(self) -> None:
        self.assertIn("FIREBASE_PROJECT_ID: ${{ vars.FIREBASE_PROJECT_ID }}", self.workflow)
        self.assertIn("FIREBASE_APP_ID: ${{ vars.FIREBASE_APP_ID }}", self.workflow)
        self.assertIn('os.environ["RELEASE_EXPECTED_FIREBASE_PROJECT_ID"]', self.prepare)
        self.assertIn('os.environ["RELEASE_EXPECTED_FIREBASE_APP_ID"]', self.prepare)

    def test_mapping_upload_receipt_is_written_only_after_upload_task(self) -> None:
        upload = self.workflow.index(":app:uploadCrashlyticsMappingFileRelease")
        receipt = self.workflow.index("scripts/record_crashlytics_mapping_upload.sh")
        cleanup = self.workflow.index("cleanup_release_secrets", receipt)
        self.assertLess(upload, receipt)
        self.assertLess(receipt, cleanup)

    def test_bundletool_and_remote_actions_are_immutably_pinned(self) -> None:
        self.assertIn("bundletool-all-1.18.3.jar", self.workflow)
        self.assertIn(
            "a099cfa1543f55593bc2ed16a70a7c67fe54b1747bb7301f37fdfd6d91028e29",
            self.workflow,
        )
        for reference, version in re.findall(
            r"(?m)^\s*-?\s*uses:\s*([^\s]+)(?:\s+#\s*(v[^\s]+))?\s*$",
            self.workflow,
        ):
            if reference.startswith("./"):
                continue
            self.assertRegex(reference, r"^[^@\s]+@[0-9a-f]{40}$")
            self.assertRegex(version, r"^v\d")

    def test_verifier_checks_signature_identity_profiles_mapping_and_provenance(self) -> None:
        for required in (
            "jarsigner",
            "bundletool",
            "expected-package",
            "expected-version-code",
            "expected-version-name",
            "expected-cert-sha256",
            "baseline.prof",
            "baseline.profm",
            "proguard.map",
            "SHA256SUMS",
            "release-provenance.json",
        ):
            self.assertIn(required, self.verify)

    def test_release_output_is_manual_only_and_has_restricted_retention(self) -> None:
        self.assertIn("actions/upload-artifact@", self.workflow)
        self.assertRegex(self.workflow, r"retention-days:\s*(?:1[0-9]|2[0-9]|30)\b")
        self.assertNotRegex(self.workflow, r"(?i)(play|fastlane|service.account).*(upload|publish|deploy)")
        self.assertNotIn("PLAY_SERVICE_ACCOUNT", self.workflow)


if __name__ == "__main__":
    unittest.main()
