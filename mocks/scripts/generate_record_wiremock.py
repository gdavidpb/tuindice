#!/usr/bin/env python3

from __future__ import annotations

import argparse
import json
import shutil
from pathlib import Path


GET_LIST_DELAY_MS = 3000
GET_SINGLE_DELAY_MS = 3000
PATCH_DELAY_MS = 1500
DELETE_DELAY_MS = 1000
POST_DELAY_MS = 1200

SUCCESS_PRIORITY = 1
STATE_PRIORITY = 5
FALLBACK_PRIORITY = 9

MIN_GRADE = 0
MAX_GRADE = 5

ADDED_QUARTER_ID = "MOCK-ADDED-QUARTER"
ADDED_QUARTER_SCENARIO = "record-quarter-MOCK-ADDED-QUARTER"
ADDED_QUARTER_PRESENT_STATE = "ADDED_R1"
ADDED_QUARTER_DELETED_STATE = "DELETED_R2"

DELETABLE_QUARTER_ID = "Q2026B"
DELETABLE_QUARTER_SCENARIO = "record-quarter-Q2026B"
DELETABLE_QUARTER_DELETED_STATE = "DELETED_R11"

GET_LIST_BODY = "{{{toJson record.quarters}}}"
GET_SINGLE_BODY = "{{{toJson record.currentQuarter}}}"
PATCH_SUCCESS_BODY = (
    "{\"mutation_id\":\"{{jsonPath request.body '$.mutation_id'}}\","
    "\"subject_patch\":{{{toJson record.currentSubject}}},"
    "\"affected_quarters\":{{{toJson record.quarters}}} }"
)
DELETE_SUCCESS_BODY = (
    "{\"mutation_id\":\"{{jsonPath request.body '$.mutation_id'}}\","
    "\"removed_quarter_id\":\"{{request.pathSegments.[2]}}\","
    "\"affected_quarters\":{{{toJson record.quarters}}} }"
)
POST_SUCCESS_BODY = (
    "{\"mutation_id\":\"{{jsonPath request.body '$.mutation_id'}}\","
    "\"quarter_patch\":{{{toJson record.addedQuarter}}},"
    "\"affected_quarters\":{{{toJson record.quarters}}} }"
)


def scenario_subject_state(revision: int, grade: int) -> str:
    return f"REV_{revision}_GRADE_{grade}"


def ensure_clean_directory(directory: Path) -> None:
    if directory.exists():
        shutil.rmtree(directory)
    directory.mkdir(parents=True, exist_ok=True)


def load_state(source_root: Path) -> dict:
    config_path = source_root / "config" / "record-base-state.json"
    return json.loads(config_path.read_text())


def copy_runtime_assets(source_root: Path, runtime_root: Path) -> None:
    ensure_clean_directory(runtime_root)
    shutil.copytree(source_root / "__files", runtime_root / "__files")
    shutil.copytree(source_root / "config", runtime_root / "config")
    shutil.copytree(source_root / "config", runtime_root / "__files" / "config")
    shutil.copytree(source_root / "mappings", runtime_root / "mappings")
    shutil.rmtree(runtime_root / "mappings" / "record")
    (runtime_root / "mappings" / "record").mkdir(parents=True, exist_ok=True)


def write_mapping(target_dir: Path, filename: str, mapping: dict) -> None:
    (target_dir / filename).write_text(json.dumps(mapping, indent=2) + "\n")


def response(status: int, body: str, delay_ms: int) -> dict:
    return {
        "status": status,
        "headers": {
            "Content-Type": "application/json"
        },
        "body": body,
        "transformers": [
            "response-template"
        ],
        "fixedDelayMilliseconds": delay_ms
    }


def matches_json_path_exists(path: str) -> dict:
    return {"matchesJsonPath": path}


def matches_number(path: str, value: int) -> dict:
    return {"matchesJsonPath": f"$[?(@.{path} == {value})]"}


def max_quarter_revision(base_state: dict) -> int:
    return max((quarter["revision"] for quarter in base_state["quarters"]), default=0)


