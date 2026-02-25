#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
CONFIGURATION_NAME="${CONFIGURATION:-Release}"
DERIVED_DATA_PATH="${DERIVED_DATA_PATH:-$ROOT_DIR/.build/ios-host-device-e2e}"
IOS_DEVICE_IDENTIFIER="${IOS_DEVICE_IDENTIFIER:-}"
SKIP_HOST_BUILD="${SKIP_HOST_BUILD:-0}"
REQUIRE_DEVICE="${REQUIRE_DEVICE:-0}"
E2E_LAUNCH_TIMEOUT_SECONDS="${E2E_LAUNCH_TIMEOUT_SECONDS:-45}"
SMOKE_RUN_ID="${SMOKE_RUN_ID:-$(date +%s)}"
SMOKE_MARKER_PREFIX="TUINDICE_SMOKE_MARKER"
SMOKE_REQUIRED_CHECKS="${SMOKE_REQUIRED_CHECKS:-smoke-runtime:disabled-on-device}"
HOST_APP_NAME="TuIndiceHost.app"

skip_or_fail() {
	local message="$1"

	if [[ "$REQUIRE_DEVICE" == "1" ]]; then
		echo "iOS host device E2E failed: $message"
		exit 1
	fi

	echo "Skipping iOS host device E2E: $message"
	exit 0
}

if [[ "$OSTYPE" != darwin* ]]; then
	skip_or_fail "non-macOS host."
fi

if ! command -v xcrun >/dev/null 2>&1; then
	skip_or_fail "xcrun is not available."
fi

if ! xcrun devicectl --help >/dev/null 2>&1; then
	skip_or_fail "devicectl is not available."
fi

CORE_DEVICE_OUTPUT="$(
	xcrun devicectl list devices 2>&1 || true
)"
if [[ "$CORE_DEVICE_OUTPUT" == *"Timed out waiting for CoreDeviceService"* ]]; then
	skip_or_fail "CoreDeviceService is unavailable."
fi

if [[ -z "$IOS_DEVICE_IDENTIFIER" ]]; then
	skip_or_fail "IOS_DEVICE_IDENTIFIER is empty."
fi

if [[ "$SKIP_HOST_BUILD" != "1" ]]; then
	CONFIGURATION="$CONFIGURATION_NAME" \
	DERIVED_DATA_PATH="$DERIVED_DATA_PATH" \
	IOS_PLATFORM="device" \
	CODE_SIGNING_ALLOWED="${CODE_SIGNING_ALLOWED:-YES}" \
	REQUIRE_SIMULATOR="0" \
	"$ROOT_DIR/scripts/ci-build-ios-host.sh"
fi

APP_BUNDLE_PATH="$DERIVED_DATA_PATH/Build/Products/${CONFIGURATION_NAME}-iphoneos/$HOST_APP_NAME"

if [[ ! -d "$APP_BUNDLE_PATH" ]]; then
	echo "iOS host device E2E failed: app bundle not found at $APP_BUNDLE_PATH"
	exit 1
fi

BUNDLE_ID="$(
	/usr/libexec/PlistBuddy \
		-c "Print :CFBundleIdentifier" \
		"$APP_BUNDLE_PATH/Info.plist"
)"

if [[ -z "$BUNDLE_ID" ]]; then
	echo "iOS host device E2E failed: bundle id is empty."
	exit 1
fi

xcrun devicectl device uninstall app --device "$IOS_DEVICE_IDENTIFIER" "$BUNDLE_ID" >/dev/null 2>&1 || true
xcrun devicectl device install app --device "$IOS_DEVICE_IDENTIFIER" "$APP_BUNDLE_PATH" >/dev/null

if [[ -n "${SMOKE_USBID:-}" || -n "${SMOKE_PASSWORD:-}" || -n "${TUINDICE_SMOKE_USBID:-}" || -n "${TUINDICE_SMOKE_PASSWORD:-}" ]]; then
	echo "iOS host device E2E failed: authenticated smoke is unavailable on iosArm64 runtime (smoke disabled on device). Use simulator smoke lane for authenticated checks."
	exit 1
fi

LAUNCH_ENV_JSON="{\"TUINDICE_IOS_SMOKE_VALIDATE\":\"1\",\"TUINDICE_IOS_SMOKE_RUN_ID\":\"$SMOKE_RUN_ID\"}"

set +e
LAUNCH_OUTPUT="$(
	xcrun devicectl \
		--timeout "$E2E_LAUNCH_TIMEOUT_SECONDS" \
		device process launch \
		--device "$IOS_DEVICE_IDENTIFIER" \
		--terminate-existing \
		--environment-variables "$LAUNCH_ENV_JSON" \
		--console \
		"$BUNDLE_ID" 2>&1
)"
LAUNCH_EXIT_CODE=$?
set -e

echo "$LAUNCH_OUTPUT"

SMOKE_FAIL_MARKER="$SMOKE_MARKER_PREFIX:$SMOKE_RUN_ID:FAIL:"
SMOKE_PASS_MARKER="$SMOKE_MARKER_PREFIX:$SMOKE_RUN_ID:PASS:"

if [[ "$LAUNCH_OUTPUT" == *"$SMOKE_FAIL_MARKER"* ]]; then
	SMOKE_FAIL_LINE="$(
		echo "$LAUNCH_OUTPUT" | \
			grep "$SMOKE_FAIL_MARKER" | \
			tail -n 1 || true
	)"
	echo "iOS host device E2E failed: smoke marker reported FAIL."
	[[ -n "$SMOKE_FAIL_LINE" ]] && echo "Marker line: $SMOKE_FAIL_LINE"
	exit 1
fi

SMOKE_PASS_LINE="$(
	echo "$LAUNCH_OUTPUT" | \
		grep "$SMOKE_PASS_MARKER" | \
		tail -n 1 || true
)"

if [[ -z "$SMOKE_PASS_LINE" ]]; then
	echo "iOS host device E2E failed: missing functional smoke PASS marker for run id '$SMOKE_RUN_ID'."
	echo "Launch exit code: $LAUNCH_EXIT_CODE"
	exit 1
fi

MISSING_SMOKE_CHECKS=()
IFS=',' read -r -a REQUIRED_SMOKE_CHECK_ITEMS <<< "$SMOKE_REQUIRED_CHECKS"
for required_check in "${REQUIRED_SMOKE_CHECK_ITEMS[@]}"; do
	required_check="$(echo "$required_check" | xargs)"

	if [[ -z "$required_check" ]]; then
		continue
	fi

	if [[ "$SMOKE_PASS_LINE" != *"$required_check"* ]]; then
		MISSING_SMOKE_CHECKS+=("$required_check")
	fi
done

if (( ${#MISSING_SMOKE_CHECKS[@]} > 0 )); then
	echo "iOS host device E2E failed: PASS marker is missing required checks."
	echo "Missing checks: ${MISSING_SMOKE_CHECKS[*]}"
	echo "Marker line: $SMOKE_PASS_LINE"
	exit 1
fi

if [[ $LAUNCH_EXIT_CODE -ne 0 ]]; then
	echo "iOS host device E2E warning: launch command exited non-zero ($LAUNCH_EXIT_CODE) after PASS marker."
fi

echo "iOS host device E2E passed: launched $BUNDLE_ID on device $IOS_DEVICE_IDENTIFIER"
