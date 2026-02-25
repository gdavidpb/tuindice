#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
CONFIGURATION_NAME="${CONFIGURATION:-Debug}"
DERIVED_DATA_PATH="${DERIVED_DATA_PATH:-$ROOT_DIR/.build/ios-host-smoke}"
SIMULATOR_NAME="${IOS_SIMULATOR_NAME:-iPhone 16}"
SKIP_HOST_BUILD="${SKIP_HOST_BUILD:-0}"
SHUTDOWN_SIMULATOR="${SHUTDOWN_SIMULATOR:-1}"
ENABLE_FUNCTIONAL_SMOKE="${ENABLE_FUNCTIONAL_SMOKE:-1}"
SMOKE_LOG_TIMEOUT_SECONDS="${SMOKE_LOG_TIMEOUT_SECONDS:-20}"
SMOKE_REQUIRED_CHECKS="${SMOKE_REQUIRED_CHECKS:-appenv:ok,network:,device:ok,user-agent:ok,file-gateway:ok,secure-store:ok,push:ok,attestation:APP_ATTEST,attestation-key-id:ok,startup:,destination:ok,review:,update:,config:ok,reporting:ok,browser-flow:ok,signin-flow:ok,about-flow:ok,summary-flow:ok,record-flow:ok,enrollment-flow:ok,evaluations-flow:ok,evaluation-flow:ok,feature-usecases:loading,viewmodels:ok}"
SMOKE_RUN_ID="${SMOKE_RUN_ID:-$(date +%s)}"
SMOKE_USBID="${SMOKE_USBID:-${TUINDICE_SMOKE_USBID:-}}"
SMOKE_PASSWORD="${SMOKE_PASSWORD:-${TUINDICE_SMOKE_PASSWORD:-}}"
AUTH_SMOKE_REQUIRED_CHECKS="${AUTH_SMOKE_REQUIRED_CHECKS:-signin-auth:ok,signout-auth:ok}"
REQUIRE_SIMULATOR="${REQUIRE_SIMULATOR:-0}"
HOST_APP_NAME="TuIndiceHost.app"
SMOKE_MARKER_PREFIX="TUINDICE_SMOKE_MARKER"

skip_or_fail() {
	local message="$1"

	if [[ "$REQUIRE_SIMULATOR" == "1" ]]; then
		echo "iOS host smoke failed: $message"
		exit 1
	fi

	echo "Skipping iOS host smoke: $message"
	exit 0
}

if [[ "$OSTYPE" != darwin* ]]; then
	skip_or_fail "non-macOS host."
fi

if ! command -v xcrun >/dev/null 2>&1; then
	skip_or_fail "xcrun is not available."
fi

SIMULATOR_DEVICES_OUTPUT="$(
	xcrun simctl list devices available 2>&1 || true
)"

if [[ "$SIMULATOR_DEVICES_OUTPUT" == *"Unable to locate device set"* ]] || \
	[[ "$SIMULATOR_DEVICES_OUTPUT" == *"CoreSimulatorService connection became invalid"* ]] || \
	[[ "$SIMULATOR_DEVICES_OUTPUT" == *"Connection refused"* ]]; then
	skip_or_fail "CoreSimulatorService is unavailable in this environment."
fi

if [[ "$SIMULATOR_DEVICES_OUTPUT" != *"iPhone"* ]]; then
	skip_or_fail "no available iPhone simulators."
fi

SIMULATOR_UDID="$(
	echo "$SIMULATOR_DEVICES_OUTPUT" | \
	awk -v name="$SIMULATOR_NAME" '
		$0 ~ name && $0 !~ /unavailable/ {
			gsub(/[()]/, "", $NF)
			print $NF
			exit
		}
	'
)"

if [[ -z "$SIMULATOR_UDID" ]]; then
	SIMULATOR_UDID="$(
		echo "$SIMULATOR_DEVICES_OUTPUT" | \
		awk '
			/iPhone/ && $0 !~ /unavailable/ {
				gsub(/[()]/, "", $NF)
				print $NF
				exit
			}
		'
	)"
fi

if [[ -z "$SIMULATOR_UDID" ]]; then
	skip_or_fail "no available iPhone simulator UDID found."
fi

if [[ "$SKIP_HOST_BUILD" != "1" ]]; then
	CONFIGURATION="$CONFIGURATION_NAME" \
	DERIVED_DATA_PATH="$DERIVED_DATA_PATH" \
	"$ROOT_DIR/scripts/ci-build-ios-host.sh"
fi

APP_BUNDLE_PATH="$DERIVED_DATA_PATH/Build/Products/${CONFIGURATION_NAME}-iphonesimulator/$HOST_APP_NAME"

if [[ ! -d "$APP_BUNDLE_PATH" ]]; then
	echo "iOS host smoke failed: app bundle not found at $APP_BUNDLE_PATH"
	exit 1
fi

BUNDLE_ID="$(
	/usr/libexec/PlistBuddy \
		-c "Print :CFBundleIdentifier" \
		"$APP_BUNDLE_PATH/Info.plist"
)"

if [[ -z "$BUNDLE_ID" ]]; then
	echo "iOS host smoke failed: bundle id is empty."
	exit 1
fi

xcrun simctl boot "$SIMULATOR_UDID" >/dev/null 2>&1 || true
xcrun simctl bootstatus "$SIMULATOR_UDID" -b

xcrun simctl uninstall "$SIMULATOR_UDID" "$BUNDLE_ID" >/dev/null 2>&1 || true
xcrun simctl install "$SIMULATOR_UDID" "$APP_BUNDLE_PATH"

