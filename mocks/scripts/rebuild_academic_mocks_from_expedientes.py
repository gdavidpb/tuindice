#!/usr/bin/env python3

from __future__ import annotations

import argparse
import calendar
import html
import json
import re
from dataclasses import dataclass
from datetime import datetime, timedelta
from pathlib import Path
from zoneinfo import ZoneInfo


TIMEZONE = ZoneInfo("America/Santiago")
TERM_BLOCK_PATTERN = re.compile(
	(
		r"&nbsp;&nbsp;&nbsp;\s*([^<]+)</td>.*?"
		r"<table width=\"500\" border=\"0\" align=\"center\" class=\"tabla\">(.*?)</table>\s*"
		r"<table width=\"500\" border=\"0\" align=\"center\" class=\"tabla\">\s*<tr>\s*"
		r"<td width=\"300\" align=\"center\">I\. Per[ií]odo:\s*([0-9.]+)\s*&nbsp;&nbsp;"
		r"\s*I\. Acumulado:\s*([0-9.]+)"
	),
	re.S,
)
COURSE_ROW_PATTERN = re.compile(
	(
		r"<tr>\s*"
		r"<td width=\"50\" align=\"left\">([^<]+)</td>\s*"
		r"<td width=\"380\" align=\"left\">([^<]+)</td>\s*"
		r"<td width=\"50\" align=\"center\">([^<]+)</td>\s*"
		r"<td width=\"45\" align=\"center\">([^<]+)</td>\s*"
		r"<td width=\"80\" align=\"center\">(.*?)</td>\s*"
		r"</tr>"
	),
	re.S,
)
MONTHS = {
	"ENERO": 1,
	"FEBRERO": 2,
	"MARZO": 3,
	"ABRIL": 4,
	"MAYO": 5,
	"JUNIO": 6,
	"JULIO": 7,
	"AGOSTO": 8,
	"SEPTIEMBRE": 9,
	"OCTUBRE": 10,
	"NOVIEMBRE": 11,
	"DICIEMBRE": 12,
}
PROJECTED_TERM_SCHEDULE = (
	{
		"id": "Q2026A",
		"name": "Enero - Marzo 2026",
		"start_date": 1767236400000,
		"end_date": 1774926000000,
		"kind": "official_current",
		"revision": 9,
		"presence_scenario": None,
	},
	{
		"id": "Q2026B",
		"name": "Abril - Julio 2026",
		"start_date": 1775012400000,
		"end_date": 1785470400000,
		"kind": "official_historical",
		"revision": 10,
		"presence_scenario": "record-term-Q2026B",
	},
	{
		"id": "Q2026C",
		"name": "Septiembre - Diciembre 2026",
		"start_date": 1788235200000,
		"end_date": 1798686000000,
		"kind": "official_historical",
		"revision": 11,
		"presence_scenario": None,
	},
)
ADDED_TERM_TEMPLATE = {
	"id": "MOCK-ADDED-QUARTER",
	"name": "Term Added",
	"start_date": 1809140400000,
	"end_date": 1817002800000,
	"kind": "synthetic",
	"revision": 1,
	"presence_scenario": "record-term-MOCK-ADDED-QUARTER",
	"attempts": [
		{
			"id": "MOCK101-MOCK-ADDED-QUARTER",
			"term_id": "MOCK-ADDED-QUARTER",
			"code": "MOCK101",
			"name": "MOCK SUBJECT",
			"credits": 4,
			"grade": 0,
			"revision": 1,
		}
	],
}
EVALUATION_TYPE_SEQUENCE = (0, 10, 12, 9)


@dataclass(frozen=True)
class ParsedCourse:
	code: str
	name: str
	credits: int
	grade_text: str
	observation: str


@dataclass(frozen=True)
class ParsedTerm:
	label: str
	term_average: float
	cumulative_average: float
	courses: list[ParsedCourse]


