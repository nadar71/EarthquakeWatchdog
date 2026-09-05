#!/usr/bin/env python3

import re
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
QUALITY = ROOT / ".github" / "workflows" / "android-quality.yml"
INSTRUMENTATION = ROOT / ".github" / "workflows" / "android-instrumentation.yml"
SETUP = ROOT / ".github" / "actions" / "setup-android" / "action.yml"
ENABLE_KVM = ROOT / ".github" / "actions" / "enable-kvm" / "action.yml"
INSTRUMENTATION_SCRIPT = ROOT / "scripts" / "run_instrumentation_ci.sh"
BENCHMARK_SCRIPT = ROOT / "scripts" / "run_benchmark_monitoring_ci.sh"


class GitHubActionsContractTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        cls.quality = QUALITY.read_text(encoding="utf-8")
        cls.instrumentation = INSTRUMENTATION.read_text(encoding="utf-8")
        cls.setup = SETUP.read_text(encoding="utf-8")
        cls.enable_kvm = (
            ENABLE_KVM.read_text(encoding="utf-8") if ENABLE_KVM.exists() else ""
        )
        cls.instrumentation_script = (
            INSTRUMENTATION_SCRIPT.read_text(encoding="utf-8")
            if INSTRUMENTATION_SCRIPT.exists()
            else ""
        )
        cls.benchmark_script = (
            BENCHMARK_SCRIPT.read_text(encoding="utf-8")
            if BENCHMARK_SCRIPT.exists()
            else ""
        )
        cls.all_configuration = "\n".join(
            (cls.quality, cls.instrumentation, cls.setup, cls.enable_kvm)
        )

    def test_required_pull_request_check_names_are_stable(self) -> None:
        self.assertRegex(
            self.quality,
            r"(?m)^  repository-hygiene:\n    name: repository-hygiene$",
        )
        self.assertRegex(
            self.quality,
            r"(?m)^  unit-lint-build:\n    name: unit-lint-build$",
        )
        self.assertRegex(
            self.instrumentation,
            r"(?m)^  instrumentation:\n    name: instrumentation$",
        )

    def test_workflows_use_read_only_default_permissions(self) -> None:
        for workflow in (self.quality, self.instrumentation):
            self.assertRegex(workflow, r"(?m)^permissions:\n  contents: read$")
            self.assertNotIn("write-all", workflow)

    def test_every_remote_action_is_immutably_pinned_with_version_comment(self) -> None:
        remote_uses = re.findall(
            r"(?m)^\s*-?\s*uses:\s*([^\s]+)(?:\s+#\s*(v[^\s]+))?\s*$",
            self.all_configuration,
        )
        self.assertTrue(remote_uses)
        for reference, version in remote_uses:
            if reference.startswith("./"):
                continue
            self.assertRegex(reference, r"^[^@\s]+@[0-9a-f]{40}$")
            self.assertRegex(version, r"^v\d")

    def test_setup_action_uses_java_17_wrapper_validation_and_gradle_cache(self) -> None:
        self.assertIn("java-version: \"17\"", self.setup)
        self.assertIn("gradle/actions/wrapper-validation@", self.setup)
        self.assertIn("gradle/actions/setup-gradle@", self.setup)

    def test_fast_jobs_run_required_commands_and_upload_failure_reports(self) -> None:
        self.assertIn("bash scripts/verify_repository_hygiene.sh", self.quality)
        self.assertIn("name: Run unit tests", self.quality)
        self.assertIn("./gradlew :app:testDebugUnitTest", self.quality)
        self.assertIn("name: Run Android lint", self.quality)
        self.assertIn("./gradlew :app:lintDebug", self.quality)
        self.assertIn("name: Assemble debug build", self.quality)
        self.assertIn("./gradlew :app:assembleDebug", self.quality)
        self.assertIn("if: failure()", self.quality)
        self.assertIn("actions/upload-artifact@", self.quality)
        self.assertIn("ci-logs/", self.quality)

    def test_ci_limits_gradle_and_emulator_memory(self) -> None:
        for workflow in (self.quality, self.instrumentation):
            self.assertIn("GRADLE_OPTS:", workflow)
            self.assertIn("org.gradle.workers.max=2", workflow)
            self.assertIn("-Xmx1536m", workflow)

        self.assertNotIn("ram-size: 4096M", self.instrumentation)
        self.assertIn("ram-size: 2048M", self.instrumentation)

    def test_ci_always_uploads_console_logs(self) -> None:
        self.assertIn("name: Upload quality console logs", self.quality)
        self.assertIn("if: always()", self.quality)
        self.assertIn("quality-ci-logs", self.quality)
        self.assertIn("instrumentation.log", self.instrumentation)

    def test_emulator_scripts_explicitly_use_bash(self) -> None:
        self.assertEqual(
            self.instrumentation.count(
                "script: bash scripts/run_instrumentation_ci.sh"
            ),
            2,
        )
        self.assertIn(
            "script: bash scripts/run_benchmark_monitoring_ci.sh",
            self.instrumentation,
        )
        for script in (self.instrumentation_script, self.benchmark_script):
            self.assertTrue(script.startswith("#!/usr/bin/env bash\n"))
            self.assertIn("set -euo pipefail", script)

    def test_instrumentation_and_compatibility_coverage_are_explicit(self) -> None:
        self.assertIn("api-level: 35", self.instrumentation)
        self.assertIn("api-level: [26, 36]", self.instrumentation)
        self.assertIn("disable-animations: true", self.instrumentation)
        self.assertIn(":app:connectedDebugAndroidTest", self.instrumentation_script)
        self.assertIn("schedule:", self.instrumentation)
        self.assertIn("workflow_dispatch:", self.instrumentation)
        self.assertIn("actions/cache/restore@", self.instrumentation)
        self.assertIn(
            "if: github.event_name != 'pull_request' && "
            "steps.api-35-avd-cache.outputs.cache-hit != 'true'",
            self.instrumentation,
        )
        self.assertNotIn("avd-v1-", self.instrumentation)
        self.assertIn("avd-v2-", self.instrumentation)

    def test_every_emulator_job_enables_linux_kvm_access(self) -> None:
        emulator_runner = "uses: reactivecircus/android-emulator-runner@"
        kvm_setup = "uses: ./.github/actions/enable-kvm"

        self.assertIn("shell: bash", self.enable_kvm)
        self.assertIn("set -euo pipefail", self.enable_kvm)
        self.assertIn("sudo tee /etc/udev/rules.d/99-kvm4all.rules", self.enable_kvm)
        self.assertIn("sudo udevadm control --reload-rules", self.enable_kvm)
        self.assertIn("sudo udevadm trigger --name-match=kvm", self.enable_kvm)

        job_blocks = re.findall(
            r"(?ms)^  [A-Za-z0-9_-]+:\n.*?(?=^  [A-Za-z0-9_-]+:\n|\Z)",
            self.instrumentation,
        )
        emulator_jobs = [job for job in job_blocks if emulator_runner in job]
        self.assertTrue(emulator_jobs)
        for job in emulator_jobs:
            self.assertEqual(job.count(kvm_setup), 1)
            self.assertLess(job.index(kvm_setup), job.index(emulator_runner))

    def test_workflow_concurrency_isolates_manual_runs_from_pushes(self) -> None:
        workflows = {
            "android-quality": self.quality,
            "android-instrumentation": self.instrumentation,
        }

        for workflow_name, workflow in workflows.items():
            with self.subTest(workflow=workflow_name):
                self.assertIn(
                    f"group: {workflow_name}-${{{{ github.workflow }}}}-"
                    "${{ github.event_name }}-${{ github.ref }}",
                    workflow,
                )
                self.assertIn(
                    "cancel-in-progress: ${{ github.event_name == 'pull_request' || "
                    "github.event_name == 'push' }}",
                    workflow,
                )

    def test_benchmark_monitoring_is_non_blocking_and_checks_task_7_outputs(self) -> None:
        self.assertRegex(
            self.instrumentation,
            r"(?s)benchmark-monitoring:.*?continue-on-error: true",
        )
        self.assertIn("BenchmarkSelectorContractTest", self.benchmark_script)
        self.assertIn(":app:generateBaselineProfile", self.benchmark_script)
        self.assertIn("StartupBenchmark", self.benchmark_script)
        self.assertIn("CoreJourneyBenchmark", self.benchmark_script)
        self.assertIn("scripts/check_performance_budgets.py", self.benchmark_script)

    def test_pull_request_workflows_do_not_reference_secrets(self) -> None:
        self.assertNotIn("pull_request_target:", self.all_configuration)
        self.assertNotRegex(self.all_configuration, r"(?i)\bsecrets\s*\.")
        self.assertNotRegex(self.all_configuration, r"(?i)\bsecrets\s*\[")


if __name__ == "__main__":
    unittest.main()
