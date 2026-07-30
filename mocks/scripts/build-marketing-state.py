#!/usr/bin/env python3
"""Generates the marketing dataset from the E2E base fixtures.

The E2E fixtures exist to exercise edge cases -- an editable synthetic term,
evaluations whose dates have already passed -- which is exactly what you do not
want in a store or landing capture. This script derives a sibling dataset that
is flattering *and still truthful*: same app, same shapes, a student whose
trimester happens to be going well.

    python3 mocks/scripts/build-marketing-state.py

Writes:
    mocks/config/record-marketing-state.json
    mocks/config/evaluations-marketing-state.json

Serve it with WIREMOCK_DATA_PROFILE=marketing; unset, the mock keeps the E2E
fixtures, so the suite and its certification fingerprint are untouched.

Dates are computed relative to the day you run this, so the dataset never rots
the way a file of fixed timestamps does. Re-run it before capturing.
"""

from __future__ import annotations

import datetime as dt
import json
import pathlib

MOCKS = pathlib.Path(__file__).resolve().parent.parent
CONFIG = MOCKS / "config"

DAY_MS = 86_400_000


def load(name: str) -> dict:
    return json.loads((CONFIG / name).read_text(encoding="utf-8"))


def dump(name: str, payload: dict) -> None:
    (CONFIG / name).write_text(
        json.dumps(payload, indent="\t", ensure_ascii=False) + "\n", encoding="utf-8"
    )


def midday_ms(day: dt.date) -> int:
    """Epoch millis at noon UTC -- avoids a timezone shifting the calendar day."""
    stamp = dt.datetime.combine(day, dt.time(12, 0), tzinfo=dt.timezone.utc)
    return int(stamp.timestamp() * 1000)


def build_record(base: dict) -> dict:
    """Drops synthetic terms so the app opens on the real current trimester.

    A synthetic term is a user-created simulation: the app renders it with an
    inline grade picker and edit/delete actions, which reads as an unfinished
    form rather than a product screenshot.
    """
    record = json.loads(json.dumps(base))
    dropped = [t for t in record["terms"] if t.get("kind") == "synthetic"]
    record["terms"] = [t for t in record["terms"] if t.get("kind") != "synthetic"]

    current = next((t for t in record["terms"] if t.get("kind") == "current"), None)
    if current is None:
        raise SystemExit("no current term in the base record state — nothing to feature")

    print(f"  record: dropped {len(dropped)} synthetic term(s)")
    print(f'  record: featuring "{current["period_label"]}" with {len(current["attempts"])} subjects')
    for attempt in current["attempts"]:
        print(f'     {attempt["code"]:8s} {attempt["credits"]}UC  nota={attempt.get("grade")}')
    return record


def build_evaluations(base: dict, record: dict, today: dt.date) -> dict:
    """Re-dates evaluations around today so upcoming ones read as scheduled.

    The app marks an evaluation overdue purely by comparing its date to now, and
    an overdue badge is red. Graded evaluations stay in the past (that is what
    graded means); ungraded ones move ahead of today, with the first two inside
    the current week so the screen opens on something.
    """
    evaluations = json.loads(json.dumps(base))
    live_attempts = {
        attempt["id"] for term in record["terms"] for attempt in term["attempts"]
    }

    kept = [
        ev for ev in evaluations["evaluations"] if ev.get("attempt_id") in live_attempts
    ]
    orphaned = len(evaluations["evaluations"]) - len(kept)
    if orphaned:
        print(f"  evaluations: dropped {orphaned} orphaned by the removed term(s)")

    done = [ev for ev in kept if ev.get("is_done")]
    upcoming = [ev for ev in kept if not ev.get("is_done") and ev.get("date") is not None]
    undated = [ev for ev in kept if not ev.get("is_done") and ev.get("date") is None]

    # Graded work, one per week going back from last week. Spreading them keeps
    # the screen's week grouping meaningful -- bunched into a single week they
    # collapse into one huge section, which reads as a data glitch.
    for index, ev in enumerate(sorted(done, key=lambda e: e.get("date") or 0)):
        weeks_back = len(done) - index
        ev["date"] = midday_ms(today - dt.timedelta(weeks=weeks_back))

    # Upcoming work: the first two land inside the current week, the rest follow
    # weekly, so the default week is never empty.
    ahead = [1, 4] + [7 + 5 * i for i in range(max(0, len(upcoming) - 2))]
    for ev, days in zip(sorted(upcoming, key=lambda e: e.get("date") or 0), ahead):
        ev["date"] = midday_ms(today + dt.timedelta(days=days))

    evaluations["evaluations"] = done + upcoming + undated
    print(
        f"  evaluations: {len(done)} graded in the past, {len(upcoming)} upcoming, "
        f"{len(undated)} unscheduled"
    )
    if upcoming:
        first = dt.datetime.fromtimestamp(
            upcoming[0]["date"] / 1000, dt.timezone.utc
        ).date()
        print(f"     next one: {first.isoformat()} (today is {today.isoformat()})")
    return evaluations


def main() -> None:
    today = dt.date.today()
    print(f"building marketing dataset for {today.isoformat()}")

    record = build_record(load("record-base-state.json"))
    evaluations = build_evaluations(
        load("evaluations-base-state.json"), record, today
    )

    dump("record-marketing-state.json", record)
    dump("evaluations-marketing-state.json", evaluations)
    print("\nwrote config/record-marketing-state.json and config/evaluations-marketing-state.json")


if __name__ == "__main__":
    main()