@dataclass
class AttemptModel:
	id: str
	term_id: str
	code: str
	name: str
	credits: int
	grade: int
	grading_mode: str = "numeric"
	mutable: bool = False
	scenario: str | None = None
	status: str | None = None
	revision: int = 1

	def resolved_outcome(self) -> str:
		status = self.status or "normal"
		if status in {"unreported", "approved", "failed", "retired", "without_effect"}:
			return status
		if self.grading_mode == "qualitative_pass_fail":
			return "normal"
		if self.grade >= 3:
			return "approved"
		if self.grade > 0:
			return "failed"
		return "normal"

	def counts_toward_quarter_numeric_average(self) -> bool:
		return (
			self.grading_mode == "numeric"
			and self.resolved_outcome() not in {"normal", "retired"}
			and (self.grade > 0 or self.resolved_outcome() == "unreported")
		)

	def counts_toward_numeric_average(self) -> bool:
		return (
			self.grading_mode == "numeric"
			and self.resolved_outcome() not in {"normal", "retired", "without_effect", "unreported"}
			and self.grade > 0
		)

	def numeric_credits_contribution(self) -> int:
		return self.credits if self.counts_toward_quarter_numeric_average() else 0

	def numeric_weighted_contribution(self) -> int:
		return self.grade * self.credits if self.counts_toward_quarter_numeric_average() else 0

	def is_approval_event(self) -> bool:
		return self.resolved_outcome() == "approved"

	def is_resolved_qualitative_outcome(self) -> bool:
		return self.grading_mode == "qualitative_pass_fail" and self.resolved_outcome() in {"approved", "failed"}

	def counts_toward_retake_timeline(self) -> bool:
		return self.counts_toward_numeric_average() or self.is_resolved_qualitative_outcome()

	def to_json(self) -> dict[str, object]:
		payload: dict[str, object] = {
			"id": self.id,
			"term_id": self.term_id,
			"code": self.code,
			"name": self.name,
			"credits": self.credits,
			"grade": self.grade,
			"revision": self.revision,
		}
		if self.grading_mode != "numeric":
			payload["grading_mode"] = self.grading_mode
		if self.mutable:
			payload["mutable"] = True
		if self.scenario is not None:
			payload["scenario"] = self.scenario
		if self.status is not None:
			payload["status"] = self.status
		return payload


@dataclass
class CodeAttempt:
	grade: int
	credits: int
	approved: bool

	def weighted(self) -> int:
		return self.grade * self.credits


class CodeState:
	def __init__(self) -> None:
		self.latest: CodeAttempt | None = None
		self.second: CodeAttempt | None = None
		self.weighted_sum = 0
		self.credits_sum = 0

	def add(self, attempt: CodeAttempt) -> None:
		self.second = self.latest
		self.latest = attempt
		self.weighted_sum += attempt.weighted()
		self.credits_sum += attempt.credits

	def effective_weighted(self) -> int:
		if self.latest is None:
			return 0
		if self.second is None:
			return self.weighted_sum
		return self.weighted_sum - (self.second.weighted() if self.latest.approved else 0)

	def effective_credits(self) -> int:
		if self.latest is None:
			return 0
		if self.second is None:
			return self.credits_sum
		return self.credits_sum - (self.second.credits if self.latest.approved else 0)


def read_terms_from_html(path: Path) -> list[ParsedTerm]:
	raw = path.read_bytes()
	try:
		text = raw.decode("utf-8")
	except UnicodeDecodeError:
		text = raw.decode("latin-1")
	terms: list[ParsedTerm] = []
	for raw_label, block, term_average, cumulative_average in TERM_BLOCK_PATTERN.findall(text):
		courses: list[ParsedCourse] = []
		for code, name, credits, grade_text, observation in COURSE_ROW_PATTERN.findall(block):
			normalized_observation = re.sub(r"<.*?>", " ", observation)
			normalized_observation = html.unescape(normalized_observation).replace("\xa0", " ")
			normalized_observation = " ".join(normalized_observation.split())
			courses.append(
				ParsedCourse(
					code=code.strip(),
					name=html.unescape(name).strip(),
					credits=int(credits.strip()),
					grade_text=grade_text.strip(),
					observation=normalized_observation,
				)
			)
		terms.append(
			ParsedTerm(
				label=html.unescape(raw_label).strip(),
				term_average=float(term_average),
				cumulative_average=float(cumulative_average),
				courses=courses,
			)
		)
	if not terms:
		raise ValueError(f"No academic periods were parsed from {path}")
	return terms


def parse_term_dates(label: str) -> tuple[int, int, int]:
	months_part, year_text = label.rsplit(" ", 1)
	start_name, end_name = [piece.strip() for piece in months_part.split("-")]
	year = int(year_text)
	start_month = MONTHS[start_name]
	end_month = MONTHS[end_name]
	start = datetime(year, start_month, 1, 0, 0, tzinfo=TIMEZONE)
	end_day = calendar.monthrange(year, end_month)[1]
	end = datetime(year, end_month, end_day, 0, 0, tzinfo=TIMEZONE)
	return int(start.timestamp() * 1000), int(end.timestamp() * 1000), year


