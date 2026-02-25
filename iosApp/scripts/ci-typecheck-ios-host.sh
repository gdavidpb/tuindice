#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
FRAMEWORK_DIR="$ROOT_DIR/maincore/build/bin/iosSimulatorArm64/debugFramework"
HOST_SOURCES_DIR="$ROOT_DIR/iosApp/Sources/TuIndiceHost"

if [[ "$OSTYPE" != darwin* ]]; then
	echo "Skipping iOS host typecheck: non-macOS host."
	exit 0
fi

if ! command -v xcrun >/dev/null 2>&1; then
	echo "Skipping iOS host typecheck: xcrun is not available."
	exit 0
fi

if [[ "${SKIP_FRAMEWORK_BUILD:-0}" != "1" ]]; then
	(cd "$ROOT_DIR" && ./gradlew :maincore:linkDebugFrameworkIosSimulatorArm64 -q)
fi

shopt -s nullglob
swift_sources=("$HOST_SOURCES_DIR"/*.swift)
shopt -u nullglob

if [[ "${#swift_sources[@]}" -eq 0 ]]; then
	echo "Skipping iOS host typecheck: no Swift sources found."
	exit 0
fi

xcrun --sdk iphonesimulator swiftc \
	-target arm64-apple-ios16.0-simulator \
	-F "$FRAMEWORK_DIR" \
	-framework maincore \
	-typecheck \
	"${swift_sources[@]}"