def build_get_mappings(target_dir: Path, base_state: dict) -> None:
    write_mapping(
        target_dir,
        "get-quarters-dynamic.json",
        {
            "priority": SUCCESS_PRIORITY,
            "request": {
                "method": "GET",
                "urlPath": "/quarters/v1"
            },
            "response": response(
                status=200,
                body=GET_LIST_BODY,
                delay_ms=GET_LIST_DELAY_MS
            )
        }
    )

    for quarter in base_state["quarters"]:
        if quarter["id"] == DELETABLE_QUARTER_ID:
            write_mapping(
                target_dir,
                "get-quarter-q2026b-present.json",
                {
                    "priority": SUCCESS_PRIORITY,
                    "scenarioName": DELETABLE_QUARTER_SCENARIO,
                    "requiredScenarioState": "Started",
                    "request": {
                        "method": "GET",
                        "urlPath": f"/quarters/v1/{quarter['id']}"
                    },
                    "response": response(
                        status=200,
                        body=GET_SINGLE_BODY,
                        delay_ms=GET_SINGLE_DELAY_MS
                    )
                }
            )
            write_mapping(
                target_dir,
                "get-quarter-q2026b-missing.json",
                {
                    "priority": FALLBACK_PRIORITY,
                    "request": {
                        "method": "GET",
                        "urlPath": f"/quarters/v1/{quarter['id']}"
                    },
                    "response": response(
                        status=404,
                        body="{\"error\":\"quarter_not_found\"}",
                        delay_ms=GET_SINGLE_DELAY_MS
                    )
                }
            )
            continue

        write_mapping(
            target_dir,
            f"get-quarter-{quarter['id'].lower()}-dynamic.json",
            {
                "priority": SUCCESS_PRIORITY,
                "request": {
                    "method": "GET",
                    "urlPath": f"/quarters/v1/{quarter['id']}"
                },
                "response": response(
                    status=200,
                    body=GET_SINGLE_BODY,
                    delay_ms=GET_SINGLE_DELAY_MS
                )
            }
        )

    write_mapping(
        target_dir,
        "get-quarter-added-present.json",
        {
            "priority": SUCCESS_PRIORITY,
            "scenarioName": ADDED_QUARTER_SCENARIO,
            "requiredScenarioState": ADDED_QUARTER_PRESENT_STATE,
            "request": {
                "method": "GET",
                "urlPath": f"/quarters/v1/{ADDED_QUARTER_ID}"
            },
            "response": response(
                status=200,
                body=GET_SINGLE_BODY,
                delay_ms=GET_SINGLE_DELAY_MS
            )
        }
    )
    write_mapping(
        target_dir,
        "get-quarter-added-missing.json",
        {
            "priority": FALLBACK_PRIORITY,
            "request": {
                "method": "GET",
                "urlPath": f"/quarters/v1/{ADDED_QUARTER_ID}"
            },
            "response": response(
                status=404,
                body="{\"error\":\"quarter_not_found\"}",
                delay_ms=GET_SINGLE_DELAY_MS
            )
        }
    )


