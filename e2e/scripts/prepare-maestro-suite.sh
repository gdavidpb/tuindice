#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

suite_path="${1:-}"
if [[ -z "${suite_path}" || ! -f "${suite_path}" ]]; then
	printf 'Usage: %s <suite-yaml>\n' "$0" >&2
	exit 1
fi
suite_path="$(cd "$(dirname "${suite_path}")" && pwd -P)/$(basename "${suite_path}")"

quarantine_entries="$(quarantine_active_entries)"

if [[ "${E2E_MAESTRO_OPTIMIZE_SETUP}" != "1" && -z "${quarantine_entries}" ]]; then
	printf '%s\n' "${suite_path}"
	exit 0
fi

flows_root="${REPO_ROOT}/e2e/maestro/flows"
case "${suite_path}" in
	"${flows_root}/"*)
		;;
	*)
		printf '%s\n' "${suite_path}"
		exit 0
		;;
esac

prepared_root="${E2E_TMP_DIR}/maestro-prepared/$(basename "${suite_path}" .yaml)"
rm -rf "${prepared_root}"
mkdir -p "${prepared_root}"
# Physical path: flow paths are resolved with pwd -P, and on macOS /tmp is a symlink —
# the prefix strip in apply_quarantine_filter needs both sides canonicalized.
prepared_root="$(cd "${prepared_root}" && pwd -P)"
cp -R "${flows_root}/." "${prepared_root}/"

rewrite_setup_triples() {
	local yaml_file="$1"
	local relative="${yaml_file#"${prepared_root}/"}"

	case "${relative}" in
		suites/auth-suite.yaml|auth/*)
			return 0
			;;
	esac

	awk '
		{
			lines[++count] = $0
		}
		END {
			for (idx = 1; idx <= count; idx++) {
				if (lines[idx] == "- runFlow: ../_shared/launch-clean.yaml" &&
					lines[idx + 1] == "- runFlow: ../auth/login-success.yaml" &&
					lines[idx + 2] == "- runFlow: ../wizard/wizard-smoke.yaml") {
					print "- runFlow: ../_shared/launch-seeded-authenticated.yaml"
					idx += 2
					continue
				}

				print lines[idx]
			}
		}
	' "${yaml_file}" >"${yaml_file}.tmp"
	mv "${yaml_file}.tmp" "${yaml_file}"
}

# Quarantined flows are removed from aggregator suites only (suites/ and smoke/), with a
# visible QUARANTINED line per skip on stderr — stdout is reserved for the prepared
# suite path consumed by the runner.
apply_quarantine_filter() {
	local yaml_file="$1"
	local yaml_dir
	local tmp="${yaml_file}.tmp"
	local line rel resolved_dir canonical

	yaml_dir="$(dirname "${yaml_file}")"
	: > "${tmp}"
	while IFS= read -r line; do
		if [[ "${line}" =~ ^[[:space:]]*-[[:space:]]*runFlow:[[:space:]]*(.*[^[:space:]])[[:space:]]*$ ]]; then
			rel="${BASH_REMATCH[1]}"
			rel="${rel%\"}"
			rel="${rel#\"}"
			rel="${rel%\'}"
			rel="${rel#\'}"
			if resolved_dir="$(cd "${yaml_dir}/$(dirname "${rel}")" 2>/dev/null && pwd -P)"; then
				canonical="e2e/maestro/flows/${resolved_dir#"${prepared_root}/"}/$(basename "${rel}")"
				if printf '%s\n' "${quarantine_entries}" | grep --fixed-strings --line-regexp --quiet "${canonical}"; then
					printf 'QUARANTINED: skipping %s (listed in %s)\n' "${canonical}" "${E2E_MAESTRO_QUARANTINE_FILE}" >&2
					continue
				fi
			fi
		fi
		printf '%s\n' "${line}" >> "${tmp}"
	done < "${yaml_file}"
	mv "${tmp}" "${yaml_file}"
}

if [[ "${E2E_MAESTRO_OPTIMIZE_SETUP}" == "1" ]]; then
	while IFS= read -r yaml_file; do
		[[ -n "${yaml_file}" ]] || continue
		rewrite_setup_triples "${yaml_file}"
	done < <(find "${prepared_root}" -name '*.yaml' -type f | sort)
fi

if [[ -n "${quarantine_entries}" ]]; then
	while IFS= read -r yaml_file; do
		[[ -n "${yaml_file}" ]] || continue
		apply_quarantine_filter "${yaml_file}"
	done < <(find "${prepared_root}/suites" "${prepared_root}/smoke" -name '*.yaml' -type f 2>/dev/null | sort)
fi

relative_suite="${suite_path#"${flows_root}/"}"
printf '%s\n' "${prepared_root}/${relative_suite}"
