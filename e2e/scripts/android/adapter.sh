#!/usr/bin/env bash
# Android adapter of the E2E harness (plan section 4.5). JSON on stdout, logs on stderr.
#
#   toolchain | ensure-device | health | recover | stop-device    delegated to device.sh
#   build <port>                          build.sh; remembers the APKs for the verbs below
#   install                               installs the app and the scenario runner
#   enumerate                             the runner's tests, without running them
#   driver-contract <dir> <port>          the driver's contract and the on-device driver probes (DriverContractTest, AndroidDriverProbesTest,
#                                         AndroidTypingProbesTest minus its typingSeries measurement, which needs -e typingSeries) in one
#                                         instrumentation run on a cleared app; {ok, passed, failed, skipped, artifacts}
#   reset-app                             pm clear of the app and the runner's output directory (never pm clear of the runner)
#   run-scenario <id> <attemptDir> <port> exactly one instrumentation run; the verdict comes from result.json, not from adb
#   crash-probe <sinceEpoch> <attemptDir> [<untilEpoch> <waitSeconds>]   crash or ANR evidence in the device log (the log is cleared
#                                         per attempt and the evidence is immediate, so the optional arguments are not needed)
#   collect-failure <attemptDir> <sinceEpoch>   failed attempts only: the device log of the attempt (capped), a fallback screenshot
# Every adb call carries the serial of the pinned emulator.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
source "${SCRIPT_DIR}/../shared/lib.sh"
source "${SCRIPT_DIR}/../shared/layout.env"

DEVICE="${SCRIPT_DIR}/device.sh"
TOOLS="${SCRIPT_DIR}/../shared/adapter_tools.py"
WORK="${E2E_TMP_ROOT:-${TMPDIR:-/tmp}/tuindice-e2e}/android"
BUILD_STATE="${WORK}/build.json"
SUITE_CLASS="${E2E_ANDROID_TEST_PACKAGE}.ScenarioSuiteTest"
TEST_OUTPUT_DIR="files/e2e"
# The device log kept for a failed attempt is at most this long (the last bytes of it); E2E_FAKE_LOG_CAP_BYTES is the test seam.
LOG_CAP_BYTES="${E2E_FAKE_LOG_CAP_BYTES:-10485760}"

# Sets SERIAL, APP_ID, TEST_ID and the APK paths; the first needs the pinned emulator, the others need a build.
resolve_device() {
	SERIAL="$(bash "${DEVICE}" serial)"
}

read_build_state() {
	read_build app=APP_APK appId=APP_ID test=TEST_APK testId=TEST_ID
}

adb_s() {
	adb -s "${SERIAL}" "$@"
}

wiremock_url() { # the WireMock URL as the emulator reaches it
	if [[ "${E2E_ANDROID_TUNNEL:-host-alias}" == "reverse" ]]; then
		printf 'http://localhost:%s\n' "$1"
	else
		printf 'http://10.0.2.2:%s\n' "$1"
	fi
}

instrument() { # <extra am instrument args...>; the raw output goes to stdout
	adb_s shell am instrument -w -r "$@" "${TEST_ID}/${E2E_ANDROID_TEST_RUNNER}"
}

cmd_build() {
	mkdir -p "${WORK}"
	bash "${SCRIPT_DIR}/build.sh" "${1:?build needs the WireMock port}" | tee "${BUILD_STATE}"
}

cmd_install() {
	resolve_device
	read_build_state
	adb_s install -r -t "${APP_APK}" >&2
	adb_s install -r -t "${TEST_APK}" >&2
	emit_json "ok=j:true" "app=s:${APP_ID}" "test=s:${TEST_ID}"
}

cmd_enumerate() {
	local listing="${WORK}/enumerate.log"
	resolve_device
	read_build_state
	instrument -e log true -e class "${SUITE_CLASS}" > "${listing}"
	python3 "${TOOLS}" instrument-tests "${listing}"
}

# The app cleared and the runner's output directory emptied (the state every attempt and the driver contract start from).
clear_app() {
	local answer
	answer="$(adb_s shell pm clear "${APP_ID}" | tr -d '\r')"
	[[ "${answer}" == "Success" ]] || fail "pm clear ${APP_ID} answered '${answer}'"
	# Only the runner's output directory is emptied: pm clear of the test package would erase result.json.
	adb_s shell am force-stop "${TEST_ID}"
	adb_s shell run-as "${TEST_ID}" rm -rf "${TEST_OUTPUT_DIR}" || fail "run-as ${TEST_ID} could not empty ${TEST_OUTPUT_DIR}"
	adb_s logcat -b all -c
}

cmd_reset_app() {
	resolve_device
	read_build_state
	clear_app
	emit_json "ok=j:true"
}

stop_instrumentation() {
	adb_s shell am force-stop "${TEST_ID}"
	adb_s shell am force-stop "${APP_ID}"
}

