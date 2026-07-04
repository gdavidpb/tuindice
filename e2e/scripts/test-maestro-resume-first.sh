#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TEST_ROOT="$(mktemp -d "${TMPDIR:-/tmp}/tuindice-maestro-resume-test.XXXXXX")"

cleanup() {
	rm -rf "${TEST_ROOT}"
}
trap cleanup EXIT

export E2E_TMP_DIR="${TEST_ROOT}/tmp"
export E2E_REPORT_DIR="${TEST_ROOT}/reports"
export E2E_MAESTRO_CHECKPOINT_DIR="${TEST_ROOT}/checkpoints"
export E2E_MAESTRO_RESET_WIREMOCK_PER_ITEM=0
export E2E_MAESTRO_SUCCESS_REPORT_GRACE_SECONDS=1

source "${SCRIPT_DIR}/common.sh"

FAKE_BIN="${TEST_ROOT}/bin"
mkdir -p "${FAKE_BIN}" "${E2E_TMP_DIR}" "${E2E_REPORT_DIR}"

cat >"${FAKE_BIN}/maestro" <<'FAKE_MAESTRO'
#!/usr/bin/env bash
set -euo pipefail

output_file=""
suite_file=""

while [[ "$#" -gt 0 ]]; do
	case "$1" in
		--output|--format|--test-output-dir|--debug-output|--device)
			if [[ "$1" == "--output" ]]; then
				output_file="$2"
			fi
			shift 2
			;;
		test)
			shift
			;;
		*)
			suite_file="$1"
			shift
			;;
	esac
done

target="$(
	awk '
		/^[[:space:]]*-[[:space:]]*runFlow:[[:space:]]*/ {
			value = $0
			sub(/^[[:space:]]*-[[:space:]]*runFlow:[[:space:]]*/, "", value)
			last = value
		}
		END { print last }
	' "${suite_file}"
)"
target_name="$(basename "${target}")"
printf '%s\n' "${target_name}" >>"${ORDER_FILE}"
printf 'fake maestro ran %s\n' "${target_name}"

status=0
if [[ -n "${FAIL_TARGET:-}" && "${target_name}" == "${FAIL_TARGET}" ]]; then
	status=7
fi
reported_status="${status}"

if [[ -n "${output_file}" ]]; then
	if [[ "${reported_status}" == "0" ]]; then
		printf '<testsuite failures="0" errors="0"><testcase name="%s" status="SUCCESS"/></testsuite>\n' "${target_name}" >"${output_file}"
	else
		printf '<testsuite failures="0" errors="1"><testcase name="%s" status="ERROR"/></testsuite>\n' "${target_name}" >"${output_file}"
	fi
fi

if [[ "${reported_status}" == "0" ]]; then
	printf '[Passed] Fake %s\n\n1/1 Flow Passed in 1s\n' "${target_name}"
fi
if [[ "${reported_status}" == "0" && -n "${HANG_AFTER_PASS_TARGET:-}" && "${target_name}" == "${HANG_AFTER_PASS_TARGET}" ]]; then
	while :; do
		sleep 60
	done
fi
if [[ "${reported_status}" == "0" && -n "${NONZERO_AFTER_PASS_TARGET:-}" && "${target_name}" == "${NONZERO_AFTER_PASS_TARGET}" ]]; then
	status=9
fi

exit "${status}"
FAKE_MAESTRO
chmod +x "${FAKE_BIN}/maestro"
export PATH="${FAKE_BIN}:${PATH}"

SUITE_ROOT="${TEST_ROOT}/suite"
mkdir -p "${SUITE_ROOT}/flows"
for index in 1 2 3 4 5 6; do
	cat >"${SUITE_ROOT}/flows/flow-${index}.yaml" <<FLOW
appId: com.gdavidpb.tuindice.debug
---
- launchApp:
    stopApp: true
    clearState: true
FLOW
done

SUITE_FILE="${SUITE_ROOT}/suite.yaml"
cat >"${SUITE_FILE}" <<'SUITE'
appId: com.gdavidpb.tuindice.debug
name: Fake resume suite
---
- runFlow: flows/flow-1.yaml
- runFlow: flows/flow-2.yaml
- runFlow: flows/flow-3.yaml
- runFlow: flows/flow-4.yaml
- runFlow: flows/flow-5.yaml
- runFlow: flows/flow-6.yaml
SUITE

order_csv() {
	awk 'BEGIN { first = 1 } { if (!first) printf ","; printf "%s", $0; first = 0 } END { print "" }' "$1"
}

assert_equals() {
	local expected="$1"
	local actual="$2"
	local message="$3"

	if [[ "${expected}" != "${actual}" ]]; then
		printf 'Assertion failed: %s\nexpected: %s\nactual:   %s\n' "${message}" "${expected}" "${actual}" >&2
		exit 1
	fi
}

run_fake_suite() {
	local fail_target="$1"
	local order_file="$2"
	local nonzero_after_pass_target="${3:-}"
	local hang_after_pass_target="${4:-}"
	local status

	: >"${order_file}"
	export ORDER_FILE="${order_file}"
	export FAIL_TARGET="${fail_target}"
	export NONZERO_AFTER_PASS_TARGET="${nonzero_after_pass_target}"
	export HANG_AFTER_PASS_TARGET="${hang_after_pass_target}"
	set +e
	run_maestro_suite_resume_first \
		"Test" \
		"${E2E_REPORT_DIR}/maestro.log" \
		"${SUITE_FILE}" \
		"${E2E_REPORT_DIR}/output" \
		"${E2E_REPORT_DIR}/debug" \
		"${E2E_REPORT_DIR}/junit.xml" \
		"${TEST_ROOT}/maestro-home"
	status="$?"
	set -e
	return "${status}"
}

