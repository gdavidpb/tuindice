#!/usr/bin/env bash
# iOS adapter of the E2E harness (plan section 4.5). JSON on stdout, logs on stderr.
#
#   toolchain | ensure-device | health | recover | stop-device    delegated to device.sh
#   build <port>                          build.sh against the dedicated simulator; remembers the app
#   install                               installs the app on the simulator
#   enumerate                             the XCUITest tests, without running them
#   driver-contract <dir> <port>          DriverContractTests on a freshly reset app: one xcodebuild invocation; {ok, passed, failed, skipped, artifacts}
#   reset-app                             terminate, uninstall, keychain reset, reinstall
#   run-scenario <id> <attemptDir> <port> exactly one xcodebuild invocation; the verdict comes from result.json, not from xcodebuild
#   crash-probe <sinceEpoch> <attemptDir> [<untilEpoch> [<waitSeconds>]]   the crash report of the app process captured between the two
#                                         times on this simulator, polling up to <waitSeconds> for a report that lands late, and only
#                                         when the attempt can have crashed (no readable result.json, or the scenario failed with APP_NOT_RUNNING)
#   collect-failure <attemptDir> <sinceEpoch>   failed attempts only: the log store from the attempt on (capped), a fallback screenshot
# Every simctl call and every xcodebuild destination carries the UDID of the dedicated simulator.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
source "${SCRIPT_DIR}/../shared/lib.sh"
source "${SCRIPT_DIR}/../shared/layout.env"

DEVICE="${SCRIPT_DIR}/device.sh"
TOOLS="${SCRIPT_DIR}/../shared/adapter_tools.py"
WORK="${E2E_TMP_ROOT:-${TMPDIR:-/tmp}/tuindice-e2e}/ios"
BUILD_STATE="${WORK}/build.json"
REPORTS_DIR="${E2E_FAKE_DIAGNOSTIC_REPORTS:-${HOME}/Library/Logs/DiagnosticReports}"
WORKSPACE="${REPO_ROOT}/iosApp/TuIndiceHost.xcworkspace"
# The log kept for a failed attempt is at most this long (the last bytes of it); E2E_FAKE_LOG_CAP_BYTES is the test seam.
LOG_CAP_BYTES="${E2E_FAKE_LOG_CAP_BYTES:-20971520}"

resolve_device() {
	UDID="$(bash "${DEVICE}" serial)"
}

read_build_state() {
	read_build app=APP appId=APP_ID executable=EXECUTABLE derivedData=DERIVED_DATA
}

# The arguments every xcodebuild test invocation shares. -collect-test-diagnostics never is mandatory: without it a
# failed test spends 625 s in `simctl diagnose`.
xcodebuild_test() { # <extra xcodebuild args...>
	xcodebuild test-without-building -workspace "${WORKSPACE}" -scheme "${E2E_IOS_UITEST_SCHEME}" -configuration Debug \
		-sdk iphonesimulator -destination "platform=iOS Simulator,id=${UDID}" -derivedDataPath "${DERIVED_DATA}" \
		-parallel-testing-enabled NO -collect-test-diagnostics never CODE_SIGNING_ALLOWED=NO "$@"
}

cmd_build() {
	mkdir -p "${WORK}"
	resolve_device
	bash "${SCRIPT_DIR}/build.sh" --port "${1:?build needs the WireMock port}" --udid "${UDID}" | tee "${BUILD_STATE}"
}

cmd_install() {
	resolve_device
	read_build_state
	xcrun simctl install "${UDID}" "${APP}"
	emit_json "ok=j:true" "app=s:${APP_ID}"
}

cmd_enumerate() {
	local listing="${WORK}/enumeration.json"
	resolve_device
	read_build_state
	rm -f "${listing}"
	xcodebuild_test -enumerate-tests -test-enumeration-style flat -test-enumeration-format json \
		-test-enumeration-output-path "${listing}" >&2
	python3 "${TOOLS}" xctest-tests "${listing}"
}

# Terminate, uninstall, keychain reset, reinstall: the state every attempt and the driver contract start from.
reset_app_state() {
	local answer
	# An app that is not running is the normal case after the previous attempt; any other answer is a failure.
	if ! answer="$(xcrun simctl terminate "${UDID}" "${APP_ID}" 2>&1)"; then
		case "${answer}" in
			*"found nothing to terminate"*) log "${APP_ID} was not running" ;;
			*) fail "simctl terminate ${APP_ID} answered: ${answer}" ;;
		esac
	fi
	xcrun simctl uninstall "${UDID}" "${APP_ID}"
	xcrun simctl keychain "${UDID}" reset
	xcrun simctl install "${UDID}" "${APP}"
}

cmd_reset_app() {
	resolve_device
	read_build_state
	reset_app_state
	emit_json "ok=j:true"
}

CONTRACT_TEST="${E2E_IOS_UITEST_SCHEME}/DriverContractTests/test_driver_contract"

