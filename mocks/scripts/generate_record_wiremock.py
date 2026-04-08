#!/usr/bin/env python3

from __future__ import annotations

import argparse
import json
import shutil
from pathlib import Path


GET_DELAY_MS = 3000
PUT_DELAY_MS = 1500
DELETE_DELAY_MS = 1000
POST_DELAY_MS = 1200

SUCCESS_PRIORITY = 1
STATE_PRIORITY = 5
FALLBACK_PRIORITY = 9

MIN_GRADE = 0
MAX_GRADE = 5
QUALITATIVE_OUTCOMES = ("pending", "approved", "failed", "retired")

ADDED_TERM_ID = "MOCK-ADDED-QUARTER"
ADDED_TERM_SCENARIO = "record-term-MOCK-ADDED-QUARTER"
ADDED_TERM_PRESENT_STATE = "ADDED_R1"
ADDED_TERM_DELETED_STATE = "DELETED_R2"

RECORD_BODY = "{{{toJson record}}}"


def scenario_attempt_grade_state(revision: int, grade: int) -> str:
	return f"REV_{revision}_GRADE_{grade}"


def scenario_attempt_status_state(revision: int, outcome: str) -> str:
	return f"REV_{revision}_STATUS_{outcome.upper()}"


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


def build_get_mapping(target_dir: Path) -> None:
	write_mapping(
		target_dir,
		"get-record-success.json",
		{
				"priority": SUCCESS_PRIORITY,
				"request": {
					"method": "GET",
					"urlPath": "/record/v2"
				},
			"response": response(
				status=200,
				body=RECORD_BODY,
				delay_ms=GET_DELAY_MS
			)
		}
	)


def build_put_mappings(target_dir: Path, base_state: dict, max_revision: int) -> None:
	mutable_attempts = [
		attempt
		for term in base_state["terms"]
		for attempt in term["attempts"]
		if attempt.get("mutable")
	]

	for attempt in mutable_attempts:
		url_path = f"/record/v2/overlay/attempts/{attempt['id']}"
		base_revision = attempt["revision"]
		grading_mode = attempt.get("grading_mode", "numeric")

		if grading_mode == "qualitative_pass_fail":
			for outcome in QUALITATIVE_OUTCOMES:
				write_mapping(
					target_dir,
					f"put-{attempt['id'].lower()}-started-{outcome}.json",
					{
						"priority": SUCCESS_PRIORITY,
						"scenarioName": attempt["scenario"],
						"requiredScenarioState": "Started",
						"newScenarioState": scenario_attempt_status_state(base_revision + 1, outcome),
						"request": {
							"method": "PUT",
							"urlPath": url_path,
							"bodyPatterns": [
								{"matchesJsonPath": f"$[?(@.outcome == '{outcome}')]"}
							]
						},
						"response": response(
							status=200,
							body=RECORD_BODY,
							delay_ms=PUT_DELAY_MS
						)
					}
				)

			for current_revision in range(base_revision + 1, max_revision + 1):
				next_revision = min(current_revision + 1, max_revision)
				for current_outcome in QUALITATIVE_OUTCOMES:
					required_state = scenario_attempt_status_state(current_revision, current_outcome)
					for next_outcome in QUALITATIVE_OUTCOMES:
						write_mapping(
							target_dir,
							f"put-{attempt['id'].lower()}-r{current_revision}-{current_outcome}-to-{next_outcome}.json",
							{
								"priority": SUCCESS_PRIORITY,
								"scenarioName": attempt["scenario"],
								"requiredScenarioState": required_state,
								"newScenarioState": scenario_attempt_status_state(next_revision, next_outcome),
								"request": {
									"method": "PUT",
									"urlPath": url_path,
									"bodyPatterns": [
										{"matchesJsonPath": f"$[?(@.outcome == '{next_outcome}')]"}
									]
								},
								"response": response(
									status=200,
									body=RECORD_BODY,
									delay_ms=PUT_DELAY_MS
								)
							}
						)
		else:
			for new_grade in range(MIN_GRADE, MAX_GRADE + 1):
				write_mapping(
					target_dir,
					f"put-{attempt['id'].lower()}-started-g{new_grade}.json",
					{
						"priority": SUCCESS_PRIORITY,
						"scenarioName": attempt["scenario"],
						"requiredScenarioState": "Started",
						"newScenarioState": scenario_attempt_grade_state(base_revision + 1, new_grade),
						"request": {
							"method": "PUT",
							"urlPath": url_path,
							"bodyPatterns": [
								matches_number("score.numeric_value", new_grade)
							]
						},
						"response": response(
							status=200,
							body=RECORD_BODY,
							delay_ms=PUT_DELAY_MS
						)
					}
				)

			for current_revision in range(base_revision + 1, max_revision + 1):
				next_revision = min(current_revision + 1, max_revision)
				for current_grade in range(MIN_GRADE, MAX_GRADE + 1):
					required_state = scenario_attempt_grade_state(current_revision, current_grade)
					for new_grade in range(MIN_GRADE, MAX_GRADE + 1):
						write_mapping(
							target_dir,
							f"put-{attempt['id'].lower()}-r{current_revision}-g{current_grade}-to-g{new_grade}.json",
							{
								"priority": SUCCESS_PRIORITY,
								"scenarioName": attempt["scenario"],
								"requiredScenarioState": required_state,
								"newScenarioState": scenario_attempt_grade_state(next_revision, new_grade),
								"request": {
									"method": "PUT",
									"urlPath": url_path,
									"bodyPatterns": [
										matches_number("score.numeric_value", new_grade)
									]
								},
								"response": response(
									status=200,
									body=RECORD_BODY,
									delay_ms=PUT_DELAY_MS
								)
							}
						)

		write_mapping(
			target_dir,
			f"put-{attempt['id'].lower()}-fallback.json",
			{
				"priority": FALLBACK_PRIORITY,
				"request": {
					"method": "PUT",
					"urlPath": url_path
				},
				"response": response(
					status=412,
					body="{\"error\":\"precondition_failed\"}",
					delay_ms=PUT_DELAY_MS
				)
			}
		)


