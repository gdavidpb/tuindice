#!/usr/bin/env python3
"""Builds <out>/report.md from the run records written by scripts/measure-scenario-poc.sh.

Facts only: every gate criterion gets its measured value and whether it meets the threshold, and
no overall verdict is written. Standard library only (Python 3.9).
"""
import argparse
import json
import math
import os
import shutil
import statistics
import subprocess
import sys
import tempfile

EXPECTED_FAILURE_FILE = "PocScenarios.kt"
CONTAINERS = {"Group", "Retry", "IfVisible", "IfGone", "OnPlatform"}
BRIDGE_MS_LIMIT = 5.0
DECODE_MS_LIMIT = 500.0
TAP_MS_LIMIT = 1500.0
XCODEBUILD_OVERHEAD_CONSULT_S = 40.0
PASS_RATE_MIN = 19


def read_env(path):
    values = {}
    try:
        with open(path) as handle:
            for line in handle:
                if "=" in line:
                    key, value = line.rstrip("\n").split("=", 1)
                    values[key] = value
    except OSError:
        pass
    return values


def read_json(path):
    try:
        with open(path) as handle:
            return json.load(handle)
    except (OSError, ValueError):
        return None


def median(values):
    return statistics.median(values) if values else None


def p95(values):
    if not values:
        return None
    ordered = sorted(values)
    return ordered[max(0, math.ceil(0.95 * len(ordered)) - 1)]


def fmt(value, digits=1, unit=""):
    if value is None:
        return "n/a"
    return ("%.*f" % (digits, value)) + unit


def run_tool(command):
    result = subprocess.run(command, capture_output=True, text=True)
    return result.returncode, result.stdout, result.stderr


class XcResult:
    """What the report needs from one .xcresult, cached next to it as xcresult-facts.json."""

    def __init__(self, path):
        self.path = path
        self.cache = os.path.join(os.path.dirname(path), "xcresult-facts.json")

    def facts(self, with_failure_detail):
        cached = read_json(self.cache)
        if cached is not None and (cached.get("detail") or not with_failure_detail):
            return cached
        facts = {"detail": with_failure_detail, "error": None, "tests": [], "summary": {}}
        if not os.path.isdir(self.path):
            facts["error"] = "no xcresult"
            return self._store(facts)
        code, out, err = run_tool(["xcrun", "xcresulttool", "get", "test-results", "summary", "--path", self.path])
        if code != 0:
            facts["error"] = "summary: " + err.strip()[:300]
            return self._store(facts)
        summary = json.loads(out)
        facts["summary"] = {key: summary.get(key) for key in ("result", "passedTests", "failedTests", "skippedTests", "totalTestCount", "startTime", "finishTime")}
        facts["summary"]["failureTexts"] = [item.get("failureText", "") for item in summary.get("testFailures", [])]
        code, out, err = run_tool(["xcrun", "xcresulttool", "get", "test-results", "tests", "--path", self.path])
        if code != 0:
            facts["error"] = "tests: " + err.strip()[:300]
            return self._store(facts)
        for case in self._cases(json.loads(out).get("testNodes", [])):
            facts["tests"].append(case)
        if with_failure_detail:
            for case in facts["tests"]:
                if case["result"] == "Failed":
                    case["detail"] = self._failure_detail(case["identifier"])
        return self._store(facts)

    def _store(self, facts):
        with open(self.cache, "w") as handle:
            json.dump(facts, handle)
        return facts

    def _cases(self, nodes):
        for node in nodes:
            if node.get("nodeType") == "Test Case":
                messages = [child for child in node.get("children", []) if child.get("nodeType") == "Failure Message"]
                yield {
                    "name": node.get("name"),
                    "identifier": node.get("nodeIdentifier"),
                    "result": node.get("result"),
                    "durationSeconds": node.get("durationInSeconds"),
                    "failureMessages": len(messages),
                }
            else:
                yield from self._cases(node.get("children", []))

    def _failure_detail(self, test_name):
        detail = {"sourceLocations": [], "attachments": [], "error": None}
        code, out, err = run_tool(["xcrun", "xcresulttool", "get", "test-results", "test-details", "--path", self.path, "--test-id", test_name])
        if code != 0:
            detail["error"] = "test-details: " + err.strip()[:300]
        else:
            self._collect_locations(json.loads(out).get("testRuns", []), detail["sourceLocations"])
        export_dir = tempfile.mkdtemp(prefix="f13-attachments-")
        try:
            code, out, err = run_tool(["xcrun", "xcresulttool", "export", "attachments", "--path", self.path, "--output-path", export_dir])
            manifest = read_json(os.path.join(export_dir, "manifest.json"))
            if code != 0 or manifest is None:
                detail["error"] = (detail["error"] or "") + " export: " + err.strip()[:300]
            else:
                for entry in manifest:
                    if not entry.get("testIdentifier", "").endswith(test_name.split("/")[-1]) and entry.get("testIdentifier") != test_name:
                        continue
                    for attachment in entry.get("attachments", []):
                        detail["attachments"].append({
                            "name": attachment.get("suggestedHumanReadableName", ""),
                            "failure": bool(attachment.get("isAssociatedWithFailure")),
                        })
        finally:
            shutil.rmtree(export_dir, ignore_errors=True)
        return detail

    def _collect_locations(self, nodes, into):
        for node in nodes:
            location = node.get("sourceLocation")
            if location and node.get("nodeType") == "Source Code Reference":
                into.append({"file": location.get("filePath"), "line": location.get("lineNumber")})
            self._collect_locations(node.get("children", []), into)