def titleize_term_label(label: str) -> str:
	months_part, year = label.rsplit(" ", 1)
	start_name, end_name = [piece.strip().capitalize() for piece in months_part.split("-")]
	return f"{start_name} - {end_name} {year}"


def build_attempt(course: ParsedCourse, term_id: str, mutable: bool) -> AttemptModel:
	base_kwargs = {
		"id": f"{course.code}{term_id}",
		"term_id": term_id,
		"code": course.code,
		"name": course.name,
		"credits": course.credits,
		"mutable": mutable,
		"scenario": f"record-attempt-{course.code}{term_id}" if mutable else None,
		"revision": 1,
	}
	if course.grade_text == "R":
		return AttemptModel(grade=0, status="retired", **base_kwargs)
	if course.grade_text == "A":
		return AttemptModel(grade=0, grading_mode="qualitative_pass_fail", status="approved", **base_kwargs)

	grade = int(course.grade_text)
	if course.observation == "Sin Efecto":
		return AttemptModel(grade=grade, status="without_effect", **base_kwargs)
	if grade == 0:
		return AttemptModel(grade=grade, status="unreported", **base_kwargs)
	if grade < 3:
		return AttemptModel(grade=grade, status="failed", **base_kwargs)
	return AttemptModel(grade=grade, **base_kwargs)


def compute_average(weighted: int, credits: int) -> float:
	return round(weighted / credits, 4) if credits else 0.0


def recompute_metrics(terms: list[dict[str, object]]) -> None:
	ascending = sorted(terms, key=lambda term: (int(term["start_date"]), str(term["id"])))
	code_states: dict[str, CodeState] = {}
	cumulative_weighted = 0
	cumulative_credits = 0

	for term in ascending:
		attempts: list[AttemptModel] = term["attempts"]  # type: ignore[assignment]
		term_weighted = sum(attempt.numeric_weighted_contribution() for attempt in attempts)
		term_credits = sum(attempt.numeric_credits_contribution() for attempt in attempts)

		for attempt in sorted(attempts, key=lambda item: item.id, reverse=True):
			if not attempt.counts_toward_retake_timeline():
				continue
			state = code_states.setdefault(attempt.code, CodeState())
			previous_weighted = state.effective_weighted()
			previous_credits = state.effective_credits()
			state.add(CodeAttempt(attempt.grade, attempt.credits, attempt.is_approval_event()))
			cumulative_weighted += state.effective_weighted() - previous_weighted
			cumulative_credits += state.effective_credits() - previous_credits

		term["grade"] = compute_average(term_weighted, term_credits)
		term["credits"] = term_credits
		term["grade_sum"] = compute_average(cumulative_weighted, cumulative_credits)
		term["credits_sum"] = cumulative_credits


def assign_historical_term_ids(terms: list[ParsedTerm]) -> dict[str, tuple[str, int, int]]:
	metadata = []
	for term in terms:
		start_date, end_date, year = parse_term_dates(term.label)
		metadata.append((start_date, end_date, year, term.label))

	letters_by_year: dict[int, list[str]] = {}
	for _, _, year, _ in sorted(metadata, key=lambda item: item[0]):
		letters_by_year.setdefault(year, [])

	assignments: dict[str, tuple[str, int, int]] = {}
	for start_date, end_date, year, label in sorted(metadata, key=lambda item: item[0]):
		letter = chr(ord("A") + len(letters_by_year[year]))
		letters_by_year[year].append(letter)
		assignments[label] = (f"Q{year}{letter}", start_date, end_date)
	return assignments


