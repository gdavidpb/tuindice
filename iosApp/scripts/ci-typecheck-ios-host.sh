#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
FRAMEWORK_DIR="$ROOT_DIR/maincore/build/bin/iosSimulatorArm64/debugFramework"
HOST_SOURCES_DIR="$ROOT_DIR/iosApp/Sources/TuIndiceHost"
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
	[[ "$GRADLE_USER_HOME_DIR" == "$LOCAL_GRADLE_USER_HOME_DIR" ]] || return
	[[ -n "$GRADLE_DIST_NAME" ]] || return

	local source_dist_dir="$DEFAULT_GRADLE_USER_HOME_DIR/wrapper/dists/$GRADLE_DIST_NAME"
	local target_dist_dir="$GRADLE_USER_HOME_DIR/wrapper/dists/$GRADLE_DIST_NAME"
	local target_ok_marker="$target_dist_dir"/*/"$GRADLE_DIST_NAME.zip.ok"
	local target_unpacked_dir="$target_dist_dir"/*/gradle-*

	[[ -d "$source_dist_dir" ]] || return
	compgen -G "$target_ok_marker" >/dev/null && compgen -G "$target_unpacked_dir" >/dev/null && return

	mkdir -p "$target_dist_dir"
	cp -R "$source_dist_dir"/. "$target_dist_dir"/
}

if [[ "$OSTYPE" != darwin* ]]; then
	echo "Skipping iOS host typecheck: non-macOS host."
	exit 0
fi

if ! command -v xcrun >/dev/null 2>&1; then
	echo "Skipping iOS host typecheck: xcrun is not available."
	exit 0
fi

if [[ "${SKIP_FRAMEWORK_BUILD:-0}" != "1" ]]; then
	mkdir -p "$GRADLE_USER_HOME_DIR"
	prime_local_gradle_wrapper_dist
	(
		cd "$ROOT_DIR" && \
			GRADLE_USER_HOME="$GRADLE_USER_HOME_DIR" \
			./gradlew --no-configuration-cache :maincore:linkDebugFrameworkIosSimulatorArm64 -q
	)
fi

shopt -s nullglob
swift_sources=("$HOST_SOURCES_DIR"/*.swift)
shopt -u nullglob

if [[ "${#swift_sources[@]}" -eq 0 ]]; then
	echo "Skipping iOS host typecheck: no Swift sources found."
	exit 0
fi

xcrun --sdk iphonesimulator swiftc \
	-target arm64-apple-ios18.5-simulator \
	-F "$FRAMEWORK_DIR" \
	-framework maincore \
	-typecheck \
	"${swift_sources[@]}"
