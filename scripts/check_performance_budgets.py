#!/usr/bin/env python3

import argparse
import json
import sys
from pathlib import Path
from typing import Any, Dict, Iterable, Tuple


def benchmark_key(entry: Dict[str, Any]) -> str:
    class_name = str(entry.get("className", "")).rsplit(".", 1)[-1]
    return f"{class_name}#{entry.get('name', '')}"


def load_benchmarks(paths: Iterable[Path]) -> Dict[str, Dict[str, Any]]:
    benchmarks: Dict[str, Dict[str, Any]] = {}
    for path in paths:
        with path.open(encoding="utf-8") as source:
            document = json.load(source)
        for benchmark in document.get("benchmarks", []):
            benchmarks[benchmark_key(benchmark)] = benchmark
    return benchmarks


def metric_value(benchmark: Dict[str, Any], selector: str) -> float:
    metric_name, statistic = selector.rsplit(".", 1)
    metric = benchmark.get("metrics", {}).get(metric_name)
    if metric is None:
        metric = benchmark.get("sampledMetrics", {}).get(metric_name)
    if metric is None:
        raise KeyError(f"missing metric {metric_name}")
    if statistic not in metric:
        raise KeyError(f"missing statistic {selector}")
    return float(metric[statistic])


def verify(
    measured: Dict[str, Dict[str, Any]], budgets: Dict[str, Any]
) -> Tuple[bool, list, list]:
    failures = []
    reported_observations = []
    minimum_iterations = int(budgets.get("minimumIterations", 1))
    missing_sections = [
        section
        for section in ("enforcedBudgets", "observations")
        if section not in budgets
    ]
    if missing_sections:
        failures.append(
            "budget config is missing required section(s): "
            + ", ".join(missing_sections)
        )
        return False, failures, reported_observations

    enforced = budgets.get("enforcedBudgets", {})
    observations = budgets.get("observations", {})
    required_keys = list(dict.fromkeys([*enforced.keys(), *observations.keys()]))

    for key in required_keys:
        benchmark = measured.get(key)
        if benchmark is None:
            failures.append(f"missing benchmark: {key}")
            continue
        iterations = int(benchmark.get("repeatIterations", 0))
        if iterations < minimum_iterations:
            failures.append(
                f"{key}: expected at least {minimum_iterations} iterations, found {iterations}"
            )
        for selector, limit in enforced.get(key, {}).items():
            try:
                actual = metric_value(benchmark, selector)
            except KeyError as error:
                failures.append(f"{key}: {error.args[0]}")
                continue
            if actual > float(limit):
                failures.append(
                    f"{key} {selector}: measured {actual:.3f}, budget {float(limit):.3f}"
                )
        for selector in observations.get(key, []):
            try:
                actual = metric_value(benchmark, selector)
            except KeyError as error:
                failures.append(f"{key}: {error.args[0]}")
                continue
            reported_observations.append(f"{key} {selector}: {actual:.3f}")
    return not failures, failures, reported_observations


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Fail when AndroidX benchmark JSON exceeds checked performance budgets."
    )
    parser.add_argument("--budgets", required=True, type=Path)
    parser.add_argument("results", nargs="+", type=Path)
    arguments = parser.parse_args()

    with arguments.budgets.open(encoding="utf-8") as source:
        budgets = json.load(source)
    measured = load_benchmarks(arguments.results)
    passed, failures, observations = verify(measured, budgets)
    for observation in observations:
        print(f"OBSERVE: {observation}")
    if not passed:
        for failure in failures:
            print(f"FAIL: {failure}", file=sys.stderr)
        return 1
    print("Performance budget verification passed.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
