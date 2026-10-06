#!/usr/bin/env bash
# Measures the native scenario runners of the proof of concept, one platform at a time, and writes
# the raw record of every run under <out>/<platform>/. `--report` turns the records of both
# platforms into <out>/report.md (scripts/measure-scenario-poc-report.py).
#
# Every run is ONE runner invocation with the cleanup the harness will apply before it:
#   Android  pm clear of the app (never of the test package), then `am instrument -e class ...ScenarioSuiteTest`
#            (AGP's task is not used: its XML reports every Assume as a failure).
#   iOS      simctl terminate + uninstall + keychain reset, reinstall of the app, then
#            `xcodebuild test-without-building` with the canonical flags of the F12 notes
#            (-collect-test-diagnostics never, -parallel-testing-enabled NO, CODE_SIGNING_ALLOWED=NO).
# A run that fails is recorded with its artifacts and the series goes on: nothing is retried and no
# timeout is adjusted. The only automatic stop is a systematic failure (the first runs of a block
# all not green) or an unhealthy WireMock, which are preparation problems, not data.
#
# Usage:
#   scripts/measure-scenario-poc.sh --platform android|ios [--runs N] [--out DIR]
#       [--serial SERIAL | --udid UDID] [--wiremock-port PORT] [--derived-data DIR]
#       [--plan kind:count,...] [--type-chunk N] [--series-name NAME]
#   scripts/measure-scenario-poc.sh --report [--out DIR]
#
# `--runs N` is the count for each of the three scenarios; the extra auth-login-cancel block and the
# expected-failure block get N/2 (20 -> 30 typing samples and 10 expected failures). `--plan`
# replaces that plan; a kind is a scenario id or `expected-failure` (on iOS that run is one
# invocation with poc-expected-failure and summary-profile-picture, to prove the second test runs).
# `--type-chunk` is forwarded as TEST_RUNNER_E2E_TYPE_CHUNK for the iOS typing ladder.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CATALOG="$ROOT_DIR/e2e/catalog/scenarios.json"

PLATFORM=""
RUNS=20
OUT_DIR="$ROOT_DIR/build/e2e/poc"
SERIAL="${ANDROID_SERIAL:-emulator-5554}"
UDID="${IOS_UDID:-F3F8297A-D2D4-493A-BF07-4AA0AA7A7528}"
WIREMOCK_PORT=""
DERIVED_DATA="${IOS_DERIVED_DATA:-/tmp/tuindice-e2e/f12/derived-data}"
PLAN=""
TYPE_CHUNK=""
SERIES_NAME=""
REPORT_ONLY=0

TIMEOUT_SECONDS=300
LOAD_WAIT_SECONDS=600
LOAD_THRESHOLD=8.0
ABORT_AFTER_UNGREEN=3

APP_ID="com.gdavidpb.tuindice.debug"
ANDROID_TEST_PACKAGE="com.gdavidpb.tuindice.scenariorunner"
ANDROID_SUITE_CLASS="com.gdavidpb.tuindice.scenariorunner.ScenarioSuiteTest"
LOGIN_CANCEL_EXPECTED="11-11111:login-cancel-pass"
AUTH_SCENARIO="auth-login-cancel"
EXPECTED_FAILURE_SCENARIO="poc-expected-failure"
SECOND_TEST_SCENARIO="summary-profile-picture"
DECODE_KIND="catalog-decode"
DECODE_TEST="TuIndiceUITests/CatalogDecodeTimingTests/test_catalog_decode_time"

fail() {
	echo "measure-scenario-poc: $*" >&2
	exit 2
}

usage() {
	sed -n '2,/^set -euo/p' "$0" | sed '$d' | sed 's/^# \{0,1\}//'
}

