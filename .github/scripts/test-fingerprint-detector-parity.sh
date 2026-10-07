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

# A change that touches only signing keys of the two files below is classified apart from the rest of the file (release
# configuration, not host runtime: no version bump). The fingerprint reads both files whole, so it moves anyway and the
# detector must still ask for iOS evidence. The probes above run over an empty diff and cannot reach that branch, so
# each file gets a real signing-only commit.
signing_only_commit() {
	local file="$1"
	local expression="$2"
	local temp index blob tree
	temp="$(mktemp -d "${work_root}/signing.XXXXXX")"
	index="${temp}/index"
	git -C "$REPO_ROOT" show "HEAD:${file}" | sed -E "$expression" >"${temp}/content"
	GIT_INDEX_FILE="$index" git -C "$REPO_ROOT" read-tree HEAD
	blob="$(git -C "$REPO_ROOT" hash-object -w "${temp}/content")"
	GIT_INDEX_FILE="$index" git -C "$REPO_ROOT" update-index --add --cacheinfo "100644,${blob},${file}"
	tree="$(GIT_INDEX_FILE="$index" git -C "$REPO_ROOT" write-tree)"
	GIT_AUTHOR_NAME="TuIndice CI Test" GIT_AUTHOR_EMAIL="tuindice-ci-test@example.invalid" \
		GIT_COMMITTER_NAME="TuIndice CI Test" GIT_COMMITTER_EMAIL="tuindice-ci-test@example.invalid" \
		git -C "$REPO_ROOT" commit-tree "$tree" -p HEAD -m "signing-only change of ${file}"
}

for entry in \
	'iosApp/Config/Release.xcconfig|s/^TUINDICE_CODE_SIGN_STYLE = .*/TUINDICE_CODE_SIGN_STYLE = Manual/' \
	'iosApp/TuIndiceHost.xcodeproj/project.pbxproj|s/CODE_SIGN_STYLE = Automatic;/CODE_SIGN_STYLE = Manual;/'; do
	file="${entry%%|*}"
	expression="${entry#*|}"
	checked=$((checked + 1))
	commit="$(signing_only_commit "$file" "$expression")"
	if [[ "$(git -C "$REPO_ROOT" diff --name-only HEAD "$commit")" != "$file" ]]; then
		printf 'The signing-only commit of %s changed nothing (the expression no longer matches the file).\n' "$file" >&2
		failures=$((failures + 1))
		continue
	fi
	state="${work_root}/signing-state.${checked}"
	STATE_DIR="$state" bash "${SCRIPT_DIR}/detect-changed-app.sh" HEAD "$commit" >"${state}.log" 2>&1 || {
		cat "${state}.log" >&2
		exit 1
	}
	if ! grep -q '^ios,' "${state}/e2e-scope.csv"; then
		printf 'The ios fingerprint reads %s but the detector asks for nothing when only its signing keys change.\n' "$file" >&2
		failures=$((failures + 1))
	fi
done

if (( failures > 0 )); then
	printf 'test-fingerprint-detector-parity: %d of %d pathspecs diverge.\n' "$failures" "$checked" >&2
	exit 1
fi
printf 'Fingerprint and detector agree on %d pathspecs.\n' "$checked"
