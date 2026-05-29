#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
CONFIGURATION_NAME="${CONFIGURATION:-Debug}"
PLATFORM="${PLATFORM_NAME:-iphonesimulator}"
ARCHS_VALUE="${ARCHS:-arm64}"
CI_MODE="${CI:-}"
LOCAL_GRADLE_USER_HOME_DIR="$ROOT_DIR/.gradle-local"
DEFAULT_GRADLE_USER_HOME_DIR="${HOME:-}/.gradle"
GRADLE_DIST_NAME="$(
	sed -n 's/^distributionUrl=.*\/\(gradle-[^\/]*\)\.zip$/\1/p' \
		"$ROOT_DIR/gradle/wrapper/gradle-wrapper.properties" | head -n 1
)"

if [[ -n "${GRADLE_USER_HOME:-}" ]]; then
	GRADLE_USER_HOME_DIR="$GRADLE_USER_HOME"
else
	GRADLE_USER_HOME_DIR="$LOCAL_GRADLE_USER_HOME_DIR"
fi

prime_local_gradle_wrapper_dist() {
	[[ "$GRADLE_USER_HOME_DIR" == "$LOCAL_GRADLE_USER_HOME_DIR" ]] || return 0
	[[ -n "$GRADLE_DIST_NAME" ]] || return 0

	local source_dist_dir="$DEFAULT_GRADLE_USER_HOME_DIR/wrapper/dists/$GRADLE_DIST_NAME"
	local target_dist_dir="$GRADLE_USER_HOME_DIR/wrapper/dists/$GRADLE_DIST_NAME"
	local target_ok_marker="$target_dist_dir"/*/"$GRADLE_DIST_NAME.zip.ok"
	local target_unpacked_dir="$target_dist_dir"/*/gradle-*

	[[ -d "$source_dist_dir" ]] || return 0
	if compgen -G "$target_ok_marker" >/dev/null &&
		compgen -G "$target_unpacked_dir" >/dev/null; then
		return 0
	fi

	mkdir -p "$target_dist_dir"
	cp -R "$source_dist_dir"/. "$target_dist_dir"/
}

# Xcode script phases may run without JAVA_HOME; Gradle/Kotlin toolchain needs it.
if [[ -z "${JAVA_HOME:-}" ]]; then
	if JAVA_21_HOME="$(/usr/libexec/java_home -v 21 2>/dev/null)"; then
		export JAVA_HOME="$JAVA_21_HOME"
	elif JAVA_DEFAULT_HOME="$(/usr/libexec/java_home 2>/dev/null)"; then
		export JAVA_HOME="$JAVA_DEFAULT_HOME"
	fi
fi

BUILD_TYPE="Debug"
if [[ "$CONFIGURATION_NAME" == "Release" ]]; then
	BUILD_TYPE="Release"
fi

DEFAULT_GRADLE_JVM_ARGS="-Xmx4g -XX:MaxMetaspaceSize=1536m -Dfile.encoding=UTF-8"
if [[ "$BUILD_TYPE" == "Release" ]]; then
	DEFAULT_GRADLE_JVM_ARGS="-Xmx6g -XX:MaxMetaspaceSize=1536m -Dfile.encoding=UTF-8"
fi
GRADLE_JVM_ARGS="${TUINDICE_IOS_GRADLE_JVM_ARGS:-$DEFAULT_GRADLE_JVM_ARGS}"

declare -a target_suffixes=()
if [[ "$PLATFORM" == "iphoneos" ]]; then
	target_suffixes=("IosArm64")