while [[ $# -gt 0 ]]; do
	case "$1" in
		--platform) PLATFORM="${2:?--platform needs android or ios}"; shift 2 ;;
		--runs) RUNS="${2:?--runs needs a number}"; shift 2 ;;
		--out) OUT_DIR="${2:?--out needs a directory}"; shift 2 ;;
		--serial) SERIAL="${2:?--serial needs a value}"; shift 2 ;;
		--udid) UDID="${2:?--udid needs a value}"; shift 2 ;;
		--wiremock-port) WIREMOCK_PORT="${2:?--wiremock-port needs a number}"; shift 2 ;;
		--derived-data) DERIVED_DATA="${2:?--derived-data needs a directory}"; shift 2 ;;
		--plan) PLAN="${2:?--plan needs kind:count,...}"; shift 2 ;;
		--type-chunk) TYPE_CHUNK="${2:?--type-chunk needs a number}"; shift 2 ;;
		--series-name) SERIES_NAME="${2:?--series-name needs a name}"; shift 2 ;;
		--report) REPORT_ONLY=1; shift ;;
		-h | --help) usage; exit 0 ;;
		*) fail "unknown argument: $1" ;;
	esac
done

if [[ "$REPORT_ONLY" -eq 1 ]]; then
	exec python3 "$ROOT_DIR/scripts/measure-scenario-poc-report.py" --out "$OUT_DIR"
fi

[[ "$PLATFORM" == "android" || "$PLATFORM" == "ios" ]] || fail "--platform android|ios is required"
[[ "$RUNS" =~ ^[0-9]+$ && "$RUNS" -ge 2 ]] || fail "--runs must be an integer >= 2"
[[ -f "$CATALOG" ]] || fail "missing $CATALOG"

if [[ -z "$WIREMOCK_PORT" ]]; then
	if [[ "$PLATFORM" == "android" ]]; then WIREMOCK_PORT=18626; else WIREMOCK_PORT=18627; fi
fi
SERIES_NAME="${SERIES_NAME:-$PLATFORM}"
SERIES_DIR="$OUT_DIR/$SERIES_NAME"
[[ ! -e "$SERIES_DIR" ]] || fail "$SERIES_DIR already exists; choose another --out or --series-name (nothing is overwritten)"
mkdir -p "$SERIES_DIR"

HOST_WIREMOCK_URL="http://127.0.0.1:$WIREMOCK_PORT"
if [[ "$PLATFORM" == "android" ]]; then
	RUNNER_WIREMOCK_URL="http://10.0.2.2:$WIREMOCK_PORT"
else
	RUNNER_WIREMOCK_URL="http://localhost:$WIREMOCK_PORT"
fi

# --- helpers --------------------------------------------------------------------------------------

now_ms() {
	python3 -c 'import time; print(int(time.time() * 1000))'
}

iso_now() {
	date '+%Y-%m-%dT%H:%M:%S%z'
}

load1() {
	sysctl -n vm.loadavg | awk '{ print $2 }'
}

# Runs a command in its own process group with a hard timeout, killing the whole group when it
# expires. Output goes to the log; the elapsed milliseconds go to the elapsed file. Exit status:
# the command's own, or 124 when it was killed for taking longer than the timeout.
run_timed() {
	local seconds="$1" log="$2" elapsed_file="$3"
	shift 3
	python3 - "$seconds" "$log" "$elapsed_file" "$@" <<'PY'
import os, signal, subprocess, sys, time

seconds, log, elapsed_file = float(sys.argv[1]), sys.argv[2], sys.argv[3]
command = sys.argv[4:]
start = time.monotonic()
with open(log, "wb") as out:
    process = subprocess.Popen(command, stdout=out, stderr=subprocess.STDOUT, stdin=subprocess.DEVNULL, start_new_session=True)
    try:
        code = process.wait(timeout=seconds)
    except subprocess.TimeoutExpired:
        os.killpg(process.pid, signal.SIGKILL)
        process.wait()
        code = 124
with open(elapsed_file, "w") as f:
    f.write(str(int((time.monotonic() - start) * 1000)))
sys.exit(code)
PY
}

# catalog_field <scenario id> <dotted path>
catalog_field() {
	python3 - "$CATALOG" "$1" "$2" <<'PY'
import json, sys

catalog = json.load(open(sys.argv[1]))
scenario = next((s for s in catalog["scenarios"] if s["id"] == sys.argv[2]), None)
if scenario is None:
    sys.exit("unknown scenario " + sys.argv[2])
value = scenario
for key in sys.argv[3].split("."):
    value = value[key]
print(value)
PY
}