checkpoint_file="$(maestro_checkpoint_file "Test" "${SUITE_FILE}")"

ORDER_ONE="${TEST_ROOT}/order-one.txt"
if run_fake_suite "flow-5.yaml" "${ORDER_ONE}"; then
	printf 'Expected first run to fail at flow-5.yaml.\n' >&2
	exit 1
fi
assert_equals \
	"flow-1.yaml,flow-2.yaml,flow-3.yaml,flow-4.yaml,flow-5.yaml" \
	"$(order_csv "${ORDER_ONE}")" \
	"first run should stop at the failing case"
assert_equals "flows/flow-5.yaml" "$(cat "${checkpoint_file}")" "checkpoint should store the failing target"

ORDER_TWO="${TEST_ROOT}/order-two.txt"
run_fake_suite "" "${ORDER_TWO}"
assert_equals \
	"flow-5.yaml,flow-6.yaml,flow-1.yaml,flow-2.yaml,flow-3.yaml,flow-4.yaml" \
	"$(order_csv "${ORDER_TWO}")" \
	"second run should rotate from the previous failure and still execute every case"
if [[ -e "${checkpoint_file}" ]]; then
	printf 'Expected checkpoint to be cleared after a complete passing run.\n' >&2
	exit 1
fi

ORDER_NONZERO="${TEST_ROOT}/order-nonzero.txt"
run_fake_suite "" "${ORDER_NONZERO}" "flow-4.yaml"
assert_equals \
	"flow-1.yaml,flow-2.yaml,flow-3.yaml,flow-4.yaml,flow-5.yaml,flow-6.yaml" \
	"$(order_csv "${ORDER_NONZERO}")" \
	"nonzero after a clean passed flow should not rotate or fail the suite"
if [[ -e "${checkpoint_file}" ]]; then
	printf 'Expected checkpoint to remain cleared after recoverable Maestro nonzero.\n' >&2
	exit 1
fi

ORDER_HANG="${TEST_ROOT}/order-hang.txt"
run_fake_suite "" "${ORDER_HANG}" "" "flow-2.yaml"
assert_equals \
	"flow-1.yaml,flow-2.yaml,flow-3.yaml,flow-4.yaml,flow-5.yaml,flow-6.yaml" \
	"$(order_csv "${ORDER_HANG}")" \
	"stuck Maestro after clean JUnit should be terminated and treated as pass"
if [[ -e "${checkpoint_file}" ]]; then
	printf 'Expected checkpoint to remain cleared after recoverable Maestro hang.\n' >&2
	exit 1
fi

mkdir -p "$(dirname "${checkpoint_file}")"
printf '%s\n' "flows/flow-3.yaml" >"${checkpoint_file}"
ORDER_THREE="${TEST_ROOT}/order-three.txt"
if run_fake_suite "flow-5.yaml" "${ORDER_THREE}"; then
	printf 'Expected rotated run to fail at flow-5.yaml.\n' >&2
	exit 1
fi
assert_equals \
	"flow-3.yaml,flow-4.yaml,flow-5.yaml" \
	"$(order_csv "${ORDER_THREE}")" \
	"rotated run should fail fast at the new failing case"
assert_equals "flows/flow-5.yaml" "$(cat "${checkpoint_file}")" "checkpoint should move to the new failing target"

printf '%s\n' "flows/deleted-flow.yaml" >"${checkpoint_file}"
ORDER_FOUR="${TEST_ROOT}/order-four.txt"
run_fake_suite "" "${ORDER_FOUR}"
assert_equals \
	"flow-1.yaml,flow-2.yaml,flow-3.yaml,flow-4.yaml,flow-5.yaml,flow-6.yaml" \
	"$(order_csv "${ORDER_FOUR}")" \
	"missing checkpoint target should restart from the first case"

MIXED_FILE="${SUITE_ROOT}/mixed.yaml"
cat >"${MIXED_FILE}" <<'MIXED'
appId: com.gdavidpb.tuindice.debug
---
- runFlow: flows/flow-1.yaml
- runFlow: flows/flow-2.yaml
- tapOn:
    id: some_inline_command
MIXED

run_fake_mixed() {
	local order_file="$1"
	local status

	: >"${order_file}"
	export ORDER_FILE="${order_file}"
	export FAIL_TARGET=""
	export NONZERO_AFTER_PASS_TARGET=""
	export HANG_AFTER_PASS_TARGET=""
	set +e
	run_maestro_suite_resume_first \
		"Test" \
		"${E2E_REPORT_DIR}/maestro-mixed.log" \
		"${MIXED_FILE}" \
		"${E2E_REPORT_DIR}/output-mixed" \
		"${E2E_REPORT_DIR}/debug-mixed" \
		"${E2E_REPORT_DIR}/junit-mixed.xml" \
		"${TEST_ROOT}/maestro-home"
	status="$?"
	set -e
	return "${status}"
}

ORDER_MIXED="${TEST_ROOT}/order-mixed.txt"
run_fake_mixed "${ORDER_MIXED}"
assert_equals \
	"flow-2.yaml" \
	"$(order_csv "${ORDER_MIXED}")" \
	"mixed runFlow refs + inline commands must run as one single flow, never expand into ref-only cases"

printf 'Maestro resume-first tests passed.\n'
