#!/usr/bin/env bash
set -euo pipefail

# Rewrites response.fixedDelayMilliseconds across every mapping in the given
# directory for the fast delay profile. The per-mapping contract lives in the
# mapping JSON itself: metadata.fastDelayMilliseconds is the value the fast
# profile must keep (WireMock stub metadata is inert for matching). Mappings
# without the marker collapse to the 250ms default — a mapping whose flow
# depends on its delay (cancel windows, reveal timers) must declare the marker
# or the E2E contract lint fails.

mappings_dir="${1:-}"

if [ -z "${mappings_dir}" ] || [ ! -d "${mappings_dir}" ]; then
	echo "Usage: $0 <mappings-dir>" >&2
	exit 1
fi

if ! command -v jq >/dev/null 2>&1; then
	echo "Missing jq: required when WIREMOCK_DELAY_PROFILE=fast." >&2
	exit 1
fi

while IFS= read -r mapping_file; do
	[ -n "${mapping_file}" ] || continue
	if ! jq -e '.response.fixedDelayMilliseconds? != null' "${mapping_file}" >/dev/null; then
		continue
	fi

	tmp_file="${mapping_file}.tmp"
	jq '.response.fixedDelayMilliseconds = (.metadata.fastDelayMilliseconds // 250)' \
		"${mapping_file}" >"${tmp_file}"
	mv "${tmp_file}" "${mapping_file}"
done < <(find "${mappings_dir}" -name '*.json' -type f | sort)