# Writes <run dir>/auth-check.json for an auth-login-cancel run: the Authorization of every
# POST /auth/v2/bootstrap in the journal, decoded, against the credential the scenario types.
check_bootstrap_credential() {
	python3 - "$1/journal.json" "$LOGIN_CANCEL_EXPECTED" "$1/auth-check.json" <<'PY'
import base64, json, sys

journal_path, expected, out_path = sys.argv[1:4]
try:
    requests = json.load(open(journal_path)).get("requests", [])
except (OSError, ValueError) as error:
    json.dump({"expected": expected, "verdict": "no-journal", "error": str(error), "decoded": []}, open(out_path, "w"))
    sys.exit(0)

decoded = []
for entry in requests:
    request = entry.get("request", {})
    if request.get("method") != "POST" or not request.get("url", "").split("?")[0].endswith("/auth/v2/bootstrap"):
        continue
    headers = {k.lower(): v for k, v in request.get("headers", {}).items()}
    authorization = headers.get("authorization", "")
    if authorization.lower().startswith("basic "):
        try:
            text = base64.b64decode(authorization.split(" ", 1)[1]).decode("utf-8")
        except ValueError:
            text = "<undecodable: %s>" % authorization
    else:
        text = "<no basic authorization: %s>" % authorization
    decoded.append({"decoded": text, "status": entry.get("response", {}).get("status")})

if not decoded:
    verdict = "no-bootstrap-request"
elif all(item["decoded"] == expected for item in decoded):
    verdict = "match"
else:
    verdict = "mismatch"
json.dump({"expected": expected, "verdict": verdict, "decoded": decoded}, open(out_path, "w"), indent=1)
PY
}

# --- plan -----------------------------------------------------------------------------------------

if [[ -z "$PLAN" ]]; then
	extra=$((RUNS / 2))
	PLAN="$AUTH_SCENARIO:$RUNS,evaluations-swipe-delete:$RUNS,$SECOND_TEST_SCENARIO:$RUNS,$AUTH_SCENARIO:$extra,expected-failure:$extra"
	# Catalog decode time through the Kotlin->Swift bridge: five fresh test processes (five cold samples).
	if [[ "$PLATFORM" == "ios" ]]; then PLAN="$PLAN,$DECODE_KIND:5"; fi
fi

IFS=',' read -r -a PLAN_ITEMS <<<"$PLAN"
for item in "${PLAN_ITEMS[@]}"; do
	[[ "$item" =~ ^[a-z0-9-]+:[0-9]+$ ]] || fail "bad plan item '$item' (expected kind:count)"
	if [[ "${item%%:*}" == "$DECODE_KIND" && "$PLATFORM" != "ios" ]]; then fail "$DECODE_KIND exists only on ios"; fi
done

# --- environment record ----------------------------------------------------------------------------