def build_patch_mappings(target_dir: Path, base_state: dict, max_revision: int) -> None:
    mutable_subjects = [
        subject
        for quarter in base_state["quarters"]
        for subject in quarter["subjects"]
        if subject.get("mutable")
    ]

    for subject in mutable_subjects:
        url_path = f"/quarters/v1/{subject['qid']}/subjects/{subject['id']}"
        base_revision = subject["revision"]

        for new_grade in range(MIN_GRADE, MAX_GRADE + 1):
            write_mapping(
                target_dir,
                f"patch-{subject['id'].lower()}-started-g{new_grade}.json",
                {
                    "priority": SUCCESS_PRIORITY,
                    "scenarioName": subject["scenario"],
                    "requiredScenarioState": "Started",
                    "newScenarioState": scenario_subject_state(base_revision + 1, new_grade),
                    "request": {
                        "method": "PATCH",
                        "urlPath": url_path,
                        "bodyPatterns": [
                            matches_json_path_exists("$.mutation_id"),
                            matches_number("expected_revision", base_revision),
                            matches_number("grade", new_grade)
                        ]
                    },
                    "response": response(
                        status=200,
                        body=PATCH_SUCCESS_BODY,
                        delay_ms=PATCH_DELAY_MS
                    )
                }
            )

        for current_revision in range(base_revision + 1, max_revision + 1):
            next_revision = min(current_revision + 1, max_revision)
            for current_grade in range(MIN_GRADE, MAX_GRADE + 1):
                required_state = scenario_subject_state(current_revision, current_grade)
                for new_grade in range(MIN_GRADE, MAX_GRADE + 1):
                    write_mapping(
                        target_dir,
                        (
                            f"patch-{subject['id'].lower()}-r{current_revision}"
                            f"-g{current_grade}-to-g{new_grade}.json"
                        ),
                        {
                            "priority": SUCCESS_PRIORITY,
                            "scenarioName": subject["scenario"],
                            "requiredScenarioState": required_state,
                            "newScenarioState": scenario_subject_state(next_revision, new_grade),
                            "request": {
                                "method": "PATCH",
                                "urlPath": url_path,
                                "bodyPatterns": [
                                    matches_json_path_exists("$.mutation_id"),
                                    matches_number("expected_revision", current_revision),
                                    matches_number("grade", new_grade)
                                ]
                            },
                            "response": response(
                                status=200,
                                body=PATCH_SUCCESS_BODY,
                                delay_ms=PATCH_DELAY_MS
                            )
                        }
                    )

        write_mapping(
            target_dir,
            f"patch-{subject['id'].lower()}-precondition-failed.json",
            {
                "priority": FALLBACK_PRIORITY,
                "request": {
                    "method": "PATCH",
                    "urlPath": url_path,
                    "bodyPatterns": [
                        matches_json_path_exists("$.mutation_id"),
                        matches_json_path_exists("$.expected_revision"),
                        matches_json_path_exists("$.grade")
                    ]
                },
                "response": response(
                    status=412,
                    body="{\"error\":\"precondition_failed\"}",
                    delay_ms=PATCH_DELAY_MS
                )
            }
        )


def build_delete_mappings(target_dir: Path) -> None:
    write_mapping(
        target_dir,
        "delete-quarter-q2026b-success.json",
        {
            "priority": SUCCESS_PRIORITY,
            "scenarioName": DELETABLE_QUARTER_SCENARIO,
            "requiredScenarioState": "Started",
            "newScenarioState": DELETABLE_QUARTER_DELETED_STATE,
            "request": {
                "method": "DELETE",
                "urlPath": f"/quarters/v1/{DELETABLE_QUARTER_ID}",
                "bodyPatterns": [
                    matches_json_path_exists("$.mutation_id"),
                    matches_number("expected_revision", 10)
                ]
            },
            "response": response(
                status=200,
                body=DELETE_SUCCESS_BODY,
                delay_ms=DELETE_DELAY_MS
            )
        }
    )
    write_mapping(
        target_dir,
        "delete-quarter-q2026b-not-found.json",
        {
            "priority": STATE_PRIORITY,
            "scenarioName": DELETABLE_QUARTER_SCENARIO,
            "requiredScenarioState": DELETABLE_QUARTER_DELETED_STATE,
            "request": {
                "method": "DELETE",
                "urlPath": f"/quarters/v1/{DELETABLE_QUARTER_ID}"
            },
            "response": response(
                status=404,
                body="{\"error\":\"quarter_not_found\"}",
                delay_ms=DELETE_DELAY_MS
            )
        }
    )
    write_mapping(
        target_dir,
        "delete-quarter-q2026b-precondition-failed.json",
        {
            "priority": FALLBACK_PRIORITY,
            "request": {
                "method": "DELETE",
                "urlPath": f"/quarters/v1/{DELETABLE_QUARTER_ID}",
                "bodyPatterns": [
                    matches_json_path_exists("$.mutation_id"),
                    matches_json_path_exists("$.expected_revision")
                ]
            },
            "response": response(
                status=412,
                body="{\"error\":\"precondition_failed\"}",
                delay_ms=DELETE_DELAY_MS
            )
        }
    )

    write_mapping(
        target_dir,
        "delete-added-quarter-success.json",
        {
            "priority": SUCCESS_PRIORITY,
            "scenarioName": ADDED_QUARTER_SCENARIO,
            "requiredScenarioState": ADDED_QUARTER_PRESENT_STATE,
            "newScenarioState": ADDED_QUARTER_DELETED_STATE,
            "request": {
                "method": "DELETE",
                "urlPath": f"/quarters/v1/{ADDED_QUARTER_ID}",
                "bodyPatterns": [
                    matches_json_path_exists("$.mutation_id"),
                    matches_number("expected_revision", 1)
                ]
            },
            "response": response(
                status=200,
                body=DELETE_SUCCESS_BODY,
                delay_ms=DELETE_DELAY_MS
            )
        }
    )
    for state_name, suffix in (
        ("Started", "absent"),
        (ADDED_QUARTER_DELETED_STATE, "deleted"),
    ):
        write_mapping(
            target_dir,
            f"delete-added-quarter-{suffix}.json",
            {
                "priority": STATE_PRIORITY,
                "scenarioName": ADDED_QUARTER_SCENARIO,
                "requiredScenarioState": state_name,
                "request": {
                    "method": "DELETE",
                    "urlPath": f"/quarters/v1/{ADDED_QUARTER_ID}"
                },
                "response": response(
                    status=404,
                    body="{\"error\":\"quarter_not_found\"}",
                    delay_ms=DELETE_DELAY_MS
                )
            }
        )
    write_mapping(
        target_dir,
        "delete-added-quarter-precondition-failed.json",
        {
            "priority": FALLBACK_PRIORITY,
            "request": {
                "method": "DELETE",
                "urlPath": f"/quarters/v1/{ADDED_QUARTER_ID}",
                "bodyPatterns": [
                    matches_json_path_exists("$.mutation_id"),
                    matches_json_path_exists("$.expected_revision")
                ]
            },
            "response": response(
                status=412,
                body="{\"error\":\"precondition_failed\"}",
                delay_ms=DELETE_DELAY_MS
            )
        }
    )


