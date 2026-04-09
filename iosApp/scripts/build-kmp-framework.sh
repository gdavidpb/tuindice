#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
CONFIGURATION_NAME="${CONFIGURATION:-Debug}"
PLATFORM="${PLATFORM_NAME:-iphonesimulator}"
ARCHS_VALUE="${ARCHS:-arm64}"
CI_MODE="${CI:-}"

if [[ -n "${GRADLE_USER_HOME:-}" ]]; then
	GRADLE_USER_HOME_DIR="$GRADLE_USER_HOME"
elif [[ -d "${HOME:-}/.gradle" && -w "${HOME:-}/.gradle" ]]; then
	GRADLE_USER_HOME_DIR="${HOME}/.gradle"
else
	GRADLE_USER_HOME_DIR="$ROOT_DIR/.gradle-local"
fi

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
mkdir -p "$GRADLE_USER_HOME_DIR"
export GRADLE_USER_HOME="$GRADLE_USER_HOME_DIR"
echo "Building maincore framework and syncing Compose resources for iOS"
declare -a gradle_args=(
	"-Dorg.gradle.jvmargs=$GRADLE_JVM_ARGS"
)

for target_suffix in "${target_suffixes[@]}"; do
	gradle_args+=(":maincore:link${BUILD_TYPE}Framework${target_suffix}")
done

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

if [[ -n "${SCRIPT_OUTPUT_FILE_0:-}" ]]; then
	mkdir -p "$(dirname "$SCRIPT_OUTPUT_FILE_0")"
	echo "maincore framework linked: ${BUILD_TYPE} ${target_suffixes[*]}" > "$SCRIPT_OUTPUT_FILE_0"
fi