else
	if [[ "$ARCHS_VALUE" == *"arm64"* ]]; then
		target_suffixes+=("IosSimulatorArm64")
	fi
	if [[ "$ARCHS_VALUE" == *"x86_64"* ]]; then
		target_suffixes+=("IosX64")
	fi
	if [[ ${#target_suffixes[@]} -eq 0 ]]; then
		target_suffixes=("IosSimulatorArm64")
	fi
fi

cd "$ROOT_DIR"

bash "$ROOT_DIR/.github/scripts/sync-app-version.sh"

write_script_output_stamp() {
	local stamp="$1"

	if [[ -n "${SCRIPT_OUTPUT_FILE_0:-}" ]]; then
		mkdir -p "$(dirname "$SCRIPT_OUTPUT_FILE_0")"
		echo "$stamp" > "$SCRIPT_OUTPUT_FILE_0"
	fi
}

if [[ "${SKIP_FRAMEWORK_BUILD:-0}" == "1" ]]; then
	echo "Skipping maincore framework build; Gradle task already linked it."
	write_script_output_stamp "maincore framework build skipped: ${BUILD_TYPE} ${target_suffixes[*]}"
	exit 0
fi

BUILD_TYPE_DIR="$(printf '%s' "$BUILD_TYPE" | tr '[:upper:]' '[:lower:]')"

target_name_for_suffix() {
	local target_suffix="$1"

	case "$target_suffix" in
		IosArm64)
			printf '%s\n' "iosArm64"
			;;
		IosSimulatorArm64)
			printf '%s\n' "iosSimulatorArm64"
			;;
		IosX64)
			printf '%s\n' "iosX64"
			;;
		*)
			echo "Unknown iOS target suffix: $target_suffix" >&2
			return 1
			;;
	esac
}

framework_binary_for_target_suffix() {
	local target_suffix="$1"
	local target_name

	target_name="$(target_name_for_suffix "$target_suffix")"
	printf '%s/maincore/build/bin/%s/%sFramework/maincore.framework/maincore\n' \
		"$ROOT_DIR" "$target_name" "$BUILD_TYPE_DIR"
}

cached_frameworks_available() {
	local target_suffix
	local framework_binary
	local missing=0

	for target_suffix in "${target_suffixes[@]}"; do
		framework_binary="$(framework_binary_for_target_suffix "$target_suffix")"
		if [[ ! -f "$framework_binary" ]]; then
			echo "Cached maincore framework is missing: $framework_binary" >&2
			missing=1
		fi
	done

	[[ "$missing" -eq 0 ]]
}

mkdir -p "$GRADLE_USER_HOME_DIR"
prime_local_gradle_wrapper_dist
export GRADLE_USER_HOME="$GRADLE_USER_HOME_DIR"
declare -a gradle_args=(
	"-Dorg.gradle.jvmargs=$GRADLE_JVM_ARGS"
	"--no-configuration-cache"
)
used_cached_frameworks=0

if [[ "${TUINDICE_IOS_USE_CACHED_FRAMEWORK:-0}" == "1" ]] && cached_frameworks_available; then
	echo "Using cached maincore framework and syncing Compose resources for iOS"
	used_cached_frameworks=1
else
	echo "Building maincore framework and syncing Compose resources for iOS"
	for target_suffix in "${target_suffixes[@]}"; do
		gradle_args+=(":maincore:link${BUILD_TYPE}Framework${target_suffix}")
	done
fi

gradle_args+=(":maincore:syncComposeResourcesForIos")

if [[ -n "$CI_MODE" || "${TUINDICE_IOS_GRADLE_STACKTRACE:-0}" == "1" ]]; then
	gradle_args+=("--stacktrace")
fi

if [[ -n "$CI_MODE" || "${TUINDICE_IOS_GRADLE_NO_DAEMON:-0}" == "1" ]]; then
	gradle_args+=("--no-daemon")
fi

NATIVE_CACHE_KIND="${TUINDICE_IOS_NATIVE_CACHE_KIND:-}"
if [[ -z "$NATIVE_CACHE_KIND" && -n "$CI_MODE" ]]; then
	NATIVE_CACHE_KIND="none"
fi

if [[ -n "$NATIVE_CACHE_KIND" ]]; then
	gradle_args+=("-Pkotlin.native.cacheKind=$NATIVE_CACHE_KIND")
fi

./gradlew "${gradle_args[@]}"

if [[ "$used_cached_frameworks" == "1" ]]; then
	write_script_output_stamp "maincore framework cache used: ${BUILD_TYPE} ${target_suffixes[*]}"
else
	write_script_output_stamp "maincore framework linked: ${BUILD_TYPE} ${target_suffixes[*]}"
fi
