#!/usr/bin/env python3

from __future__ import annotations

import argparse
import calendar
import hashlib
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


def mock_hex_id(*parts: str) -> str:
	return hashlib.sha256("::".join(parts).encode("utf-8")).hexdigest()[:32]


PROJECTED_TERM_SCHEDULE = (
	{
		"id": mock_hex_id("projected_term", "2026A"),
		"name": "Enero - Marzo 2026",
		"start_date": 1767236400000,
		"end_date": 1774926000000,
		"kind": "historical",
		"revision": 9,
		"presence_scenario": None,
	},
	{
		"id": mock_hex_id("projected_term", "2026B"),
		"name": "Abril - Julio 2026",
		"start_date": 1775012400000,
		"end_date": 1785470400000,
		"kind": "current",
		"revision": 10,
		"presence_scenario": f"record-term-{mock_hex_id('projected_term', '2026B')}",
	},
	{
		"id": mock_hex_id("projected_term", "2026C"),
		"name": "Septiembre - Diciembre 2026",
		"start_date": 1788235200000,
		"end_date": 1798686000000,
		"kind": "synthetic",
		"revision": 11,
		"presence_scenario": None,
	},
)
ADDED_TERM_TEMPLATE = {
	"id": mock_hex_id("added_term", "MOCK-ADDED-TERM"),
	"name": "Term Added",
	"start_date": 1809140400000,
	"end_date": 1817002800000,
	"kind": "synthetic",
	"revision": 1,
	"presence_scenario": f"record-term-{mock_hex_id('added_term', 'MOCK-ADDED-TERM')}",
	"attempts": [
		{
			"id": mock_hex_id("attempt", mock_hex_id("added_term", "MOCK-ADDED-TERM"), "MOCK101"),
			"term_id": mock_hex_id("added_term", "MOCK-ADDED-TERM"),
			"code": "MOCK101",
			"name": "MOCK SUBJECT",
			"credits": 4,
			"grade": 0,
			"revision": 1,
		}
	],
}
PROFILE_TEMPLATE = {
	"user_id": "mock-user",
	"identity_card_number": 12345678,
	"usb_id": "00000000",
	"email": "mock@tuindice.app",
	"first_names": "Mock",
	"last_names": "User",
	"career_name": "Ingenieria Civil Electronica",
	"career_code": 12039,
	"scholarship": False,
}
LEGACY_VISIBLE_EVALUATION_ID = "bfa02abf2292f47e5baee862a8a50ff1"
EVALUATION_MOCK_PLANS = (
	(
		{"week": 1, "day_offset": 3, "type": 0, "schedule_mode": "dated", "grade": None, "max_grade": 10.0, "slug": "semana-1-pendiente"},
		{"week": 2, "day_offset": 2, "type": 4, "schedule_mode": "dated", "grade": 9.0, "max_grade": 10.0, "slug": "semana-2-completada"},
		{"week": 4, "day_offset": 2, "type": 1, "schedule_mode": "dated", "grade": None, "max_grade": 15.0, "slug": "semana-4-pendiente"},
		{"week": 5, "day_offset": 2, "type": 8, "schedule_mode": "dated", "grade": 12.0, "max_grade": 15.0, "slug": "semana-5-completada"},
		{"week": 7, "day_offset": 2, "type": 10, "schedule_mode": "dated", "grade": None, "max_grade": 10.0, "slug": "semana-7-pendiente"},
		{"week": 8, "day_offset": 2, "type": 7, "schedule_mode": "dated", "grade": 8.0, "max_grade": 10.0, "slug": "semana-8-completada"},
		{"week": 10, "day_offset": 4, "type": 9, "schedule_mode": "dated", "grade": None, "max_grade": 15.0, "id": LEGACY_VISIBLE_EVALUATION_ID},
		{"week": 11, "day_offset": 2, "type": 11, "schedule_mode": "continuous", "grade": 12.0, "max_grade": 15.0, "slug": "semana-11-continua"},
	),
	(
		{"week": 2, "day_offset": 2, "type": 4, "schedule_mode": "dated", "grade": None, "max_grade": 10.0, "slug": "semana-2-pendiente"},
		{"week": 3, "day_offset": 2, "type": 0, "schedule_mode": "dated", "grade": 9.0, "max_grade": 10.0, "slug": "semana-3-completada"},
		{"week": 5, "day_offset": 2, "type": 8, "schedule_mode": "dated", "grade": None, "max_grade": 10.0, "slug": "semana-5-pendiente"},
		{"week": 6, "day_offset": 2, "type": 5, "schedule_mode": "dated", "grade": 13.0, "max_grade": 15.0, "slug": "semana-6-completada"},
		{"week": 8, "day_offset": 2, "type": 7, "schedule_mode": "dated", "grade": None, "max_grade": 10.0, "slug": "semana-8-pendiente"},
		{"week": 9, "day_offset": 2, "type": 2, "schedule_mode": "continuous", "grade": 8.0, "max_grade": 10.0, "slug": "semana-9-continua"},
		{"week": 11, "day_offset": 2, "type": 11, "schedule_mode": "dated", "grade": None, "max_grade": 20.0, "slug": "semana-11-programada"},
		{"week": 12, "day_offset": 2, "type": 12, "schedule_mode": "continuous", "grade": 12.0, "max_grade": 15.0, "slug": "semana-12-continua"},
	),
	(
		{"week": 1, "day_offset": 4, "type": 1, "schedule_mode": "dated", "grade": 9.0, "max_grade": 10.0, "slug": "semana-1-completada"},
		{"week": 3, "day_offset": 2, "type": 1, "schedule_mode": "dated", "grade": None, "max_grade": 10.0, "slug": "semana-3-pendiente"},
		{"week": 4, "day_offset": 3, "type": 6, "schedule_mode": "dated", "grade": 14.0, "max_grade": 15.0, "slug": "semana-4-completada"},
		{"week": 6, "day_offset": 2, "type": 6, "schedule_mode": "dated", "grade": None, "max_grade": 10.0, "slug": "semana-6-pendiente"},
		{"week": 7, "day_offset": 3, "type": 10, "schedule_mode": "dated", "grade": 9.0, "max_grade": 10.0, "slug": "semana-7-completada"},
		{"week": 9, "day_offset": 2, "type": 2, "schedule_mode": "dated", "grade": None, "max_grade": 15.0, "slug": "semana-9-pendiente"},
		{"week": 10, "day_offset": 1, "type": 9, "schedule_mode": "dated", "grade": 13.0, "max_grade": 15.0, "slug": "semana-10-completada"},
		{"week": 12, "day_offset": 2, "type": 12, "schedule_mode": "dated", "grade": None, "max_grade": 15.0, "slug": "semana-12-programada"},
	),
)


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
	attempt_id = mock_hex_id("attempt", term_id, course.code)
	base_kwargs = {
		"id": attempt_id,
		"term_id": term_id,
		"code": course.code,
		"name": course.name,
		"credits": course.credits,
		"mutable": mutable,
		"scenario": f"record-attempt-{attempt_id}" if mutable else None,
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
		assignments[label] = (
			mock_hex_id("historical_term", str(year), letter, str(start_date), str(end_date)),
			start_date,
			end_date,
		)
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
				"kind": "historical",
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

	terms_desc = sorted(terms, key=lambda term: (int(term["start_date"]), str(term["id"])), reverse=True)
	added_term = dict(ADDED_TERM_TEMPLATE)

	def serialize_term(term: dict[str, object]) -> dict[str, object]:
		payload: dict[str, object] = {
			"id": term["id"],
			"name": term["name"],
			"start_date": term["start_date"],
			"end_date": term["end_date"],
			"kind": term["kind"],
			"revision": term["revision"],
			"attempts": [attempt.to_json() for attempt in term["attempts"]],  # type: ignore[index]
		}
		presence_scenario = term["presence_scenario"]
		if presence_scenario is not None:
			payload["presence_scenario"] = presence_scenario
		return payload

	return {
		"profile": PROFILE_TEMPLATE,
		"added_term": added_term,
		"terms": [serialize_term(term) for term in terms_desc],
	}


def clamp_datetime(target: datetime, minimum: datetime, maximum: datetime) -> datetime:
	if maximum < minimum:
		maximum = minimum
	return min(max(target, minimum), maximum)


def select_current_evaluations_term(record_state: dict[str, object]) -> dict[str, object]:
	now_millis = int(datetime.now(tz=TIMEZONE).timestamp() * 1000)
	candidate_terms = [
		term
		for term in record_state["terms"]  # type: ignore[index]
		if term["kind"] != "historical"
		and any(
			attempt.get("grading_mode", "numeric") == "numeric"
			for attempt in term["attempts"]
		)
	]
	if not candidate_terms:
		raise ValueError("No editable term with numeric attempts was found for evaluations")

	active_terms = [
		term for term in candidate_terms
		if int(term["start_date"]) <= now_millis <= int(term["end_date"])
	]
	if active_terms:
		return max(active_terms, key=lambda term: (int(term["start_date"]), str(term["id"])))

	started_terms = [
		term for term in candidate_terms
		if int(term["start_date"]) <= now_millis
	]
	if started_terms:
		return max(started_terms, key=lambda term: (int(term["start_date"]), str(term["id"])))

	return min(candidate_terms, key=lambda term: (int(term["start_date"]), str(term["id"])))


def build_sample_evaluation_date(current_term: dict[str, object], offset_days: int) -> int:
	term_start = datetime.fromtimestamp(current_term["start_date"] / 1000, tz=TIMEZONE)
	term_end = datetime.fromtimestamp(current_term["end_date"] / 1000, tz=TIMEZONE)
	now = datetime.now(tz=TIMEZONE).replace(hour=12, minute=0, second=0, microsecond=0)

	date = clamp_datetime(
		target=now + timedelta(days=offset_days),
		minimum=(term_start + timedelta(days=7)).replace(hour=12, minute=0, second=0, microsecond=0),
		maximum=(term_end - timedelta(days=7)).replace(hour=12, minute=0, second=0, microsecond=0),
	)
	return int(date.timestamp() * 1000)


def build_week_evaluation_date(current_term: dict[str, object], week: int, day_offset: int) -> int:
	term_start = datetime.fromtimestamp(current_term["start_date"] / 1000, tz=TIMEZONE)
	week_one_start = (term_start - timedelta(days=term_start.weekday())).replace(
		hour=12,
		minute=0,
		second=0,
		microsecond=0,
	)
	date = week_one_start + timedelta(days=((week - 1) * 7) + day_offset)
	return int(date.timestamp() * 1000)


def build_evaluations_state(record_state: dict[str, object]) -> dict[str, object]:
	current_term = select_current_evaluations_term(record_state)
	current_attempts = sorted(
		[
			attempt
			for attempt in current_term["attempts"]
			if attempt.get("grading_mode", "numeric") == "numeric"
		],
		key=lambda attempt: (str(attempt["code"]), str(attempt["id"])),
	)
	if not current_attempts:
		raise ValueError("Expected at least one numeric attempt in the current editable term")

	evaluations = []
	for index, attempt in enumerate(current_attempts):
		plan = EVALUATION_MOCK_PLANS[index % len(EVALUATION_MOCK_PLANS)]
		total_weight = sum(float(pattern["max_grade"]) for pattern in plan)
		if total_weight > 100.0:
			raise ValueError(f"Evaluation mock plan for {attempt['code']} exceeds 100 points: {total_weight}")

		for pattern in plan:
			grade = pattern["grade"]
			evaluation_id = pattern.get("id") or mock_hex_id(
				"evaluation",
				str(attempt["id"]),
				str(pattern["slug"]),
			)
			evaluations.append(
				{
					"id": evaluation_id,
					"reference_id": evaluation_id,
					"term_id": current_term["id"],
					"attempt_id": attempt["id"],
					"subject_code": attempt["code"],
					"type": pattern["type"],
					"schedule_mode": pattern["schedule_mode"],
					"grade": grade,
					"max_grade": pattern["max_grade"],
					"date": build_week_evaluation_date(
						current_term,
						week=int(pattern["week"]),
						day_offset=int(pattern["day_offset"]),
					),
					"is_done": grade is not None,
					"revision": 1,
				}
			)

	return {
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