class Run:
    def __init__(self, series, directory):
        self.series = series
        self.dir = directory
        self.name = os.path.basename(directory)
        self.env = read_env(os.path.join(directory, "run.env"))
        self.kind = self.env.get("kind", self.name.split("-", 1)[-1])
        self.wall_ms = int(self.env["wall_ms"]) if self.env.get("wall_ms") else None
        self.cleanup_ms = int(self.env["cleanup_ms"]) if self.env.get("cleanup_ms") else None
        self.exit_code = int(self.env["exit_code"]) if self.env.get("exit_code") else None
        self.timed_out = self.env.get("timed_out") == "1"
        self.scenarios = self.env.get("scenarios", self.kind).split()
        self.auth = read_json(os.path.join(directory, "auth-check.json"))

    def result_path(self, scenario):
        platform = self.series.platform
        if platform == "ios":
            return os.path.join(self.dir, "results", scenario, "result.json")
        return os.path.join(self.dir, "artifacts", "result.json")

    def result(self, scenario):
        return read_json(self.result_path(scenario))

    @property
    def primary(self):
        return self.scenarios[0]

    def is_green(self):
        if self.kind == "catalog-decode":
            return self.exit_code == 0 and os.path.exists(os.path.join(self.dir, "results", "catalog-decode", "decode.json"))
        result = self.result(self.primary)
        return self.exit_code == 0 and result is not None and result.get("outcome") == "passed"

    def xcresult(self):
        return XcResult(os.path.join(self.dir, "run.xcresult"))

    def runner_log_tail(self, lines=8):
        try:
            with open(os.path.join(self.dir, "runner.log"), errors="replace") as handle:
                return [line.rstrip() for line in handle.readlines()[-lines:]]
        except OSError:
            return []

    def runner_log_contains(self, *needles):
        try:
            with open(os.path.join(self.dir, "runner.log"), errors="replace") as handle:
                text = handle.read()
        except OSError:
            return False
        return any(needle in text for needle in needles)


class Series:
    def __init__(self, directory):
        self.dir = directory
        self.name = os.path.basename(directory)
        self.env = read_env(os.path.join(directory, "series.env"))
        self.platform = self.env.get("platform", "?")
        self.runs = []
        for entry in sorted(os.listdir(directory)):
            path = os.path.join(directory, entry)
            if os.path.isdir(path) and os.path.exists(os.path.join(path, "run.env")):
                self.runs.append(Run(self, path))

    def of_kind(self, kind):
        return [run for run in self.runs if run.kind == kind]

    def scenario_kinds(self):
        seen = []
        for run in self.runs:
            if run.kind not in ("expected-failure", "catalog-decode") and run.kind not in seen:
                seen.append(run.kind)
        return seen


def load_values(series):
    values = []
    for run in series.runs:
        for key in ("load1_start", "load1_end"):
            try:
                values.append(float(run.env[key]))
            except (KeyError, ValueError):
                pass
    return values


def failure_text(run, scenario):
    result = run.result(scenario)
    if result and result.get("failure"):
        failure = result["failure"]
        site = failure.get("site") or {}
        where = "%s:%s" % (site.get("file"), site.get("line")) if site else "n/a"
        message = (failure.get("message") or "").replace("\n", " ")[:240]
        return "%s | step %s %s %s | %s | at %s" % (
            failure.get("kind"), failure.get("stepIndex"), failure.get("primitive"), failure.get("target"), message, where)
    return None


def not_green_section(series, out):
    out.append("#### Corridas no verdes")
    rows = []
    for run in series.runs:
        if run.kind in ("expected-failure",) or run.is_green():
            continue
        cause = failure_text(run, run.primary)
        if run.timed_out:
            cause = "COLGADA: matada a los 300 s (grupo de procesos). " + (cause or "")
        if cause is None:
            if series.platform == "ios":
                facts = run.xcresult().facts(False)
                texts = facts.get("summary", {}).get("failureTexts") or []
                cause = "sin result.json; xcodebuild exit %s; xcresult: %s" % (run.exit_code, (texts[0].replace("\n", " ")[:240] if texts else (facts.get("error") or "sin fallos de test")))
            else:
                cause = "sin result.json; adb exit %s; log: %s" % (run.exit_code, " / ".join(run.runner_log_tail(4)))
        artifacts = os.path.relpath(run.dir, os.path.dirname(os.path.dirname(series.dir)))
        rows.append("- `%s` (%s): %s. Artefactos: `%s`" % (run.name, run.kind, cause, artifacts))
    if rows:
        out.extend(rows)
    else:
        out.append("Ninguna.")
    # A run of the expected-failure kind whose second test (iOS) did not pass is also not green.
    for run in series.of_kind("expected-failure"):
        for scenario in run.scenarios[1:]:
            second = run.result(scenario)
            if second is None or second.get("outcome") != "passed":
                out.append("- `%s` (segundo test `%s`): no verde. %s" % (run.name, scenario, failure_text(run, scenario) or "sin result.json"))
    out.append("")


