#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

suite_path="${1:-}"
if [[ -z "${suite_path}" || ! -f "${suite_path}" ]]; then
	printf 'Usage: %s <suite-yaml>\n' "$0" >&2
	exit 1
fi
suite_path="$(cd "$(dirname "${suite_path}")" && pwd -P)/$(basename "${suite_path}")"

if [[ "${E2E_MAESTRO_OPTIMIZE_SETUP}" != "1" ]]; then
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
cp -R "${flows_root}/." "${prepared_root}/"

rewrite_setup_triples() {
	local yaml_file="$1"
	local relative="${yaml_file#"${prepared_root}/"}"

	case "${relative}" in
		suites/auth-suite.yaml|suites/wizard-suite.yaml|auth/*|wizard/*)
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

while IFS= read -r yaml_file; do
	[[ -n "${yaml_file}" ]] || continue
	rewrite_setup_triples "${yaml_file}"
done < <(find "${prepared_root}" -name '*.yaml' -type f | sort)

relative_suite="${suite_path#"${flows_root}/"}"
printf '%s\n' "${prepared_root}/${relative_suite}"
