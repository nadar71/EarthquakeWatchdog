#!/usr/bin/env python3
"""Contracts for evidence-driven production operations documentation."""

from pathlib import Path
import re
import unittest


ROOT = Path(__file__).resolve().parents[2]
RELEASE_DOCS = ROOT / "docs" / "release"
REQUIRED_DOCS = {
    "play-compliance-checklist.md",
    "data-safety-inventory.md",
    "rollout-record.md",
    "incident-template.md",
}


class ProductionOperationsDocsTest(unittest.TestCase):
    def read(self, relative_path: str) -> str:
        path = ROOT / relative_path
        self.assertTrue(path.is_file(), f"Missing required document: {relative_path}")
        return path.read_text(encoding="utf-8")

    def test_required_records_define_evidence_statuses(self) -> None:
        for filename in REQUIRED_DOCS:
            content = self.read(f"docs/release/{filename}")
            for marker in ("CODE-VERIFIED", "PENDING", "BLOCKED", "NOT APPLICABLE"):
                self.assertIn(marker, content, f"{filename} must define/use {marker}")
            self.assertIn("Reviewed: 2026-08-27", content)

    def test_compliance_record_covers_every_external_gate(self) -> None:
        content = self.read("docs/release/play-compliance-checklist.md")
        required_topics = (
            "Target API",
            "precise location",
            "coarse location",
            "background location",
            "privacy policy",
            "Data safety",
            "Contains ads",
            "content rating",
            "target audience",
            "store listing",
            "Play App Signing",
            "production AdMob",
            "EEA",
            "non-EEA",
            "withdrawal",
            "offline",
            "internal testing",
            "closed testing",
            "current-public upgrade",
            "Crashlytics delivery",
            "physical performance",
        )
        for topic in required_topics:
            self.assertIn(topic.casefold(), content.casefold(), topic)

        self.assertIn("com.indiewalk.watchdog.earthquake", content)
        self.assertIn("ads_key_ids.xml", content)
        self.assertIn("Responsible action", content)
        self.assertIn("Evidence path", content)

    def test_data_safety_record_covers_code_and_sdk_flows(self) -> None:
        content = self.read("docs/release/data-safety-inventory.md")
        required_topics = (
            "Room",
            "DataStore",
            "backup",
            "clear storage",
            "USGS",
            "Geocoder",
            "Maps SDK",
            "Crashlytics",
            "Mobile Ads",
            "UMP",
            "Ktor",
            "OkHttp",
            "support email",
            "external browser",
            "sanitized",
            "no account",
            "retention",
            "deletion",
        )
        for topic in required_topics:
            self.assertIn(topic.casefold(), content.casefold(), topic)

        evidence_paths = re.findall(r"`(app/src/main/[^`]+)`", content)
        self.assertGreaterEqual(len(evidence_paths), 20)
        for relative_path in evidence_paths:
            self.assertTrue(
                (ROOT / relative_path).is_file(),
                f"Data inventory cites a nonexistent evidence path: {relative_path}",
            )

    def test_rollout_record_has_immutable_identity_and_all_stages(self) -> None:
        content = self.read("docs/release/rollout-record.md")
        for field in (
            "Commit SHA",
            "AAB SHA-256",
            "Signing certificate SHA-256",
            "Version code",
            "Version name",
            "Mapping SHA-256",
            "Provenance",
            "Approver",
            "Start time",
            "End time",
            "Population",
            "Crash-free users",
            "ANR rate",
            "Crash rate",
            "Support signals",
            "Maps health",
            "Network health",
            "Consent health",
            "Decision",
            "Evidence",
        ):
            self.assertIn(field, content)
        for stage in ("5%", "20%", "50%", "100%"):
            self.assertIn(stage, content)
        for threshold in ("0.47%", "1.09%", "8%"):
            self.assertIn(threshold, content)
        self.assertIn("verified-signed-release-aab", content)

    def test_incident_record_preserves_evidence_and_requires_new_artifact(self) -> None:
        content = self.read("docs/release/incident-template.md")
        self.assertIn("higher versionCode", content)
        self.assertIn("preserve", content.casefold())
        self.assertIn("Do not replace", content)
        self.assertIn("credentials", content)
        self.assertIn("coordinates", content)
        self.assertIn("consent payload", content)

    def test_runbook_links_operational_records(self) -> None:
        content = self.read("docs/release/release-runbook.md")
        for filename in REQUIRED_DOCS:
            self.assertIn(filename, content)
        self.assertIn("5%", content)
        self.assertIn("20%", content)
        self.assertIn("50%", content)
        self.assertIn("100%", content)

    def test_external_checks_are_not_marked_code_verified_or_checked(self) -> None:
        external_terms = re.compile(
            r"Play Console|Play App Signing|production AdMob|EEA|non-EEA|"
            r"internal testing|closed testing|Crashlytics delivery|physical performance",
            re.IGNORECASE,
        )
        for filename in REQUIRED_DOCS:
            for line in self.read(f"docs/release/{filename}").splitlines():
                if external_terms.search(line):
                    self.assertNotIn("[x]", line.casefold(), f"Unchecked external claim: {line}")
                    self.assertNotRegex(
                        line,
                        r"\|\s*CODE-VERIFIED\s*\|",
                        f"External evidence cannot be code-verified: {line}",
                    )

    def test_official_policy_citations_are_dated_and_primary(self) -> None:
        allowed_hosts = (
            "support.google.com",
            "developer.android.com",
            "developers.google.com",
            "firebase.google.com",
            "earthquake.usgs.gov",
        )
        combined = "\n".join(
            self.read(f"docs/release/{filename}") for filename in REQUIRED_DOCS
        )
        urls = re.findall(r"https://([^/\s)]+)[^\s)]*", combined)
        self.assertGreaterEqual(len(urls), 12)
        for host in urls:
            self.assertTrue(
                any(host == allowed or host.endswith(f".{allowed}") for allowed in allowed_hosts),
                f"Non-primary policy citation host: {host}",
            )
        self.assertGreaterEqual(combined.count("Reviewed 2026-08-27"), 12)

    def test_localized_faq_matches_current_retention_and_location_behavior(self) -> None:
        english = self.read("app/src/main/res/values/strings.xml")
        italian = self.read("app/src/main/res/values-it-rIT/strings.xml")
        self.assertNotIn("last 120 earthquakes", english)
        self.assertNotIn("ultimi 120 terremoti", italian)
        for content in (english, italian):
            self.assertIn("30", content)
            self.assertIn("200", content)
        for phrase, content in (
            ("nearby statistics", english),
            ("statistiche nelle vicinanze", italian),
            ("No background location", english),
            ("Nessuna localizzazione in background", italian),
        ):
            self.assertIn(phrase, content)

    def test_checklist_keeps_task_10_external_work_pending(self) -> None:
        checklist = self.read("docs/release/production-readiness-checklist.md")
        self.assertIn("Task 10", checklist)
        self.assertIn("PENDING", checklist)
        self.assertIn("694a0d9", checklist)

        # The local Superpowers execution ledger is intentionally gitignored.
        # Validate it when present without making clean checkouts depend on it.
        ledger_path = ROOT / ".superpowers/sdd/2026-08-26-production-readiness/progress.md"
        if ledger_path.is_file():
            ledger = ledger_path.read_text(encoding="utf-8")
            self.assertIn("Task 10", ledger)
            self.assertIn("PENDING", ledger)


if __name__ == "__main__":
    unittest.main()