record_environment() {
	local avd_path=""
	{
		echo "platform=$PLATFORM"
		echo "series=$SERIES_NAME"
		echo "plan=$PLAN"
		echo "timeout_seconds=$TIMEOUT_SECONDS"
		echo "wiremock_port=$WIREMOCK_PORT"
		echo "wiremock_runner_url=$RUNNER_WIREMOCK_URL"
		echo "host_model=$(sysctl -n hw.model)"
		echo "host_cores=$(sysctl -n hw.ncpu)"
		echo "host_memory_bytes=$(sysctl -n hw.memsize)"
		echo "host_os=$(sw_vers -productName) $(sw_vers -productVersion) ($(sw_vers -buildVersion))"
		echo "host_kernel=$(uname -r)"
		echo "git_head=$(git -C "$ROOT_DIR" rev-parse HEAD)"
		echo "git_dirty=$(git -C "$ROOT_DIR" status --porcelain | wc -l | tr -d ' ')"
		if [[ "$PLATFORM" == "android" ]]; then
			echo "android_serial=$SERIAL"
			echo "android_avd=$(adb -s "$SERIAL" emu avd name 2>/dev/null | head -1 | tr -d '\r')"
			avd_path="$(adb -s "$SERIAL" emu avd path 2>/dev/null | head -1 | tr -d '\r')"
			echo "android_avd_path=$avd_path"
			echo "android_image_sysdir=$(grep -E '^image.sysdir.1' "$avd_path/config.ini" 2>/dev/null | cut -d= -f2 || echo unknown)"
			echo "android_hw_ram=$(grep -E '^hw.ramSize' "$avd_path/config.ini" 2>/dev/null | cut -d= -f2 || echo unknown)"
			echo "android_hw_cores=$(grep -E '^hw.cpu.ncore' "$avd_path/config.ini" 2>/dev/null | cut -d= -f2 || echo unknown)"
			echo "android_release=$(adb -s "$SERIAL" shell getprop ro.build.version.release | tr -d '\r')"
			echo "android_sdk=$(adb -s "$SERIAL" shell getprop ro.build.version.sdk | tr -d '\r')"
			echo "android_model=$(adb -s "$SERIAL" shell getprop ro.product.model | tr -d '\r')"
			echo "android_abi=$(adb -s "$SERIAL" shell getprop ro.product.cpu.abi | tr -d '\r')"
			echo "android_fingerprint=$(adb -s "$SERIAL" shell getprop ro.build.fingerprint | tr -d '\r')"
			echo "android_page_size=$(adb -s "$SERIAL" shell getconf PAGE_SIZE | tr -d '\r')"
		else
			echo "ios_udid=$UDID"
			echo "ios_derived_data=$DERIVED_DATA"
			echo "ios_type_chunk=${TYPE_CHUNK:-default}"
			echo "xcode=$(xcodebuild -version | tr '\n' ' ')"
			python3 - "$UDID" <<'PY'
import json, subprocess, sys

udid = sys.argv[1]
devices = json.loads(subprocess.check_output(["xcrun", "simctl", "list", "-j", "devices"]))["devices"]
for runtime, entries in devices.items():
    for entry in entries:
        if entry["udid"] == udid:
            print("ios_runtime=" + runtime)
            print("ios_device=" + entry["name"])
            print("ios_state=" + entry["state"])
PY
		fi
	} >"$SERIES_DIR/series.env"
}

# --- preparation ----------------------------------------------------------------------------------

wait_for_load() {
	local waited=0 current
	current="$(load1)"
	while awk -v l="$current" -v t="$LOAD_THRESHOLD" 'BEGIN { exit !(l >= t) }'; do
		if [[ "$waited" -ge "$LOAD_WAIT_SECONDS" ]]; then
			echo "load_wait=gave-up after ${waited}s with load1=$current (threshold $LOAD_THRESHOLD); starting anyway" >>"$SERIES_DIR/series.env"
			echo "load1 stayed at $current for ${waited}s (threshold $LOAD_THRESHOLD); starting anyway."
			return 0
		fi
		sleep 15
		waited=$((waited + 15))
		current="$(load1)"
	done
	echo "load_wait=ok after ${waited}s with load1=$current (threshold $LOAD_THRESHOLD)" >>"$SERIES_DIR/series.env"
}

prepare_android() {
	adb -s "$SERIAL" get-state >/dev/null
	local app_apk test_apk
	app_apk="$ROOT_DIR/app/build/outputs/apk/debug/app-debug.apk"
	test_apk="$ROOT_DIR/scenariorunner/build/outputs/apk/debug/scenariorunner-debug.apk"
	[[ -f "$app_apk" && -f "$test_apk" ]] || fail "build the APKs first (:app:assembleDebug :scenariorunner:assembleDebug)"
	adb -s "$SERIAL" install -r -t "$app_apk" >"$SERIES_DIR/install-app.log" 2>&1
	adb -s "$SERIAL" install -r -t "$test_apk" >"$SERIES_DIR/install-test.log" 2>&1
	echo "apk_app_sha256=$(shasum -a 256 "$app_apk" | cut -d' ' -f1)" >>"$SERIES_DIR/series.env"
	echo "apk_test_sha256=$(shasum -a 256 "$test_apk" | cut -d' ' -f1)" >>"$SERIES_DIR/series.env"
}

