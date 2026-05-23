#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

if [[ "${E2E_SKIP_ANDROID:-0}" != "1" ]]; then
	bash "${SCRIPT_DIR}/run-maestro-evidence.sh" android
else
	log "Skipping Android Maestro evidence because E2E_SKIP_ANDROID=1."
fi

if is_macos && command -v xcrun >/dev/null 2>&1; then
	bash "${SCRIPT_DIR}/run-maestro-evidence.sh" ios
elif [[ "${E2E_STRICT_IOS:-0}" == "1" ]]; then
	printf 'iOS Maestro evidence is required but unavailable in this environment.\n' >&2
	exit 1
else
	log "Skipping iOS Maestro evidence because the local iOS simulator toolchain is unavailable."
fi
