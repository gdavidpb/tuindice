#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

if ! is_macos; then
	printf 'iOS E2E build requires macOS.\n' >&2
	exit 1
fi

require_command xcrun

log "Linking shared iOS simulator framework."
"${REPO_ROOT}/gradlew" --console=plain :maincore:linkDebugFrameworkIosSimulatorArm64

log "Building iOS debug host."
CONFIGURATION="Debug" \
DERIVED_DATA_PATH="${E2E_IOS_DERIVED_DATA}" \
REQUIRE_SIMULATOR="1" \
	"${REPO_ROOT}/iosApp/scripts/ci-build-ios-host.sh"

printf '%s\n' "${E2E_IOS_DERIVED_DATA}/Build/Products/Debug-iphonesimulator/TuIndiceHost.app"