LAUNCH_ARGUMENTS=()
if [[ "$ENABLE_FUNCTIONAL_SMOKE" == "1" ]]; then
	if [[ -n "$SMOKE_USBID" || -n "$SMOKE_PASSWORD" ]]; then
		if [[ -z "$SMOKE_USBID" || -z "$SMOKE_PASSWORD" ]]; then
			echo "iOS host smoke failed: both SMOKE_USBID and SMOKE_PASSWORD are required for authenticated smoke."
			exit 1
		fi

		if [[ "$SMOKE_REQUIRED_CHECKS" != *"signin-auth:ok"* ]]; then
			SMOKE_REQUIRED_CHECKS="${SMOKE_REQUIRED_CHECKS},${AUTH_SMOKE_REQUIRED_CHECKS}"
		fi
	fi

	LAUNCH_ARGUMENTS+=("TUINDICE_IOS_SMOKE_VALIDATE=1")
	LAUNCH_ARGUMENTS+=("TUINDICE_IOS_SMOKE_RUN_ID=$SMOKE_RUN_ID")
	if [[ -n "$SMOKE_USBID" && -n "$SMOKE_PASSWORD" ]]; then
		LAUNCH_ARGUMENTS+=("TUINDICE_SMOKE_USBID=$SMOKE_USBID")
		LAUNCH_ARGUMENTS+=("TUINDICE_SMOKE_PASSWORD=$SMOKE_PASSWORD")
	fi
fi

LAUNCH_OUTPUT="$(
	xcrun simctl launch "$SIMULATOR_UDID" "$BUNDLE_ID" "${LAUNCH_ARGUMENTS[@]}"
)"

echo "$LAUNCH_OUTPUT"

if [[ "$ENABLE_FUNCTIONAL_SMOKE" == "1" ]]; then
	SMOKE_RESULT=""
	SMOKE_LOGS=""
	SMOKE_MARKER_LINE=""

	for _ in $(seq 1 "$SMOKE_LOG_TIMEOUT_SECONDS"); do
		SMOKE_LOGS="$(
			xcrun simctl spawn "$SIMULATOR_UDID" log show \
				--style compact \
				--last 2m \
				--predicate "eventMessage CONTAINS \"$SMOKE_MARKER_PREFIX\"" 2>/dev/null || true
		)"

		if [[ "$SMOKE_LOGS" == *"$SMOKE_MARKER_PREFIX:$SMOKE_RUN_ID:PASS:"* ]]; then
			SMOKE_RESULT="PASS"
			SMOKE_MARKER_LINE="$(
				echo "$SMOKE_LOGS" | \
					grep "$SMOKE_MARKER_PREFIX:$SMOKE_RUN_ID:PASS:" | \
					tail -n 1 || true
			)"
			break
		fi

		if [[ "$SMOKE_LOGS" == *"$SMOKE_MARKER_PREFIX:$SMOKE_RUN_ID:FAIL:"* ]]; then
			SMOKE_RESULT="FAIL"
			SMOKE_MARKER_LINE="$(
				echo "$SMOKE_LOGS" | \
					grep "$SMOKE_MARKER_PREFIX:$SMOKE_RUN_ID:FAIL:" | \
					tail -n 1 || true
			)"
			break
		fi

		sleep 1
	done

	if [[ "$SMOKE_RESULT" != "PASS" ]]; then
		echo "iOS host smoke failed: missing functional smoke PASS marker for run id '$SMOKE_RUN_ID'."
		if [[ -n "$SMOKE_LOGS" ]]; then
			echo "$SMOKE_LOGS"
		fi
		xcrun simctl terminate "$SIMULATOR_UDID" "$BUNDLE_ID" >/dev/null 2>&1 || true
		if [[ "$SHUTDOWN_SIMULATOR" == "1" ]]; then
			xcrun simctl shutdown "$SIMULATOR_UDID" >/dev/null 2>&1 || true
		fi
		exit 1
	fi

	MISSING_SMOKE_CHECKS=()
	IFS=',' read -r -a REQUIRED_SMOKE_CHECK_ITEMS <<< "$SMOKE_REQUIRED_CHECKS"
	for required_check in "${REQUIRED_SMOKE_CHECK_ITEMS[@]}"; do
		required_check="$(echo "$required_check" | xargs)"

		if [[ -z "$required_check" ]]; then
			continue
		fi

		if [[ "$SMOKE_MARKER_LINE" != *"$required_check"* ]]; then
			MISSING_SMOKE_CHECKS+=("$required_check")
		fi
	done

	if (( ${#MISSING_SMOKE_CHECKS[@]} > 0 )); then
		echo "iOS host smoke failed: PASS marker is missing required functional checks."
		echo "Missing checks: ${MISSING_SMOKE_CHECKS[*]}"
		if [[ -n "$SMOKE_MARKER_LINE" ]]; then
			echo "Marker line: $SMOKE_MARKER_LINE"
		fi
		xcrun simctl terminate "$SIMULATOR_UDID" "$BUNDLE_ID" >/dev/null 2>&1 || true
		if [[ "$SHUTDOWN_SIMULATOR" == "1" ]]; then
			xcrun simctl shutdown "$SIMULATOR_UDID" >/dev/null 2>&1 || true
		fi
		exit 1
	fi

	echo "Validated smoke marker (run id $SMOKE_RUN_ID): $SMOKE_MARKER_LINE"
else
	sleep 3
fi

xcrun simctl terminate "$SIMULATOR_UDID" "$BUNDLE_ID" >/dev/null 2>&1 || true

if [[ "$SHUTDOWN_SIMULATOR" == "1" ]]; then
	xcrun simctl shutdown "$SIMULATOR_UDID" >/dev/null 2>&1 || true
fi

echo "iOS host smoke passed: launched $BUNDLE_ID on simulator $SIMULATOR_UDID"
