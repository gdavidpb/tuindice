#!/usr/bin/env bash
# iOS adapter of the E2E harness (plan section 4.5). JSON on stdout, logs on stderr.
#
#   toolchain | ensure-device | health | recover | stop-device    delegated to device.sh
#   build <port>                          build.sh against the dedicated simulator; remembers the app
#   install                               installs the app on the simulator
#   enumerate                             the XCUITest tests, without running them
#   reset-app                             terminate, uninstall, keychain reset, reinstall
#   run-scenario <id> <attemptDir> <port> exactly one xcodebuild invocation; the verdict comes from result.json, not from xcodebuild
#   crash-probe <sinceEpoch> <attemptDir> crash reports of the app process
#   collect-failure <attemptDir> <sinceEpoch>
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

fail() {
	printf '%s\n' "$*" >&2
	exit 3
}

resolve_device() {
	UDID="$(bash "${DEVICE}" serial)"
}

read_build() {
	[[ -f "${BUILD_STATE}" ]] || fail "No build is recorded in ${BUILD_STATE}; run the build verb first"
	eval "$(python3 -c '
import json, shlex, sys
state = json.load(open(sys.argv[1]))
for key, name in (("app", "APP"), ("appId", "APP_ID"), ("executable", "EXECUTABLE"), ("derivedData", "DERIVED_DATA")):
    print("%s=%s" % (name, shlex.quote(state[key])))' "${BUILD_STATE}")"
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
	read_build
	xcrun simctl install "${UDID}" "${APP}"
	emit_json "ok=j:true" "app=s:${APP_ID}"
}

cmd_enumerate() {
	local listing="${WORK}/enumeration.json"
	resolve_device
	read_build
	rm -f "${listing}"
	xcodebuild_test -enumerate-tests -test-enumeration-style flat -test-enumeration-format json \
		-test-enumeration-output-path "${listing}" >&2
	python3 "${TOOLS}" xctest-tests "${listing}"
}

cmd_reset_app() {
	local answer
	resolve_device
	read_build
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
	emit_json "ok=j:true"
}

cmd_run_scenario() {
	local id="${1:?id}" dir="${2:?attempt dir}" port="${3:?port}" only status=0
	resolve_device
	read_build
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

cmd_crash_probe() {
	local since="${1:?since}" dir="${2:?attempt dir}"
	read_build
	python3 "${TOOLS}" ios-crash "${REPORTS_DIR}" "${since}" "${EXECUTABLE}" "${dir}/crash.txt"
}

cmd_collect_failure() {
	local dir="${1:?attempt dir}"
	resolve_device
	read_build
	if ! compgen -G "${dir}/*.png" > /dev/null; then
		xcrun simctl io "${UDID}" screenshot "${dir}/fallback-screen.png" >&2
	fi
	xcrun simctl spawn "${UDID}" log show --last 3m --style compact --predicate "process == \"${EXECUTABLE}\"" | tail -n 2000 > "${dir}/app.log"
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
	run-scenario) shift; cmd_run_scenario "$@" ;;
	crash-probe) shift; cmd_crash_probe "$@" ;;
	collect-failure) shift; cmd_collect_failure "$@" ;;
	*) printf 'Usage: %s toolchain|ensure-device|build|install|enumerate|health|reset-app|run-scenario|crash-probe|collect-failure|recover|stop-device\n' "$0" >&2; exit 64 ;;
esac
