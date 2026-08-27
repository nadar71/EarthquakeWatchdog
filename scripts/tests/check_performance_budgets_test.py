#!/usr/bin/env python3

import json
import subprocess
import tempfile
import unittest
from pathlib import Path
from typing import Optional


ROOT = Path(__file__).resolve().parents[2]
CHECKER = ROOT / "scripts" / "check_performance_budgets.py"


def benchmark(name: str, class_name: str, metrics: dict, sampled_metrics: Optional[dict] = None) -> dict:
    return {
        "name": name,
        "className": class_name,
        "repeatIterations": 10,
        "metrics": metrics,
        "sampledMetrics": sampled_metrics or {},
    }


def metric(median: float, maximum: Optional[float] = None) -> dict:
    return {
        "minimum": median,
        "maximum": median if maximum is None else maximum,
        "median": median,
        "runs": [median] * 10,
    }


class PerformanceBudgetCheckerTest(unittest.TestCase):
    def run_checker(self, benchmarks: list[dict], budgets: dict) -> subprocess.CompletedProcess[str]:
        with tempfile.TemporaryDirectory() as directory:
            directory_path = Path(directory)
            results = directory_path / "results.json"
            budget_file = directory_path / "budgets.json"
            results.write_text(json.dumps({"benchmarks": benchmarks}), encoding="utf-8")
            budget_file.write_text(json.dumps(budgets), encoding="utf-8")
            return subprocess.run(
                [str(CHECKER), "--budgets", str(budget_file), str(results)],
                cwd=ROOT,
                capture_output=True,
                text=True,
                check=False,
            )

    def test_accepts_complete_results_within_budgets(self) -> None:
        result = self.run_checker(
            [
                benchmark(
                    "coldStartup",
                    "com.example.StartupBenchmark",
                    {"timeToInitialDisplayMs": metric(900.0)},
                ),
                benchmark(
                    "listScroll",
                    "com.example.CoreJourneyBenchmark",
                    {
                        "memoryRssAnonMaxKb": metric(120_000.0),
                    },
                    {
                        "frameDurationCpuMs": {"P50": 35.0, "P95": 80.0},
                        "frameOverrunMs": {"P50": 20.0, "P95": 50.0},
                    },
                ),
            ],
            {
                "minimumIterations": 10,
                "enforcedBudgets": {
                    "StartupBenchmark#coldStartup": {
                        "timeToInitialDisplayMs.median": 1_000.0,
                    },
                    "CoreJourneyBenchmark#listScroll": {
                        "frameDurationCpuMs.P50": 40.0,
                        "frameOverrunMs.P50": 60.0,
                    },
                },
                "observations": {
                    "CoreJourneyBenchmark#listScroll": [
                        "frameDurationCpuMs.P95",
                        "memoryRssAnonMaxKb.median",
                    ]
                },
            },
        )

        self.assertEqual(0, result.returncode, result.stderr)
        self.assertIn("Performance budget verification passed", result.stdout)
        self.assertIn(
            "OBSERVE: CoreJourneyBenchmark#listScroll frameDurationCpuMs.P95: 80.000",
            result.stdout,
        )
        self.assertIn(
            "OBSERVE: CoreJourneyBenchmark#listScroll memoryRssAnonMaxKb.median: 120000.000",
            result.stdout,
        )

    def test_fails_when_a_metric_exceeds_its_budget(self) -> None:
        result = self.run_checker(
            [
                benchmark(
                    "coldStartup",
                    "com.example.StartupBenchmark",
                    {"timeToInitialDisplayMs": metric(1_001.0)},
                )
            ],
            {
                "minimumIterations": 10,
                "enforcedBudgets": {
                    "StartupBenchmark#coldStartup": {
                        "timeToInitialDisplayMs.median": 1_000.0,
                    }
                },
                "observations": {},
            },
        )

        self.assertNotEqual(0, result.returncode)
        self.assertIn("timeToInitialDisplayMs.median", result.stderr)

    def test_fails_when_required_benchmark_or_metric_is_missing(self) -> None:
        result = self.run_checker(
            [],
            {
                "minimumIterations": 10,
                "enforcedBudgets": {
                    "StartupBenchmark#coldStartup": {
                        "timeToInitialDisplayMs.median": 1_000.0,
                    }
                },
                "observations": {},
            },
        )

        self.assertNotEqual(0, result.returncode)
        self.assertIn("missing benchmark", result.stderr.lower())

    def test_observed_metrics_are_required_but_never_compared_to_a_limit(self) -> None:
        result = self.run_checker(
            [
                benchmark(
                    "listScroll",
                    "com.example.CoreJourneyBenchmark",
                    {},
                    {"frameDurationCpuMs": {"P99": 99_999.0}},
                )
            ],
            {
                "minimumIterations": 10,
                "enforcedBudgets": {},
                "observations": {
                    "CoreJourneyBenchmark#listScroll": [
                        "frameDurationCpuMs.P99",
                    ]
                },
            },
        )

        self.assertEqual(0, result.returncode, result.stderr)
        self.assertIn("frameDurationCpuMs.P99: 99999.000", result.stdout)

        missing_result = self.run_checker(
            [benchmark("listScroll", "com.example.CoreJourneyBenchmark", {})],
            {
                "minimumIterations": 10,
                "enforcedBudgets": {},
                "observations": {
                    "CoreJourneyBenchmark#listScroll": [
                        "frameDurationCpuMs.P99",
                    ]
                },
            },
        )

        self.assertNotEqual(0, missing_result.returncode)
        self.assertIn("missing metric frameDurationCpuMs", missing_result.stderr)

    def test_rejects_legacy_or_incomplete_budget_schema(self) -> None:
        result = self.run_checker(
            [],
            {
                "minimumIterations": 10,
                "benchmarks": {},
            },
        )

        self.assertNotEqual(0, result.returncode)
        self.assertIn("enforcedBudgets", result.stderr)
        self.assertIn("observations", result.stderr)


if __name__ == "__main__":
    unittest.main()