IOS_APP_PATH=""
prepare_ios() {
	IOS_APP_PATH="$DERIVED_DATA/Build/Products/Debug-iphonesimulator/TuIndiceHost.app"
	[[ -d "$IOS_APP_PATH" ]] || fail "missing $IOS_APP_PATH; run build-for-testing first"
	[[ "$(xcrun simctl list devices | grep -c "$UDID.*Booted")" -ge 1 ]] || fail "simulator $UDID is not booted"
	echo "ios_xctest_bundle_mtime=$(stat -f %Sm -t %Y-%m-%dT%H:%M:%S "$DERIVED_DATA/Build/Products/Debug-iphonesimulator/TuIndiceHost.app/PlugIns/TuIndiceUITests.xctest" 2>/dev/null || echo unknown)" >>"$SERIES_DIR/series.env"
}

# --- one run ---------------------------------------------------------------------------------------

# Cleanup, exactly what the harness will do before an invocation.
reset_app_android() {
	local out
	out="$(adb -s "$SERIAL" shell pm clear "$APP_ID" | tr -d '\r')"
	[[ "$out" == "Success" ]] || { echo "pm clear answered: $out" >&2; return 1; }
	# The test APK is never cleared; only the output directory of the scenarios of this run.
	for id in "$@"; do
		adb -s "$SERIAL" shell run-as "$ANDROID_TEST_PACKAGE" rm -rf "files/e2e/$id"
	done
}

reset_app_ios() {
	local terminate_out
	if ! terminate_out="$(xcrun simctl terminate "$UDID" "$APP_ID" 2>&1)"; then
		# Not running is the normal case after the previous run's cleanup; anything else is a failure.
		case "$terminate_out" in
			*"found nothing to terminate"* | *"not found"*) echo "terminate: $terminate_out" ;;
			*) echo "terminate failed: $terminate_out" >&2; return 1 ;;
		esac
	fi
	xcrun simctl uninstall "$UDID" "$APP_ID"
	xcrun simctl keychain "$UDID" reset
	xcrun simctl install "$UDID" "$IOS_APP_PATH"
}

invoke_android() {
	local run_dir="$1" id="$2"
	run_timed "$TIMEOUT_SECONDS" "$run_dir/runner.log" "$run_dir/wall_ms" \
		adb -s "$SERIAL" shell am instrument -w -r \
		-e class "$ANDROID_SUITE_CLASS" -e scenario "$id" -e wiremockUrl "$RUNNER_WIREMOCK_URL" \
		"$ANDROID_TEST_PACKAGE/androidx.test.runner.AndroidJUnitRunner"
}

pull_android_artifacts() {
	local run_dir="$1" id="$2" files name
	mkdir -p "$run_dir/artifacts"
	if ! files="$(adb -s "$SERIAL" exec-out run-as "$ANDROID_TEST_PACKAGE" ls "files/e2e/$id" 2>&1 | tr -d '\r')"; then
		echo "no output directory for $id: $files" >"$run_dir/artifacts/pull.err"
		files=""
	fi
	for name in $files; do
		adb -s "$SERIAL" exec-out run-as "$ANDROID_TEST_PACKAGE" cat "files/e2e/$id/$name" >"$run_dir/artifacts/$name"
	done
	if [[ -f "$run_dir/artifacts/result.json" ]]; then
		cp "$run_dir/artifacts/result.json" "$run_dir/result.json"
	fi
}

invoke_ios() {
	local run_dir="$1"
	shift
	local only=() test_name
	for test_name in "$@"; do
		only+=("-only-testing:$test_name")
	done
	mkdir -p "$run_dir/results"
	export TEST_RUNNER_E2E_WIREMOCK_URL="$RUNNER_WIREMOCK_URL"
	export TEST_RUNNER_E2E_OUTPUT_DIR="$run_dir/results"
	export TEST_RUNNER_E2E_TRACE=1
	if [[ -n "$TYPE_CHUNK" ]]; then
		export TEST_RUNNER_E2E_TYPE_CHUNK="$TYPE_CHUNK"
	fi
	run_timed "$TIMEOUT_SECONDS" "$run_dir/runner.log" "$run_dir/wall_ms" \
		xcodebuild test-without-building \
		-workspace "$ROOT_DIR/iosApp/TuIndiceHost.xcworkspace" -scheme TuIndiceUITests \
		-configuration Debug -sdk iphonesimulator \
		-destination "platform=iOS Simulator,id=$UDID" -derivedDataPath "$DERIVED_DATA" \
		"${only[@]}" -parallel-testing-enabled NO -collect-test-diagnostics never \
		-resultBundlePath "$run_dir/run.xcresult" CODE_SIGNING_ALLOWED=NO
}

