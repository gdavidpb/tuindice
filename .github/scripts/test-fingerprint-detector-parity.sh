#!/usr/bin/env bash
# The change detector and the E2E fingerprint are two definitions of "what can change what a scenario does". This test
# walks every path the fingerprint of a platform reads (`e2e-fingerprint.sh --print-pathspecs`) through the detector
# and requires that the detector asks for evidence of that platform. Together with the fixtures of
# test-detect-changed-app.sh (which fix the reverse: what the fingerprint leaves out asks for nothing) it keeps the
# rule "if it can change what runs, it moves the fingerprint and demands evidence; if not, neither" from drifting.
#   test-fingerprint-detector-parity.sh
set -euo pipefail
IFS=$'\n\t'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
FINGERPRINT="${REPO_ROOT}/e2e/scripts/shared/e2e-fingerprint.sh"

# `--probe <path> <out-dir>`: runs the detector over a single changed path and writes the platforms it requires.
if [[ "${1:-}" == "--probe" ]]; then
	path="$2"
	out_dir="$3"
	key="$(printf '%s' "$path" | shasum | awk '{ print $1 }')"
	work="$(mktemp -d "${out_dir}/probe.XXXXXX")"
	printf '%s\n' "$path" >"${work}/changed.txt"
	STATE_DIR="${work}/state" DETECT_CHANGED_APP_CHANGED_FILES_FILE="${work}/changed.txt" \
		bash "${SCRIPT_DIR}/detect-changed-app.sh" HEAD HEAD >"${work}/log" 2>&1 || {
		printf 'detector failed on %s:\n' "$path" >&2
		cat "${work}/log" >&2
		exit 1
	}
	{
		printf '%s\t' "$path"
		cut -d, -f1 "${work}/state/e2e-scope.csv" | sort -u | paste -sd, -
	} >"${out_dir}/${key}.result"
	exit 0
fi

work_root="$(mktemp -d "${TMPDIR:-/tmp}/tuindice-parity.XXXXXX")"
trap 'rm -rf "$work_root"' EXIT
mkdir -p "${work_root}/results"

# The path to hand the detector for a pathspec: the file itself when the pathspec is a file, else a file inside it.
probe_for() {
	local path="$1"
	case "$(git -C "$REPO_ROOT" cat-file -t "HEAD:${path}" 2>/dev/null || true)" in
		blob) printf '%s\n' "$path" ;;
		tree) printf '%s/probe.txt\n' "$path" ;;
		*)
			if [[ "$(basename "$path")" == *.* ]]; then
				printf '%s\n' "$path"
			else
				printf '%s/probe.txt\n' "$path"
			fi
			;;
	esac
}

# Every generic runtime module is classified by the same rules, so one of them stands for all (the specific ones,
# app, iosApp and the scenario modules, are walked in full).
generic_module=""
specific_modules="app testkit scenariokit scenarios scenariorunner iosApp"
is_walked() {
	local path="$1"
	local top="${path%%/*}"
	case " ${specific_modules} " in
		*" ${top} "*) return 0 ;;
	esac
	if [[ "$path" != */* ]] || [[ "$path" == e2e/* || "$path" == mocks* || "$path" == gradle/* || "$path" == .github/* ]]; then
		return 0
	fi
	[[ -f "${REPO_ROOT}/${top}/build.gradle.kts" ]] || return 0
	if [[ -z "$generic_module" ]]; then
		generic_module="$top"
	fi
	[[ "$top" == "$generic_module" ]]
}

probes_file="${work_root}/probes.txt"
expectations="${work_root}/expectations.txt"
: >"$probes_file"
: >"$expectations"
for platform in android ios; do
	while IFS= read -r line; do
		kind="${line%%:*}"
		path="${line#*:}"
		[[ "$kind" != "excluded" ]] || continue
		is_walked "$path" || continue
		probe="$(probe_for "$path")"
		printf '%s\n' "$probe" >>"$probes_file"
		printf '%s\t%s\t%s\n' "$platform" "$probe" "$path" >>"$expectations"
	done < <(bash "$FINGERPRINT" --print-pathspecs "$platform" HEAD)
done

sort -u "$probes_file" | xargs -P 6 -I{} bash "$0" --probe {} "${work_root}/results"

failures=0
checked=0
while IFS=$'\t' read -r platform probe path; do
	checked=$((checked + 1))
	key="$(printf '%s' "$probe" | shasum | awk '{ print $1 }')"
	requires="$(cut -f2 "${work_root}/results/${key}.result")"
	if [[ ",${requires}," != *",${platform},"* ]]; then
		printf 'The %s fingerprint reads %s but the detector asks for [%s] when %s changes.\n' \
			"$platform" "$path" "${requires:-nothing}" "$probe" >&2
		failures=$((failures + 1))
	fi
done <"$expectations"

if (( failures > 0 )); then
	printf 'test-fingerprint-detector-parity: %d of %d pathspecs diverge.\n' "$failures" "$checked" >&2
	exit 1
fi
printf 'Fingerprint and detector agree on %d pathspecs.\n' "$checked"
