#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

if ! is_macos; then
	printf 'iOS E2E reset requires macOS.\n' >&2
	exit 1
fi

require_command xcrun

log "Resetting iOS bundle ${E2E_IOS_BUNDLE_ID} on ${E2E_IOS_DEVICE_ID}."
disable_ios_keyboard_helpers
xcrun simctl terminate "${E2E_IOS_DEVICE_ID}" "${E2E_IOS_BUNDLE_ID}" >/dev/null 2>&1 || true
xcrun simctl uninstall "${E2E_IOS_DEVICE_ID}" "${E2E_IOS_BUNDLE_ID}" >/dev/null 2>&1 || true