cmd_driver_contract() {
	local dir="${1:?artifacts dir}" port="${2:?port}" status=0
	resolve_device
	read_build_state
	mkdir -p "${dir}"
	reset_app_state
	export TEST_RUNNER_E2E_WIREMOCK_URL="http://localhost:${port}"
	export TEST_RUNNER_E2E_OUTPUT_DIR="${dir}/results"
	xcodebuild_test "-only-testing:${CONTRACT_TEST}" -resultBundlePath "${dir}/attempt.xcresult" > "${dir}/runner.log" 2>&1 || status=$?
	log "xcodebuild exited ${status}; the probes are read from result.json"
	if [[ -d "${dir}/results/driver-contract" ]]; then
		cp -R "${dir}/results/driver-contract/." "${dir}/"
	fi
	python3 "${TOOLS}" xctest-contract "${dir}/runner.log" "${status}" "${dir}/result.json" \
		| python3 -c 'import json, sys; out = json.load(sys.stdin); out["artifacts"] = sys.argv[1]; print(json.dumps(out))' "${dir}"
}

cmd_run_scenario() {
	local id="${1:?id}" dir="${2:?attempt dir}" port="${3:?port}" only status=0
	resolve_device
	read_build_state
	mkdir -p "${dir}"
	only="$(python3 "${TOOLS}" catalog-field "${E2E_CATALOG_FILE:-${REPO_ROOT}/${E2E_CATALOG_JSON}}" "${id}" ios)"
	export TEST_RUNNER_E2E_WIREMOCK_URL="http://localhost:${port}"
	export TEST_RUNNER_E2E_OUTPUT_DIR="${dir}/results"
	if [[ "${E2E_TRACE:-0}" == "1" ]]; then
		export TEST_RUNNER_E2E_TRACE=1
	fi
	xcodebuild_test "-only-testing:${only}" -resultBundlePath "${dir}/attempt.xcresult" > "${dir}/runner.log" 2>&1 || status=$?
	log "xcodebuild exited ${status}; the verdict is read from result.json"
	if [[ -d "${dir}/results/${id}" ]]; then
		cp -R "${dir}/results/${id}/." "${dir}/"
	fi
	python3 "${TOOLS}" xctest-summary "${dir}/runner.log" "${status}"
}

# Whether the attempt can have left a crash report. A report lands seconds after the crash, but only an app that went away has
# one: with no readable result (the runner died or was killed) or a scenario that failed because the app was not running the probe
# waits for it; a scenario that failed with the app alive (a step timed out, an assertion) has nothing to wait for.
app_may_have_crashed() { # <attempt dir>
	python3 - "$1/result.json" << 'PY'
import json
import sys

try:
    result = json.load(open(sys.argv[1]))
except (OSError, ValueError):
    sys.exit(0)
failure = result.get("failure") if isinstance(result, dict) else None
sys.exit(0 if not isinstance(result, dict) or (isinstance(failure, dict) and failure.get("kind") == "APP_NOT_RUNNING") else 1)
PY
}

cmd_crash_probe() {
	local since="${1:?since}" dir="${2:?attempt dir}" until_epoch="${3:-$(date +%s)}" wait_seconds="${4:-0}"
	resolve_device
	read_build_state
	if [[ "${wait_seconds}" != "0" ]] && ! app_may_have_crashed "${dir}"; then
		log "The scenario failed with the app alive; not waiting ${wait_seconds} s for a crash report."
		wait_seconds=0
	fi
	python3 "${TOOLS}" ios-crash "${REPORTS_DIR}" "${since}" "${until_epoch}" "${EXECUTABLE}" "${dir}/crash.txt" "${UDID}" "${wait_seconds}"
}

cmd_collect_failure() {
	local dir="${1:?attempt dir}" since="${2:?since}" start file
	resolve_device
	read_build_state
	# A run the harness killed copied nothing from the runner's output directory: whatever it wrote there (driver.log) comes now,
	# without replacing what a finished run already copied.
	if [[ -n "${E2E_CURRENT_SCENARIO:-}" && -d "${dir}/results/${E2E_CURRENT_SCENARIO}" ]]; then
		for file in "${dir}/results/${E2E_CURRENT_SCENARIO}/"*; do
			[[ -e "${dir}/${file##*/}" ]] || cp -R "${file}" "${dir}/"
		done
	fi
	if ! compgen -G "${dir}/*.png" > /dev/null; then
		xcrun simctl io "${UDID}" screenshot "${dir}/fallback-screen.png" >&2
	fi
	# `log show` reads the log store, so nothing streams while scenarios run and nothing is lost to a late start.
	start="$(date -r "${since}" '+%Y-%m-%d %H:%M:%S')"
	xcrun simctl spawn "${UDID}" log show --start "${start}" --style compact \
		--predicate "process == \"${EXECUTABLE}\" OR process CONTAINS \"UITests\"" | python3 "${TOOLS}" cap-log "${LOG_CAP_BYTES}" "${dir}/app.log" >&2
	emit_json "ok=j:true"
}

case "${1:-}" in
	toolchain | health | recover) bash "${DEVICE}" "$1" ;;
	ensure-device) bash "${DEVICE}" ensure ;;
	stop-device) bash "${DEVICE}" stop ;;
	build) shift; cmd_build "$@" ;;
	install) cmd_install ;;
	enumerate) cmd_enumerate ;;
	reset-app) cmd_reset_app ;;
	driver-contract) shift; cmd_driver_contract "$@" ;;
	run-scenario) shift; cmd_run_scenario "$@" ;;
	crash-probe) shift; cmd_crash_probe "$@" ;;
	collect-failure) shift; cmd_collect_failure "$@" ;;
	*) printf 'Usage: %s toolchain|ensure-device|build|install|enumerate|health|reset-app|driver-contract|run-scenario|crash-probe|collect-failure|recover|stop-device\n' "$0" >&2; exit 64 ;;
esac