def build_post_mappings(target_dir: Path, base_state: dict) -> None:
    expected_revision = max_quarter_revision(base_state)
    shared_request = {
        "method": "POST",
        "urlPath": "/quarters/v1",
        "bodyPatterns": [
            matches_json_path_exists("$.quarter"),
            matches_json_path_exists("$.year"),
            matches_json_path_exists("$.subjects"),
            matches_json_path_exists("$.subjects[0].code"),
            matches_json_path_exists("$.subjects[0].grade"),
            matches_json_path_exists("$.mutation_id"),
            matches_number("expected_revision", expected_revision)
        ]
    }

    for state_name, suffix in (
        ("Started", "started"),
        (ADDED_QUARTER_DELETED_STATE, "recreate"),
    ):
        write_mapping(
            target_dir,
            f"post-quarter-success-{suffix}.json",
            {
                "priority": SUCCESS_PRIORITY,
                "scenarioName": ADDED_QUARTER_SCENARIO,
                "requiredScenarioState": state_name,
                "newScenarioState": ADDED_QUARTER_PRESENT_STATE,
                "request": shared_request,
                "response": response(
                    status=200,
                    body=POST_SUCCESS_BODY,
                    delay_ms=POST_DELAY_MS
                )
            }
        )

    write_mapping(
        target_dir,
        "post-quarter-conflict.json",
        {
            "priority": STATE_PRIORITY,
            "scenarioName": ADDED_QUARTER_SCENARIO,
            "requiredScenarioState": ADDED_QUARTER_PRESENT_STATE,
            "request": shared_request,
            "response": response(
                status=412,
                body="{\"error\":\"quarter_already_exists\"}",
                delay_ms=POST_DELAY_MS
            )
        }
    )


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--source-root", required=True)
    parser.add_argument("--runtime-root", required=True)
    parser.add_argument("--max-revision", type=int, default=20)
    args = parser.parse_args()

    source_root = Path(args.source_root).resolve()
    runtime_root = Path(args.runtime_root).resolve()
    base_state = load_state(source_root)

    copy_runtime_assets(source_root, runtime_root)

    record_mappings_dir = runtime_root / "mappings" / "record"
    build_get_mappings(record_mappings_dir, base_state)
    build_patch_mappings(record_mappings_dir, base_state, max_revision=max(args.max_revision, 2))
    build_delete_mappings(record_mappings_dir)
    build_post_mappings(record_mappings_dir, base_state)


if __name__ == "__main__":
    main()
