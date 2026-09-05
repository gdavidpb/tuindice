#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
REPO_ROOT="$(cd "${ROOT_DIR}/.." && pwd)"
PROJECT_PATH="$ROOT_DIR/TuIndiceHost.xcodeproj"
WORKSPACE_PATH="$ROOT_DIR/TuIndiceHost.xcworkspace"
SCHEME_NAME="TuIndiceHost"
CONFIGURATION_NAME="${CONFIGURATION:-Debug}"
DERIVED_DATA_PATH="${DERIVED_DATA_PATH:-}"
IOS_PLATFORM="${IOS_PLATFORM:-simulator}"
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

if [[ "$IOS_PLATFORM" == "simulator" ]]; then
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
fi

bash "$REPO_ROOT/.github/scripts/sync-app-version.sh"

if [[ -f "$ROOT_DIR/Podfile" ]] && command -v pod >/dev/null 2>&1; then
	if [[ ! -d "$WORKSPACE_PATH" || "${FORCE_POD_INSTALL:-0}" == "1" ]]; then
		# CocoaPods normalizes paths as UTF-8 and dies with Encoding::CompatibilityError when the
		# locale is unset — which is what a Gradle daemon passes down when it was started from a shell
		# without one. Pinning it here keeps the build independent of who launched the daemon.
		(cd "$ROOT_DIR" && LANG="${LANG:-en_US.UTF-8}" LC_ALL="${LC_ALL:-en_US.UTF-8}" pod install --silent)
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
)

if [[ "$IOS_PLATFORM" == "simulator" ]]; then
	xcodebuild_args+=(
		-sdk iphonesimulator
		-destination "generic/platform=iOS Simulator"
	)
else
	xcodebuild_args+=(
		-sdk iphoneos
		-destination "generic/platform=iOS"
	)
fi

if [[ -n "$DERIVED_DATA_PATH" ]]; then
	xcodebuild_args+=(
		-derivedDataPath "$DERIVED_DATA_PATH"
	)
fi

append_optional_build_setting() {
	local setting_name="$1"
	local setting_value="${!setting_name:-}"

	if [[ -n "$setting_value" ]]; then
		xcodebuild_args+=("$setting_name=$setting_value")
	fi
}

append_optional_build_setting TUINDICE_API_BASE_URL
append_optional_build_setting TUINDICE_PRIVACY_POLICY_URL
append_optional_build_setting TUINDICE_TERMS_AND_CONDITIONS_URL
append_optional_build_setting TUINDICE_SUPPORT_URL
append_optional_build_setting TUINDICE_APP_STORE_URL
append_optional_build_setting SKIP_FRAMEWORK_BUILD

if [[ "$IOS_PLATFORM" == "simulator" ]]; then
	DEFAULT_CODE_SIGNING_ALLOWED="NO"
else
	DEFAULT_CODE_SIGNING_ALLOWED="YES"
fi
CODE_SIGNING_ALLOWED_VALUE="${CODE_SIGNING_ALLOWED:-$DEFAULT_CODE_SIGNING_ALLOWED}"

xcodebuild_args+=(
	"CODE_SIGNING_ALLOWED=$CODE_SIGNING_ALLOWED_VALUE"
	build
)

xcodebuild "${xcodebuild_args[@]}"