def pass_rates(series, out):
    out.append("#### Tasa de aprobación por escenario")
    out.append("")
    out.append("| Escenario | Bloque | Verdes | Corridas | Tasa |")
    out.append("|---|---|---|---|---|")
    totals = {}
    for run in series.runs:
        if run.kind in ("expected-failure", "catalog-decode"):
            continue
        key = (run.kind, run.env.get("block", "?"))
        entry = totals.setdefault(key, [0, 0])
        entry[1] += 1
        if run.is_green():
            entry[0] += 1
    per_kind = {}
    for (kind, block), (green, total) in totals.items():
        out.append("| %s | %s | %d | %d | %.0f%% |" % (kind, block, green, total, 100.0 * green / total))
        if block in ("1", "2", "3"):
            per_kind[kind] = [green, total]
    out.append("")
    return per_kind


def wall_times(series, out):
    out.append("#### Tiempo de pared por escenario (una invocación del runner)")
    out.append("")
    if series.platform == "ios":
        out.append("`wall` incluye `xcodebuild` completo (sin la limpieza previa); `test` es la duración del caso según el `.xcresult`; sobrecarga = wall - test.")
    else:
        out.append("`wall` es `am instrument -w` completo (sin la limpieza previa), incluido el arranque en frío de la app tras `pm clear`.")
    out.append("")
    out.append("| Escenario | n | wall mediana (s) | wall p95 (s) | intérprete mediana (s) | test mediana (s) | sobrecarga mediana (s) | sobrecarga p95 (s) |")
    out.append("|---|---|---|---|---|---|---|---|")
    overheads_all = []
    for kind in series.scenario_kinds():
        runs = [run for run in series.of_kind(kind) if run.wall_ms is not None]
        walls = [run.wall_ms / 1000.0 for run in runs]
        interp = []
        tests = []
        overheads = []
        for run in runs:
            result = run.result(run.primary)
            if result:
                interp.append(iso_seconds(result.get("finishedAt")) - iso_seconds(result.get("startedAt")))
            if series.platform == "ios":
                facts = run.xcresult().facts(False)
                durations = [case["durationSeconds"] for case in facts.get("tests", []) if case.get("durationSeconds") is not None]
                if durations:
                    tests.append(sum(durations))
                    overheads.append(run.wall_ms / 1000.0 - sum(durations))
        overheads_all.extend(overheads)
        out.append("| %s | %d | %s | %s | %s | %s | %s | %s |" % (
            kind, len(runs), fmt(median(walls), 1), fmt(p95(walls), 1), fmt(median(interp), 1),
            fmt(median(tests), 1) if tests else "n/a", fmt(median(overheads), 1) if overheads else "n/a",
            fmt(p95(overheads), 1) if overheads else "n/a"))
    out.append("")
    return overheads_all


def iso_seconds(text):
    if not text:
        return 0.0
    from datetime import datetime, timezone
    return datetime.strptime(text.replace("Z", "+0000")[:26] + "+0000", "%Y-%m-%dT%H:%M:%S.%f%z").timestamp() \
        if "." in text else datetime.strptime(text.replace("Z", "+0000"), "%Y-%m-%dT%H:%M:%S%z").timestamp()


def primitives(series, out):
    out.append("#### Duración por tipo de primitiva (pasos aprobados de `result.json`, ms)")
    out.append("")
    out.append("Cada paso espera su propio objetivo: `Tap` incluye esperar visible y habilitado; los contenedores (`Group`, `IfVisible`, ...) incluyen a sus hijos. Resolución del reloj: 1 ms.")
    out.append("")
    out.append("| Primitiva | n | mediana (ms) | p95 (ms) |")
    out.append("|---|---|---|---|")
    by_primitive = {}
    taps_login = []
    taps_by_target = {}
    for run in series.runs:
        if run.kind in ("expected-failure", "catalog-decode"):
            continue
        result = run.result(run.primary)
        if not result:
            continue
        for step in result.get("steps", []):
            if step.get("outcome") != "passed":
                continue
            by_primitive.setdefault(step["primitive"], []).append(step["durationMs"])
            if run.kind == "auth-login-cancel" and step["primitive"] == "Tap" and step["target"].startswith("tag:auth_"):
                taps_login.append(step["durationMs"])
                taps_by_target.setdefault(step["target"], []).append(step["durationMs"])
    for primitive in sorted(by_primitive):
        values = by_primitive[primitive]
        out.append("| %s%s | %d | %s | %s |" % (primitive, " (contenedor)" if primitive in CONTAINERS else "", len(values), fmt(median(values), 0), fmt(p95(values), 0)))
    out.append("")
    out.append("Mediana de `Tap` en la pantalla de inicio de sesión (`auth-login-cancel`, destinos `tag:auth_*`): **%s ms** (n=%d, p95 %s ms)." % (
        fmt(median(taps_login), 0), len(taps_login), fmt(p95(taps_login), 0)))
    out.append("")
    for target in sorted(taps_by_target):
        out.append("- `%s`: mediana %s ms, n=%d" % (target, fmt(median(taps_by_target[target]), 0), len(taps_by_target[target])))
    out.append("")
    return median(taps_login), len(taps_login)