# The scenario ids whose result.json a run of this kind leaves, in test order.
ids_for_kind() {
	if [[ "$1" == "$DECODE_KIND" ]]; then
		echo "$DECODE_KIND"
	elif [[ "$1" == "expected-failure" ]]; then
		if [[ "$PLATFORM" == "ios" ]]; then
			echo "$EXPECTED_FAILURE_SCENARIO $SECOND_TEST_SCENARIO"
		else
			echo "$EXPECTED_FAILURE_SCENARIO"
		fi
	else
		echo "$1"
	fi
}

LOAD_MAX="0"
track_load() {
	local current="$1"
	if awk -v a="$current" -v b="$LOAD_MAX" 'BEGIN { exit !(a > b) }'; then
		LOAD_MAX="$current"
	fi
}

health_ok() {
	curl -sS -L -f -m 5 -o /dev/null "$HOST_WIREMOCK_URL/__admin/" 2>"$1"
}

# run_one <seq> <block> <kind>   (returns 0 when green, 1 when not green, 3 when WireMock is unhealthy)
run_one() {
	local seq="$1" block="$2" kind="$3"
	local ids id run_dir name
	read -r -a ids <<<"$(ids_for_kind "$kind")"
	name="$(printf '%03d' "$seq")-$kind"
	run_dir="$SERIES_DIR/$name"
	mkdir -p "$run_dir"

	local started_at uptime_start load_start health="ok" cleanup_start cleanup_ms exit_code timed_out=0
	started_at="$(iso_now)"
	uptime_start="$(uptime | sed 's/^ *//')"
	load_start="$(load1)"
	track_load "$load_start"

	if ! health_ok "$run_dir/health.err"; then
		health="fail"
		{
			echo "seq=$seq"
			echo "block=$block"
			echo "kind=$kind"
			echo "started_at=$started_at"
			echo "health=fail"
		} >"$run_dir/run.env"
		echo "[$name] WireMock health check failed ($HOST_WIREMOCK_URL/__admin/): $(cat "$run_dir/health.err")" >&2
		return 3
	fi

	cleanup_start="$(now_ms)"
	if [[ "$PLATFORM" == "android" ]]; then
		reset_app_android "${ids[@]}" >"$run_dir/cleanup.log" 2>&1 || { echo "[$name] cleanup failed; see $run_dir/cleanup.log" >&2; return 4; }
	else
		reset_app_ios >"$run_dir/cleanup.log" 2>&1 || { echo "[$name] cleanup failed; see $run_dir/cleanup.log" >&2; return 4; }
	fi
	cleanup_ms=$(($(now_ms) - cleanup_start))

	if [[ "$PLATFORM" == "android" ]]; then
		invoke_android "$run_dir" "${ids[0]}" || exit_code=$?
	else
		local tests=() scenario
		for scenario in "${ids[@]}"; do
			if [[ "$scenario" == "$DECODE_KIND" ]]; then
				tests+=("$DECODE_TEST")
			else
				tests+=("$(catalog_field "$scenario" ios.onlyTesting)")
			fi
		done
		invoke_ios "$run_dir" "${tests[@]}" || exit_code=$?
	fi
	exit_code="${exit_code:-0}"
	if [[ "$exit_code" -eq 124 ]]; then
		timed_out=1
		if [[ "$PLATFORM" == "android" ]]; then
			# A killed adb leaves the instrumentation running on the device; stop both processes.
			adb -s "$SERIAL" shell am force-stop "$ANDROID_TEST_PACKAGE"
			adb -s "$SERIAL" shell am force-stop "$APP_ID"
		fi
	fi

	curl -sS -m 10 "$HOST_WIREMOCK_URL/__admin/requests" >"$run_dir/journal.json" 2>"$run_dir/journal.err" || echo "[$name] journal not saved: $(cat "$run_dir/journal.err")" >&2

	local scenario_dir
	for id in "${ids[@]}"; do
		if [[ "$PLATFORM" == "android" ]]; then
			if [[ "$timed_out" -eq 0 ]]; then pull_android_artifacts "$run_dir" "$id"; fi
		else
			scenario_dir="$run_dir/results/$id"
			if [[ -f "$scenario_dir/result.json" ]]; then
				# The first scenario's files are the run's; a second test keeps its own directory.
				[[ -f "$run_dir/result.json" ]] || cp "$scenario_dir/result.json" "$run_dir/result.json"
			fi
		fi
	done
	if [[ "$kind" == "$AUTH_SCENARIO" ]]; then
		check_bootstrap_credential "$run_dir" || { echo "[$name] credential check failed" >&2; return 4; }
	fi

	local uptime_end load_end
	uptime_end="$(uptime | sed 's/^ *//')"
	load_end="$(load1)"
	track_load "$load_end"

	{
		echo "seq=$seq"
		echo "block=$block"
		echo "kind=$kind"
		echo "scenarios=${ids[*]}"
		echo "started_at=$started_at"
		echo "uptime_start=$uptime_start"
		echo "uptime_end=$uptime_end"
		echo "load1_start=$load_start"
		echo "load1_end=$load_end"
		echo "health=$health"
		echo "cleanup_ms=$cleanup_ms"
		echo "wall_ms=$(cat "$run_dir/wall_ms")"
		echo "exit_code=$exit_code"
		echo "timed_out=$timed_out"
	} >"$run_dir/run.env"

	local verdict="not-green"
	if [[ "$kind" == "$DECODE_KIND" ]]; then
		if [[ "$exit_code" -eq 0 && -f "$run_dir/results/$DECODE_KIND/decode.json" ]]; then verdict="green"; fi
	elif [[ "$exit_code" -eq 0 && -f "$run_dir/result.json" ]] && grep -q '"outcome":"passed"' "$run_dir/result.json"; then
		verdict="green"
	fi
	echo "[$name] $verdict exit=$exit_code timed_out=$timed_out wall=$(cat "$run_dir/wall_ms")ms cleanup=${cleanup_ms}ms load1=$load_start->$load_end"
	[[ "$verdict" == "green" ]]
}

