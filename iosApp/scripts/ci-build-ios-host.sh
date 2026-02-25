#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
PROJECT_PATH="$ROOT_DIR/TuIndiceHost.xcodeproj"
WORKSPACE_PATH="$ROOT_DIR/TuIndiceHost.xcworkspace"
SCHEME_NAME="TuIndiceHost"
CONFIGURATION_NAME="${CONFIGURATION:-Debug}"
DERIVED_DATA_PATH="${DERIVED_DATA_PATH:-}"
REQUIRE_SIMULATOR="${REQUIRE_SIMULATOR:-0}"

skip_or_fail() {
	local message="$1"

	if [[ "$REQUIRE_SIMULATOR" == "1" ]]; then
		echo "iOS host build failed: $message"
		exit 1
	fi

	echo "Skipping iOS host build: $message"
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

if [[ -f "$ROOT_DIR/Podfile" ]] && command -v pod >/dev/null 2>&1; then
	if [[ ! -d "$WORKSPACE_PATH" || "${FORCE_POD_INSTALL:-0}" == "1" ]]; then
		(cd "$ROOT_DIR" && pod install --silent)
	fi
fi

declare -a xcodebuild_args

if [[ -d "$WORKSPACE_PATH" ]]; then
	xcodebuild_args=(
		-workspace "$WORKSPACE_PATH"
	)
else
	xcodebuild_args=(
		-project "$PROJECT_PATH"
	)
fi

xcodebuild_args+=(
	-scheme "$SCHEME_NAME"
	-configuration "$CONFIGURATION_NAME"
	-sdk iphonesimulator
	-destination "generic/platform=iOS Simulator"
)

if [[ -n "$DERIVED_DATA_PATH" ]]; then
	xcodebuild_args+=(
		-derivedDataPath "$DERIVED_DATA_PATH"
	)
fi

xcodebuild_args+=(
	CODE_SIGNING_ALLOWED=NO
	build
)

xcodebuild "${xcodebuild_args[@]}"