def build_delete_mappings(target_dir: Path) -> None:
	for term_id, scenario_name, deleted_state in (
		(ADDED_TERM_ID, ADDED_TERM_SCENARIO, ADDED_TERM_DELETED_STATE),
	):
		write_mapping(
			target_dir,
			f"delete-{term_id.lower()}-success.json",
			{
				"priority": SUCCESS_PRIORITY,
				"scenarioName": scenario_name,
				"requiredScenarioState": ADDED_TERM_PRESENT_STATE,
				"newScenarioState": deleted_state,
				"request": {
					"method": "DELETE",
					"urlPath": f"/record/v2/overlay/terms/{term_id}"
				},
				"response": response(
					status=200,
					body=RECORD_BODY,
					delay_ms=DELETE_DELAY_MS
				)
			}
		)
		write_mapping(
			target_dir,
			f"delete-{term_id.lower()}-missing.json",
			{
				"priority": STATE_PRIORITY,
				"scenarioName": scenario_name,
				"requiredScenarioState": deleted_state,
				"request": {
					"method": "DELETE",
					"urlPath": f"/record/v2/overlay/terms/{term_id}"
				},
				"response": response(
					status=404,
					body="{\"error\":\"term_not_found\"}",
					delay_ms=DELETE_DELAY_MS
				)
			}
		)


def build_post_mappings(target_dir: Path) -> None:
	shared_request = {
		"method": "POST",
		"urlPath": "/record/v2/overlay/terms",
		"bodyPatterns": [
			matches_json_path_exists("$.label"),
			matches_json_path_exists("$.start_at"),
			matches_json_path_exists("$.end_at"),
			matches_json_path_exists("$.attempts"),
			matches_json_path_exists("$.attempts[0].subject_code")
		]
	}

	for state_name, suffix in (
		("Started", "started"),
		(ADDED_TERM_DELETED_STATE, "recreate"),
	):
		write_mapping(
			target_dir,
			f"post-overlay-terms-success-{suffix}.json",
			{
				"priority": SUCCESS_PRIORITY,
				"scenarioName": ADDED_TERM_SCENARIO,
				"requiredScenarioState": state_name,
				"newScenarioState": ADDED_TERM_PRESENT_STATE,
				"request": shared_request,
				"response": response(
					status=200,
					body=RECORD_BODY,
					delay_ms=POST_DELAY_MS
				)
			}
		)

	write_mapping(
		target_dir,
		"post-overlay-terms-conflict.json",
		{
			"priority": STATE_PRIORITY,
			"scenarioName": ADDED_TERM_SCENARIO,
			"requiredScenarioState": ADDED_TERM_PRESENT_STATE,
			"request": shared_request,
			"response": response(
				status=412,
				body="{\"error\":\"term_already_exists\"}",
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
	build_get_mapping(record_mappings_dir)
	build_put_mappings(record_mappings_dir, base_state, max_revision=max(args.max_revision, 2))
	build_delete_mappings(record_mappings_dir)
	build_post_mappings(record_mappings_dir)


if __name__ == "__main__":
	main()