def typing(series, out):
    out.append("#### Corrección del tecleo (`auth-login-cancel`: USB-ID de 7 dígitos y contraseña de 17 caracteres)")
    runs = series.of_kind("auth-login-cancel")
    ok = 0
    mismatches = []
    for run in runs:
        auth = run.auth or {}
        if auth.get("verdict") == "match":
            ok += 1
        else:
            decoded = [item.get("decoded") for item in auth.get("decoded", [])]
            mismatches.append("- `%s`: veredicto `%s`, esperado `%s`, decodificado literal %s" % (
                run.name, auth.get("verdict", "sin auth-check.json"), auth.get("expected"), json.dumps(decoded, ensure_ascii=False)))
    out.append("")
    out.append("**%d/%d** muestras con el `Authorization: Basic` del `POST /auth/v2/bootstrap` igual a `11-11111:login-cancel-pass`." % (ok, len(runs)))
    untyped = []
    for run in runs:
        if (run.auth or {}).get("verdict") == "no-bootstrap-request":
            failure = (run.result(run.primary) or {}).get("failure") or {}
            untyped.append("%s (%s en el paso %s)" % (run.name, failure.get("kind", "sin result.json"), failure.get("stepIndex")))
    out.append("")
    out.extend(mismatches or ["Ninguna muestra distinta de la esperada."])
    out.append("")
    if untyped:
        out.append("De las %d muestras que faltan, %d no llegaron a teclear porque el escenario falló antes de la primera tecla: %s. Muestras tecleadas: %d; con texto distinto del esperado: %d." % (
            len(runs) - ok, len(untyped), ", ".join(untyped), len(runs) - len(untyped), len(runs) - ok - len(untyped)))
        out.append("")
    return ok, len(runs)


def expected_failure(series, out):
    runs = series.of_kind("expected-failure")
    out.append("#### Fallo esperado (`poc-expected-failure`)")
    out.append("")
    if not runs:
        out.append("No se corrió.")
        out.append("")
        return None
    ok = 0
    rows = []
    for run in runs:
        result = run.result("poc-expected-failure") or {}
        failure = result.get("failure") or {}
        site = failure.get("site") or {}
        site_ok = str(site.get("file", "")).endswith(EXPECTED_FAILURE_FILE) and isinstance(site.get("line"), int)
        if series.platform == "ios":
            facts = run.xcresult().facts(True)
            cases = {case["name"]: case for case in facts.get("tests", [])}
            poc = next((case for name, case in cases.items() if "poc_expected_failure" in name), None)
            second = next((case for name, case in cases.items() if "summary_profile_picture" in name), None)
            issues = poc.get("failureMessages") if poc else None
            detail = (poc or {}).get("detail", {})
            locations = detail.get("sourceLocations", [])
            xc_site = next((loc for loc in locations if str(loc.get("file", "")).endswith(EXPECTED_FAILURE_FILE)), None)
            attachments = [a["name"] for a in detail.get("attachments", []) if a["failure"]]
            screenshot = any("screenshot" in name for name in attachments)
            hierarchy = any("hierarchy" in name for name in attachments)
            second_ran = second is not None and second.get("result") in ("Passed", "Failed")
            second_passed = second is not None and second.get("result") == "Passed"
            good = issues == 1 and xc_site is not None and screenshot and hierarchy and second_ran and not run.timed_out and (run.exit_code == 65)
            rows.append((good, "- `%s`: XCTIssue=%s, ubicación XCTest=%s, result.json=%s:%s (%s), captura=%s, jerarquía=%s, segundo test corrió=%s (resultado %s), exit=%s%s" % (
                run.name, issues, ("%s:%s" % (os.path.basename(xc_site["file"]), xc_site["line"])) if xc_site else "ninguna",
                os.path.basename(str(site.get("file", ""))), site.get("line"), failure.get("kind"), screenshot, hierarchy, second_ran,
                (second or {}).get("result"), run.exit_code, "; error: " + detail["error"] if detail.get("error") else "")))
        else:
            names = os.listdir(os.path.join(run.dir, "artifacts")) if os.path.isdir(os.path.join(run.dir, "artifacts")) else []
            png = any(name.endswith(".png") for name in names)
            uix = any(name.endswith(".uix") for name in names)
            reported = run.runner_log_contains("Tests run: 1,  Failures: 1")
            good = result.get("outcome") == "failed" and site_ok and png and uix and reported and not run.timed_out
            rows.append((good, "- `%s`: result.json=%s %s:%s, captura=%s, jerarquía=%s, `am instrument` informa 1 fallo=%s" % (
                run.name, failure.get("kind"), os.path.basename(str(site.get("file", ""))), site.get("line"), png, uix, reported)))
    ok = sum(1 for good, _ in rows if good)
    out.append("**%d/%d** corridas con un único fallo atribuido a `%s:<línea>`, adjuntos presentes y%s." % (
        ok, len(runs), EXPECTED_FAILURE_FILE, " el segundo test (`summary-profile-picture`) corrió" if series.platform == "ios" else " sin colgarse"))
    out.append("")
    out.extend(row for _, row in rows)
    out.append("")
    return ok, len(runs)