def build_record_state(primary_terms: list[ParsedTerm], projected_terms: list[ParsedTerm]) -> dict[str, object]:
	primary_codes = {course.code for term in primary_terms for course in term.courses}
	historical_ids = assign_historical_term_ids(primary_terms)

	terms: list[dict[str, object]] = []
	for term in primary_terms:
		term_id, start_date, end_date = historical_ids[term.label]
		terms.append(
			{
				"id": term_id,
				"name": titleize_term_label(term.label),
				"start_date": start_date,
				"end_date": end_date,
				"kind": "official_historical",
				"revision": 1,
				"presence_scenario": None,
				"attempts": [build_attempt(course, term_id, mutable=False) for course in term.courses],
			}
		)

	for source_term, schedule in zip(projected_terms, PROJECTED_TERM_SCHEDULE, strict=True):
		projected_attempts = [
			build_attempt(course, schedule["id"], mutable=True)
			for course in source_term.courses
			if course.code not in primary_codes
		]
		if not projected_attempts:
			raise ValueError(f"Projected term {source_term.label} does not contribute any new courses")
		terms.append(
			{
				"id": schedule["id"],
				"name": schedule["name"],
				"start_date": schedule["start_date"],
				"end_date": schedule["end_date"],
				"kind": schedule["kind"],
				"revision": schedule["revision"],
				"presence_scenario": schedule["presence_scenario"],
				"attempts": projected_attempts,
			}
		)

	recompute_metrics(terms)
	terms_desc = sorted(terms, key=lambda term: (int(term["start_date"]), str(term["id"])), reverse=True)
	latest_term = max(terms, key=lambda term: int(term["start_date"]))

	added_term = {
		**ADDED_TERM_TEMPLATE,
		"grade": 0.0,
		"grade_sum": latest_term["grade_sum"],
		"credits": 0,
		"credits_sum": latest_term["credits_sum"],
	}

	def serialize_term(term: dict[str, object]) -> dict[str, object]:
		payload: dict[str, object] = {
			"id": term["id"],
			"name": term["name"],
			"start_date": term["start_date"],
			"end_date": term["end_date"],
			"grade": term["grade"],
			"grade_sum": term["grade_sum"],
			"credits": term["credits"],
			"credits_sum": term["credits_sum"],
			"kind": term["kind"],
			"revision": term["revision"],
			"attempts": [attempt.to_json() for attempt in term["attempts"]],  # type: ignore[index]
		}
		presence_scenario = term["presence_scenario"]
		if presence_scenario is not None:
			payload["presence_scenario"] = presence_scenario
		return payload

	return {
		"added_term": added_term,
		"terms": [serialize_term(term) for term in terms_desc],
	}


def build_evaluations_state(record_state: dict[str, object]) -> dict[str, object]:
	current_term = next(
		term for term in record_state["terms"]  # type: ignore[index]
		if term["id"] == "Q2026A"
	)
	current_attempts = [
		attempt
		for attempt in current_term["attempts"]
		if attempt.get("grading_mode", "numeric") == "numeric"
	]

	evaluations = []
	base_date = datetime.fromtimestamp(current_term["start_date"] / 1000, tz=TIMEZONE)
	for index, attempt in enumerate(current_attempts, start=1):
		evaluation_date = base_date + timedelta(days=14 + (index * 14))
		evaluations.append(
			{
				"id": f"EV{attempt['code']}A1",
				"reference_id": f"EV{attempt['code']}A1",
				"term_id": "Q2026A",
				"attempt_id": attempt["id"],
				"subject_code": attempt["code"],
				"type": EVALUATION_TYPE_SEQUENCE[(index - 1) % len(EVALUATION_TYPE_SEQUENCE)],
				"schedule_mode": "dated",
				"grade": 18.0 if index == 1 else None,
				"max_grade": 20.0,
				"date": int(evaluation_date.timestamp() * 1000),
				"is_done": index == 1,
				"revision": 1,
			}
		)

	return {
		"anchor_revision": 10,
		"evaluations": evaluations,
	}


def main() -> None:
	parser = argparse.ArgumentParser()
	parser.add_argument("--primary-html", required=True)
	parser.add_argument("--projected-html", required=True)
	parser.add_argument("--record-config", required=True)
	parser.add_argument("--evaluations-config", required=True)
	args = parser.parse_args()

	primary_terms = read_terms_from_html(Path(args.primary_html))
	projected_source_terms = read_terms_from_html(Path(args.projected_html))
	primary_codes = {course.code for term in primary_terms for course in term.courses}
	projected_candidates = [
		ParsedTerm(
			label=term.label,
			term_average=term.term_average,
			cumulative_average=term.cumulative_average,
			courses=[course for course in term.courses if course.code not in primary_codes],
		)
		for term in projected_source_terms
	]
	projected_candidates = [term for term in projected_candidates if term.courses]
	if len(projected_candidates) < 3:
		raise ValueError("Expected at least three projected source periods with new courses")

	selected_projected_terms = projected_candidates[-3:]
	record_state = build_record_state(primary_terms, selected_projected_terms)
	evaluations_state = build_evaluations_state(record_state)

	record_config_path = Path(args.record_config)
	evaluations_config_path = Path(args.evaluations_config)
	record_config_path.write_text(json.dumps(record_state, indent=2, ensure_ascii=False) + "\n")
	evaluations_config_path.write_text(json.dumps(evaluations_state, indent=2, ensure_ascii=False) + "\n")


if __name__ == "__main__":
	main()
