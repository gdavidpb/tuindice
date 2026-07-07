#!/usr/bin/env bash
set -euo pipefail

# Stops the local test devices once certification wraps up: evidence runs
# leave an Android emulator and an iOS simulator burning CPU/battery after
# the PR is created. Tolerant by design: a missing toolchain or an already
# stopped device logs a skip instead of failing the wrap-up.
#
# Usage: e2e/scripts/stop-devices.sh [android] [ios]

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

stop_android() {
	if ! command -v adb >/dev/null 2>&1; then
		log "Device stop: adb unavailable; skipping Android."
		return 0
	fi

	local serials
	serials="$(adb devices 2>/dev/null | awk 'NR > 1 && $1 ~ /^emulator-/ && $2 == "device" { print $1 }' || true)"
	if [[ -z "${serials}" ]]; then
		log "Device stop: no Android emulator online; skipping Android."
		return 0
	fi

	local serial
	while IFS= read -r serial; do
		[[ -n "${serial}" ]] || continue
		log "Device stop: stopping Android emulator ${serial}."
		adb -s "${serial}" emu kill >/dev/null 2>&1 ||
			log "Device stop: failed to stop ${serial}; continuing anyway."
	done <<<"${serials}"
	return 0
}

stop_ios() {
	if ! is_macos || ! command -v xcrun >/dev/null 2>&1; then
		log "Device stop: iOS simulator toolchain unavailable; skipping iOS."
		return 0
	fi

	local udids
	udids="$(xcrun simctl list devices booted 2>/dev/null | grep -oE '[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}' || true)"
	if [[ -z "${udids}" ]]; then
		log "Device stop: no booted iOS simulator; skipping iOS."
		return 0
	fi

	local udid
	while IFS= read -r udid; do
		[[ -n "${udid}" ]] || continue
		log "Device stop: shutting down iOS simulator ${udid}."
		xcrun simctl shutdown "${udid}" >/dev/null 2>&1 ||
			log "Device stop: failed to shut down ${udid}; continuing anyway."
	done <<<"${udids}"
	return 0
}

if [[ "$#" -eq 0 ]]; then
	printf 'Usage: %s [android] [ios]\n' "$0" >&2
	exit 1
fi

for platform in "$@"; do
	case "${platform}" in
		android) stop_android ;;
		ios) stop_ios ;;
		*)
			printf 'Unknown platform: %s (expected android or ios)\n' "${platform}" >&2
			exit 1
			;;
	esac
done