# --- series ---------------------------------------------------------------------------------------

record_environment
wait_for_load
if [[ "$PLATFORM" == "android" ]]; then prepare_android; else prepare_ios; fi

{
	echo "series_start=$(iso_now)"
	echo "uptime_start=$(uptime | sed 's/^ *//')"
} >>"$SERIES_DIR/series.env"
echo "Series $SERIES_NAME on $PLATFORM, plan $PLAN, output $SERIES_DIR"

seq=0
block=0
stopped=""
for item in "${PLAN_ITEMS[@]}"; do
	kind="${item%%:*}"
	count="${item##*:}"
	block=$((block + 1))
	ungreen_in_a_row=0
	for ((n = 1; n <= count; n++)); do
		seq=$((seq + 1))
		status=0
		run_one "$seq" "$block" "$kind" || status=$?
		if [[ "$status" -eq 3 || "$status" -eq 4 ]]; then
			stopped="run $seq ($kind): preparation problem (status $status)"
			break 2
		fi
		if [[ "$kind" != "expected-failure" && "$kind" != "$DECODE_KIND" ]]; then
			if [[ "$status" -eq 0 ]]; then
				ungreen_in_a_row=0
			else
				ungreen_in_a_row=$((ungreen_in_a_row + 1))
			fi
			if [[ "$n" -le "$ABORT_AFTER_UNGREEN" && "$ungreen_in_a_row" -ge "$ABORT_AFTER_UNGREEN" ]]; then
				stopped="the first $ABORT_AFTER_UNGREEN runs of block $block ($kind) were not green: a systematic problem"
				break 2
			fi
		fi
	done
done

{
	echo "series_end=$(iso_now)"
	echo "uptime_end=$(uptime | sed 's/^ *//')"
	echo "load1_max_sampled=$LOAD_MAX"
	echo "runs_executed=$seq"
	echo "stopped=${stopped:-no}"
} >>"$SERIES_DIR/series.env"

if [[ -n "$stopped" ]]; then
	echo "STOPPED: $stopped" >&2
	exit 5
fi
echo "Series $SERIES_NAME finished: $seq runs."