def decode_section(series, out):
    runs = series.of_kind("catalog-decode")
    out.append("#### Decodificación del catálogo (Swift -> Kotlin `ScenarioRunner.ids`)")
    out.append("")
    if not runs:
        out.append("No aplica a esta serie.")
        out.append("")
        return None
    cold, warm, synthetic_cold, synthetic_warm = [], [], [], []
    info = {}
    for run in runs:
        data = read_json(os.path.join(run.dir, "results", "catalog-decode", "decode.json"))
        if not data:
            continue
        info = data
        cold.append(data["coldMicros"] / 1000.0)
        warm.extend(value / 1000.0 for value in data["warmMicros"])
        synthetic_cold.append(data["syntheticColdMicros"] / 1000.0)
        synthetic_warm.extend(value / 1000.0 for value in data["syntheticWarmMicros"])
    out.append("| Catálogo | Bytes | Primera decodificación, mediana (ms) | máx (ms) | Decodificaciones en caliente, mediana (ms) | p95 (ms) |")
    out.append("|---|---|---|---|---|---|")
    out.append("| real, %s escenarios | %s | %s | %s | %s | %s |" % (info.get("catalogScenarios"), info.get("catalogBytes"), fmt(median(cold), 2), fmt(max(cold) if cold else None, 2), fmt(median(warm), 2), fmt(p95(warm), 2)))
    out.append("| sintético, %s escenarios x %s pasos (clon del escenario más largo con otros ids, generado en el test Swift) | %s | %s | %s | %s | %s |" % (
        info.get("syntheticScenariosDecoded"), info.get("syntheticSteps"), info.get("syntheticBytes"), fmt(median(synthetic_cold), 1), fmt(max(synthetic_cold) if synthetic_cold else None, 1), fmt(median(synthetic_warm), 1), fmt(p95(synthetic_warm), 1)))
    out.append("")
    out.append("Muestras en frío: %d procesos de prueba nuevos (una por invocación); en caliente: %d decodificaciones por catálogo y proceso. Resolución: microsegundos (reloj monotónico de Swift)." % (len(cold), len(warm) // max(1, len(cold)) if cold else 0))
    out.append("")
    return median(cold), median(synthetic_cold)


def bridge_section(series, out):
    out.append("#### Sobrecarga del puente (solo iOS)")
    out.append("")
    per_step = []
    per_call_by_primitive = {}
    run_overheads = []
    negative = 0
    leaf_steps = 0
    total_calls = 0
    call_names = {}
    for run in series.runs:
        if run.kind in ("catalog-decode",):
            continue
        for scenario in run.scenarios:
            log_path = os.path.join(run.dir, "results", scenario, "driver.log")
            result = run.result(scenario)
            if not os.path.exists(log_path) or not result:
                continue
            steps = {step["index"]: step for step in result.get("steps", [])}
            pending = []
            calls_total_us = 0
            calls_count = 0
            bridge_total = None
            with open(log_path, errors="replace") as handle:
                for line in handle:
                    line = line.rstrip("\n")
                    if line.startswith("[driver] "):
                        _, name, micros = line.split(" ")
                        pending.append((name, int(micros)))
                        calls_total_us += int(micros)
                        calls_count += 1
                        call_names[name] = call_names.get(name, 0) + 1
                    elif line.startswith("[bridge] run-total "):
                        bridge_total = int(line.split(" ")[2])
                    elif line.startswith("[") and "] " in line and " -> " in line:
                        index = int(line[1:line.index("]")])
                        step = steps.get(index)
                        if step is not None and step["primitive"] not in CONTAINERS and pending:
                            driver_us = sum(micros for _, micros in pending)
                            overhead_ms = step["durationMs"] - driver_us / 1000.0
                            leaf_steps += 1
                            if overhead_ms < 0:
                                negative += 1
                            per_step.append(overhead_ms / len(pending))
                            per_call_by_primitive.setdefault(step["primitive"], []).append(overhead_ms / len(pending))
                        pending = []
            if bridge_total is not None and calls_count and result.get("outcome") == "passed":
                run_overheads.append((bridge_total - calls_total_us) / 1000.0 / calls_count)
                total_calls += calls_count
    if not per_step:
        out.append("Sin datos (no hay `driver.log` con `[driver]`).")
        out.append("")
        return None
    out.append("Por paso: (duración del paso según el intérprete, `result.json`) - (suma de las llamadas al driver medidas en Swift de ese paso), dividido entre el número de llamadas. Las llamadas se atribuyen al paso cuyo `[i] ...` el intérprete registra justo después.")
    out.append("Agregado por corrida (solo corridas verdes; la captura de fallo no es del puente): (tiempo Swift de todo `ScenarioRunner.run`, incluida la decodificación del catálogo y `prepareBackend`) - (suma de llamadas al driver), dividido entre el número de llamadas.")
    out.append("")
    out.append("| Medida | n | mediana (ms/llamada) | p95 (ms/llamada) |")
    out.append("|---|---|---|---|")
    out.append("| por paso (hojas con llamadas) | %d | %s | %s |" % (len(per_step), fmt(median(per_step), 2), fmt(p95(per_step), 2)))
    out.append("| agregado por corrida | %d | %s | %s |" % (len(run_overheads), fmt(median(run_overheads), 3), fmt(p95(run_overheads), 3)))
    for primitive in sorted(per_call_by_primitive):
        values = per_call_by_primitive[primitive]
        out.append("| por paso, `%s` | %d | %s | %s |" % (primitive, len(values), fmt(median(values), 2), fmt(p95(values), 2)))
    out.append("")
    out.append("Resolución efectiva: el reloj del intérprete en `result.json` es de milisegundos enteros (truncado), así que la medida por paso tiene una incertidumbre de hasta 1 ms por paso (repartida entre sus llamadas); %d de %d pasos dieron sobrecarga negativa por ese truncamiento. El agregado por corrida usa microsegundos de Swift en ambos lados y es la medida de mayor resolución. Llamadas totales medidas: %d (%s)." % (
        negative, leaf_steps, total_calls, ", ".join("%s=%d" % item for item in sorted(call_names.items()))))
    out.append("")
    return median(per_step), median(run_overheads)


def normal_verdicts(series, out):
    ok = 0
    rows = []
    scenario_runs = [run for run in series.runs if run.kind != "catalog-decode"]
    for run in scenario_runs:
        facts = run.xcresult().facts(False)
        summary = facts.get("summary", {})
        expected = len(run.scenarios)
        counted = (summary.get("passedTests") or 0) + (summary.get("failedTests") or 0)
        crashed = run.runner_log_contains("crashed", "Restarting after unexpected exit", "Test crashed")
        normal = (not run.timed_out) and run.exit_code in (0, 65) and counted == expected and not crashed
        if normal:
            ok += 1
        else:
            rows.append("- `%s`: exit=%s, timed_out=%s, casos con veredicto %s de %s, crash en el log=%s" % (run.name, run.exit_code, run.timed_out, counted, expected, crashed))
    core = [run for run in scenario_runs if run.env.get("block") in ("1", "2", "3")]
    core_ok = sum(1 for run in core if run.xcresult().facts(False).get("summary", {}).get("passedTests", 0) + run.xcresult().facts(False).get("summary", {}).get("failedTests", 0) == len(run.scenarios) and not run.timed_out and run.exit_code in (0, 65))
    out.append("#### Veredicto normal de XCTest")
    out.append("")
    out.append("**%d/%d** invocaciones del escenario terminaron con veredicto normal (sin colgarse, exit 0 o 65, todos los casos con resultado, sin crash en el log); de ellas, %d/%d son de los tres escenarios de la puerta (bloques de 20 corridas: auth-login-cancel, evaluations-swipe-delete, summary-profile-picture)." % (ok, len(scenario_runs), core_ok, len(core)))
    out.append("")
    out.extend(rows or ["Ninguna invocación anormal."])
    out.append("")
    return core_ok, len(core), ok, len(scenario_runs)


def link_check(series, out):
    out.append("#### Enlace de `ScenarioKit` (criterio 1)")
    out.append("")
    derived = series.env.get("ios_derived_data", "")
    app = os.path.join(derived, "Build/Products/Debug-iphonesimulator/TuIndiceHost.app")
    root = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
    script = os.path.join(root, "iosApp/scripts/verify-ui-test-target.sh")
    code, stdout, stderr = run_tool(["bash", script, "--app", app])
    out.append("`iosApp/scripts/verify-ui-test-target.sh --app %s` -> exit %d" % (app, code))
    out.append("")
    out.append("```")
    out.extend(stdout.strip().splitlines())
    if stderr.strip():
        out.extend(stderr.strip().splitlines())
    out.append("```")
    out.append("")
    bundle = os.path.join(os.path.dirname(app), "TuIndiceUITests-Runner.app/PlugIns/TuIndiceUITests.xctest")
    binary = os.path.join(bundle, "TuIndiceUITests")
    framework_dir = os.path.exists(os.path.join(bundle, "Frameworks", "ScenarioKit.framework"))
    _, nm_out, _ = run_tool(["nm", binary])
    symbols = sum(1 for line in nm_out.splitlines() if "ScenarioKit" in line)
    _, otool_out, _ = run_tool(["otool", "-L", binary])
    dynamic = any("ScenarioKit" in line for line in otool_out.splitlines())
    out.append("Bundle de pruebas `%s`: símbolos con `ScenarioKit` en el binario = %d; dependencia dinámica de `ScenarioKit` (otool -L) = %s; `Frameworks/ScenarioKit.framework` embebido = %s." % (binary, symbols, dynamic, framework_dir))
    out.append("")
    framework_dir = framework_dir or dynamic
    return code == 0 and symbols > 0 and not framework_dir, code, symbols, framework_dir


def environment_section(series, out):
    env = series.env
    out.append("### Serie `%s` (%s)" % (series.name, series.platform))
    out.append("")
    loads = load_values(series)
    out.append("- Host: %s, %s núcleos, %s GB de memoria, %s, kernel %s" % (env.get("host_model"), env.get("host_cores"), int(env.get("host_memory_bytes", "0")) // (1 << 30), env.get("host_os"), env.get("host_kernel")))
    out.append("- Commit medido: `%s` (archivos sin commitear al medir: %s)" % (env.get("git_head"), env.get("git_dirty")))
    out.append("- Plan: `%s`; timeout duro %s s; puerto de WireMock %s (perfil de retardo `fast`, el del harness)" % (env.get("plan"), env.get("timeout_seconds"), env.get("wiremock_port")))
    if series.platform == "android":
        out.append("- AVD `%s`, imagen `%s`, Android %s (API %s), %s, ABI %s, página de %s bytes, RAM %s MB, %s núcleos" % (
            env.get("android_avd"), env.get("android_image_sysdir"), env.get("android_release"), env.get("android_sdk"), env.get("android_model"),
            env.get("android_abi"), env.get("android_page_size"), env.get("android_hw_ram"), env.get("android_hw_cores")))
        out.append("- Huella del sistema: `%s`; serial `%s`" % (env.get("android_fingerprint"), env.get("android_serial")))
        out.append("- APK sha256: app `%s`, runner `%s`" % (env.get("apk_app_sha256"), env.get("apk_test_sha256")))
    else:
        out.append("- %s; runtime `%s`; dispositivo `%s` (`%s`); trozo de tecleo %s" % (env.get("xcode"), env.get("ios_runtime"), env.get("ios_device"), env.get("ios_udid"), env.get("ios_type_chunk")))
    if series.platform == "ios":
        out.append("- Trazas del driver activas (`TEST_RUNNER_E2E_TRACE=1`) en todas las corridas iOS; el driver Swift y el catálogo medidos son los del árbol de trabajo (los cambios de instrumentación van en el commit de esta fase).")
    out.append("- Espera de carga antes de empezar: %s" % env.get("load_wait", "n/a"))
    out.append("- Inicio de la serie: %s (`%s`)" % (env.get("series_start"), env.get("uptime_start")))
    out.append("- Fin de la serie: %s (`%s`)" % (env.get("series_end"), env.get("uptime_end")))
    out.append("- Carga load1 por corrida (inicio y fin de cada una): mínima %s, máxima **%s**, mediana %s; máximo muestreado por el script %s" % (
        fmt(min(loads) if loads else None, 2), fmt(max(loads) if loads else None, 2), fmt(median(loads), 2), env.get("load1_max_sampled")))
    out.append("- Corridas ejecutadas: %s; serie detenida: %s" % (env.get("runs_executed"), env.get("stopped")))
    out.append("")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--out", required=True)
    args = parser.parse_args()
    out_dir = os.path.abspath(args.out)

    series_list = []
    for entry in sorted(os.listdir(out_dir)):
        path = os.path.join(out_dir, entry)
        if os.path.isdir(path) and os.path.exists(os.path.join(path, "series.env")):
            series_list.append(Series(path))
    if not series_list:
        sys.exit("no series under " + out_dir)

    lines = ["# Informe de medición de los runners nativos (F13)", ""]
    lines.append("Solo hechos medidos. Percentil 95 = rango más cercano. Cada corrida es una invocación del runner con la limpieza del harness previa; ninguna se repitió.")
    lines.append("")
    gate = {}
    for series in series_list:
        environment_section(series, lines)
        per_kind = pass_rates(series, lines)
        not_green_section(series, lines)
        overheads = wall_times(series, lines)
        tap_median, tap_n = primitives(series, lines)
        typing_ok, typing_total = typing(series, lines)
        failure_ok = expected_failure(series, lines)
        entry = {"per_kind": per_kind, "tap": (tap_median, tap_n), "typing": (typing_ok, typing_total),
                 "failure": failure_ok, "xcodebuild_overhead": median(overheads) if overheads else None}
        if series.platform == "ios":
            entry["decode"] = decode_section(series, lines)
            entry["bridge"] = bridge_section(series, lines)
            entry["verdicts"] = normal_verdicts(series, lines)
            entry["link"] = link_check(series, lines)
            if entry["xcodebuild_overhead"] is not None and entry["xcodebuild_overhead"] > XCODEBUILD_OVERHEAD_CONSULT_S:
                lines.append("> **PUNTO DE CONSULTA DEL PLAN:** la sobrecarga mediana de `xcodebuild` por invocación es %.1f s (> %.0f s)." % (entry["xcodebuild_overhead"], XCODEBUILD_OVERHEAD_CONSULT_S))
                lines.append("")
        gate[series.name] = (series, entry)

    lines.append("## Criterios de la puerta")
    lines.append("")
    lines.append("Valores medidos y si cumplen el umbral; sin veredicto global.")
    lines.append("")
    lines.append("| Serie | Criterio | Umbral | Valor medido | Cumple |")
    lines.append("|---|---|---|---|---|")

    def row(series, criterion, threshold, value, meets):
        lines.append("| %s | %s | %s | %s | %s |" % (series, criterion, threshold, value, "sí" if meets is True else ("no" if meets is False else "n/a")))

    for name, (series, entry) in gate.items():
        if series.platform == "ios":
            linked, code, symbols, framework_dir = entry["link"]
            row(name, "Go 1: `ScenarioKit` enlaza estático en el bundle y no aparece en la app", "obligatorio", "verify-ui-test-target exit %d; %d símbolos en el bundle; enlace/embebido dinámico: %s" % (code, symbols, framework_dir), linked)
            core_ok, core_total, all_ok, all_total = entry["verdicts"]
            row(name, "Go 2: corridas con veredicto normal de XCTest (tres escenarios)", "60/60", "%d/%d en los tres escenarios x 20 (todas las invocaciones, con las 10 extra de auth y las 10 del fallo esperado: %d/%d)" % (core_ok, core_total, all_ok, all_total), core_ok == 60 and core_total == 60)
            if entry["failure"]:
                ok, total = entry["failure"]
                row(name, "Go 3: fallo esperado con un XCTIssue en archivo:línea Kotlin, adjuntos, y el siguiente test corre", "10/10", "%d/%d" % (ok, total), ok == 10 and total == 10)
            if entry["bridge"]:
                per_step, aggregated = entry["bridge"]
                row(name, "Go 4a: sobrecarga del puente por llamada, mediana por paso", "<= 5 ms", fmt(per_step, 2, " ms"), per_step is not None and per_step <= BRIDGE_MS_LIMIT)
                row(name, "Go 4a: sobrecarga del puente por llamada, agregado por corrida (µs)", "<= 5 ms", fmt(aggregated, 3, " ms"), aggregated is not None and aggregated <= BRIDGE_MS_LIMIT)
            if entry["decode"]:
                real, synthetic = entry["decode"]
                row(name, "Go 4b: decodificar el catálogo real (4 escenarios), primera vez", "<= 500 ms", fmt(real, 2, " ms"), real is not None and real <= DECODE_MS_LIMIT)
                row(name, "Go 4b (extra): catálogo sintético 100 x 30, primera vez", "<= 500 ms", fmt(synthetic, 1, " ms"), synthetic is not None and synthetic <= DECODE_MS_LIMIT)
            if entry["xcodebuild_overhead"] is not None:
                row(name, "Sobrecarga de `xcodebuild` por invocación, mediana (consulta si > 40 s)", "<= 40 s", fmt(entry["xcodebuild_overhead"], 1, " s"), entry["xcodebuild_overhead"] <= XCODEBUILD_OVERHEAD_CONSULT_S)
        else:
            if entry["failure"]:
                ok, total = entry["failure"]
                row(name, "Fallo esperado con archivo:línea Kotlin y adjuntos (informativo en Android)", "10/10", "%d/%d" % (ok, total), ok == 10 and total == 10)
        for kind, (green, total) in entry["per_kind"].items():
            row(name, "Salud: `%s` aprobadas (bloque de 20)" % kind, ">= 19/20", "%d/%d" % (green, total), green >= PASS_RATE_MIN * total / 20.0)
        tap_median, tap_n = entry["tap"]
        row(name, "Salud: mediana de `Tap` en inicio de sesión", "<= 1500 ms", "%s ms (n=%d)" % (fmt(tap_median, 0), tap_n), tap_median is not None and tap_median <= TAP_MS_LIMIT)
        evaluations = [run for run in series.of_kind("evaluations-swipe-delete")]
        if evaluations:
            streak = best = 0
            breakers = []
            for run in evaluations:
                streak = streak + 1 if run.is_green() else 0
                best = max(best, streak)
                if not run.is_green():
                    kind_text = (run.result(run.primary) or {}).get("failure", {}) or {}
                    breakers.append("%s %s" % (run.name.split("-")[0], kind_text.get("kind", "sin result.json")))
            row(name, "Salud: `evaluations-swipe-delete` aprobada en corridas consecutivas (reinicio del backend)", "todas consecutivas", "racha máxima %d de %d%s" % (best, len(evaluations), (" (no verdes: " + ", ".join(breakers) + ")") if breakers else ""), best == len(evaluations))
        typing_ok, typing_total = entry["typing"]
        row(name, "Tecleo correcto en `auth-login-cancel`", "30/30", "%d/%d" % (typing_ok, typing_total), typing_ok == typing_total and typing_total >= 30)

    with open(os.path.join(out_dir, "report.md"), "w") as handle:
        handle.write("\n".join(lines) + "\n")
    print("wrote " + os.path.join(out_dir, "report.md"))


if __name__ == "__main__":
    main()
