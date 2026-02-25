#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
CONFIGURATION_NAME="${CONFIGURATION:-Debug}"
PLATFORM="${PLATFORM_NAME:-iphonesimulator}"
ARCHS_VALUE="${ARCHS:-arm64}"

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

TARGET_SUFFIX="IosSimulatorArm64"
if [[ "$PLATFORM" == "iphoneos" ]]; then
	TARGET_SUFFIX="IosArm64"
elif [[ "$ARCHS_VALUE" == *"x86_64"* ]]; then
	TARGET_SUFFIX="IosX64"
fi

cd "$ROOT_DIR"
mkdir -p "$GRADLE_USER_HOME_DIR"
export GRADLE_USER_HOME="$GRADLE_USER_HOME_DIR"
echo "Building maincore framework and syncing Compose resources for iOS"
./gradlew \
	":maincore:link${BUILD_TYPE}Framework${TARGET_SUFFIX}" \
	":maincore:syncComposeResourcesForIos" \
	--stacktrace \
	--no-daemon \
	-Dorg.gradle.jvmargs="-Xmx4g -XX:MaxMetaspaceSize=1536m -Dfile.encoding=UTF-8" \
	-Pkotlin.native.cacheKind=none

if [[ -n "${SCRIPT_OUTPUT_FILE_0:-}" ]]; then
	mkdir -p "$(dirname "$SCRIPT_OUTPUT_FILE_0")"
	echo "maincore framework linked: ${BUILD_TYPE} ${TARGET_SUFFIX}" > "$SCRIPT_OUTPUT_FILE_0"
fi