# The scenario's files, from the runner's internal directory (readable through run-as because the APK is debuggable):
# result.json, driver.log (written line by line, so a hung run leaves it) and, after a failure, the capture.
pull_output() { # id dir
	local names name
	# `shell` propagates the exit status of the remote ls; `exec-out` exits 0 and prints the error as output, which must never be iterated.
	if ! names="$(adb_s shell run-as "${TEST_ID}" ls "${TEST_OUTPUT_DIR}/$1" 2> /dev/null | tr -d '\r')"; then
		log "the runner left no output directory for $1 on the device"
		return 0
	fi
	for name in ${names}; do
		# Under its name only when whole: collect-failure looks for the name, so a cut transfer is repeated, never taken as the log.
		adb_s exec-out run-as "${TEST_ID}" cat "${TEST_OUTPUT_DIR}/$1/${name}" > "$2/${name}.part" && mv "$2/${name}.part" "$2/${name}"
	done
}

cmd_run_scenario() {
	local id="${1:?id}" dir="${2:?attempt dir}" port="${3:?port}" pid status=0 args
	resolve_device
	read_build_state
	mkdir -p "${dir}"
	args=(-e class "${SUITE_CLASS}" -e scenario "${id}" -e wiremockUrl "$(wiremock_url "${port}")")
	instrument "${args[@]}" > "${dir}/runner.log" 2>&1 &
	pid=$!
	# The harness stops a hung run with SIGTERM: stop the on-device instrumentation too, or it outlives adb, and bring home the driver.log
	# it wrote line by line (the next reset-app empties the directory); a failed pull only says so.
	trap 'stop_instrumentation || log "the instrumentation could not be stopped"; pull_output "${id}" "${dir}" || log "the driver.log of the cut run could not be pulled"; kill "${pid}" 2> /dev/null || printf "adb already exited\n" >&2; exit 143' TERM INT
	wait "${pid}" || status=$?
	trap - TERM INT
	log "am instrument exited ${status}; the verdict is read from result.json"
	pull_output "${id}" "${dir}"
	python3 "${TOOLS}" instrument-summary "${dir}/runner.log"
}

CONTRACT_CLASSES="${E2E_ANDROID_TEST_PACKAGE}.DriverContractTest,${E2E_ANDROID_TEST_PACKAGE}.AndroidDriverProbesTest,${E2E_ANDROID_TEST_PACKAGE}.AndroidTypingProbesTest"
# The contract test must pass, and each class must have a passing test; the rest of the run is required as well (adapter_tools.py).
CONTRACT_REQUIRED="DriverContractTest#driverHonoursTheContract,AndroidDriverProbesTest,AndroidTypingProbesTest"

cmd_driver_contract() {
	local dir="${1:?artifacts dir}" port="${2:?port}" status=0 id
	resolve_device
	read_build_state
	mkdir -p "${dir}"
	clear_app
	# No `-e scenario`: a filter would make the contract skip itself. The runner's own directories (the contract's and each probe's)
	# hold result.json and driver.log; they are pulled so that a red probe leaves its record.
	instrument -e class "${CONTRACT_CLASSES}" -e wiremockUrl "$(wiremock_url "${port}")" > "${dir}/runner.log" 2>&1 || status=$?
	log "am instrument exited ${status}; the probes are read from its status stream and result.json"
	for id in $(adb_s shell run-as "${TEST_ID}" ls "${TEST_OUTPUT_DIR}" 2> /dev/null | tr -d '\r'); do
		mkdir -p "${dir}/${id}"
		pull_output "${id}" "${dir}/${id}"
	done
	python3 "${TOOLS}" instrument-probes "${dir}/runner.log" "${CONTRACT_REQUIRED}" "${dir}/driver-contract/result.json" "${dir}"
}

cmd_crash_probe() {
	local since="${1:?since}" dir="${2:?attempt dir}" events="${WORK}/crash-probe.log"
	resolve_device
	read_build_state
	adb_s logcat -b all -d -v epoch > "${events}"
	python3 "${TOOLS}" logcat-crash "${events}" "${since}" "${APP_ID}" "${dir}/crash.txt"
}

cmd_collect_failure() {
	local dir="${1:?attempt dir}" since="${2:?since}" events="${WORK}/failure-logcat.log"
	resolve_device
	mkdir -p "${WORK}"
	# Read once from the device's log store, only now that the attempt failed: nothing streams while scenarios run.
	adb_s logcat -b all -d -v epoch > "${events}"
	python3 "${TOOLS}" logcat-window "${events}" "${since}" "${LOG_CAP_BYTES}" "${dir}/logcat.txt" >&2
	if ! compgen -G "${dir}/*.png" > /dev/null; then
		adb_s exec-out screencap -p > "${dir}/fallback-screen.png"
		if adb_s shell uiautomator dump /sdcard/e2e-window.xml > /dev/null 2>&1; then
			adb_s exec-out cat /sdcard/e2e-window.xml > "${dir}/fallback-hierarchy.xml"
		else
			log "uiautomator dump failed; the fallback hierarchy is missing"
		fi
	fi
	# A run the harness killed pulled nothing, and the next reset-app empties the runner's directory: its driver.log, written line by
	# line, comes home now, after the log and the screen (the evidence that does not depend on the directory). Only an attempt that
	# started the instrumentation (runner.log) has a directory of its own on the device; any other would bring another attempt's files.
	if [[ -n "${E2E_CURRENT_SCENARIO:-}" && -e "${dir}/runner.log" && ! -e "${dir}/driver.log" ]]; then
		read_build_state
		pull_output "${E2E_CURRENT_SCENARIO}" "${dir}"
	fi
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
