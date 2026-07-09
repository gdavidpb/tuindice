#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APPLY_SCRIPT="${SCRIPT_DIR}/apply-fast-delay-profile.sh"

failures=0

fail() {
	printf 'FAIL: %s\n' "$1" >&2
	failures=1
}

fixture_dir="$(mktemp -d "${TMPDIR:-/tmp}/tuindice-fast-delay-test.XXXXXX")"
trap 'rm -rf "${fixture_dir}"' EXIT

mkdir -p "${fixture_dir}/nested"

cat >"${fixture_dir}/marked.json" <<'JSON'
{
  "request": { "method": "POST", "urlPath": "/marked" },
  "response": { "status": 200, "fixedDelayMilliseconds": 15000 },
  "metadata": { "fastDelayMilliseconds": 30000 }
}
JSON

cat >"${fixture_dir}/nested/unmarked.json" <<'JSON'
{
  "request": { "method": "GET", "urlPath": "/unmarked" },
  "response": { "status": 200, "fixedDelayMilliseconds": 9000 }
}
JSON

cat >"${fixture_dir}/no-delay.json" <<'JSON'
{
  "request": { "method": "GET", "urlPath": "/no-delay" },
  "response": { "status": 200, "jsonBody": { "ok": true } }
}
JSON

bash "${APPLY_SCRIPT}" "${fixture_dir}"

delay_of() {
	jq '.response.fixedDelayMilliseconds' "$1"
}

[ "$(delay_of "${fixture_dir}/marked.json")" = "30000" ] ||
	fail "marked mapping must keep metadata.fastDelayMilliseconds (expected 30000, got $(delay_of "${fixture_dir}/marked.json"))"

[ "$(delay_of "${fixture_dir}/nested/unmarked.json")" = "250" ] ||
	fail "unmarked mapping must collapse to the 250ms default (got $(delay_of "${fixture_dir}/nested/unmarked.json"))"

[ "$(delay_of "${fixture_dir}/no-delay.json")" = "null" ] ||
	fail "mapping without fixedDelayMilliseconds must stay untouched (got $(delay_of "${fixture_dir}/no-delay.json"))"

if ! bash "${APPLY_SCRIPT}" >/dev/null 2>&1; then
	:
else
	fail "missing mappings-dir argument must exit non-zero"
fi

if [ "${failures}" -ne 0 ]; then
	exit 1
fi

printf 'apply-fast-delay-profile self-test passed.\n'
