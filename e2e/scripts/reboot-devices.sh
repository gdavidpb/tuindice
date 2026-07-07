#!/usr/bin/env bash
set -euo pipefail

# Cold-reboots the local test devices before long evidence runs. Endurance
# flakiness after hours of continuous emulator/simulator load is real and was
# measured during certification; a fresh boot before spending 60-90 minutes of
# evidence materially cuts it. Tolerant by design: a missing device logs a
# skip instead of failing the run.
#
# Usage: e2e/scripts/reboot-devices.sh [android] [ios]

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

ANDROID_BOOT_TIMEOUT_SECONDS="${E2E_ANDROID_BOOT_TIMEOUT_SECONDS:-240}"

reboot_android() {
	if ! command -v adb >/dev/null 2>&1; then
		log "Device reboot: adb unavailable; skipping Android."
		return 0
	fi
	if [[ "$(adb get-state 2>/dev/null || true)" != "device" ]]; then
		log "Device reboot: no Android device online; skipping Android."
		return 0
	fi

	log "Device reboot: rebooting Android device."
	adb reboot || {
		log "Device reboot: adb reboot failed; skipping Android."
		return 0
	}
	adb wait-for-device >/dev/null 2>&1 || true

	local deadline=$(($(date +%s) + ANDROID_BOOT_TIMEOUT_SECONDS))
	while (( $(date +%s) < deadline )); do
		if [[ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" == "1" ]]; then
			log "Device reboot: Android booted."
			return 0
		fi
		sleep 2
	done

	log "Device reboot: Android did not report boot_completed within ${ANDROID_BOOT_TIMEOUT_SECONDS}s; continuing anyway."
	return 0
}

reboot_ios() {
	if ! is_macos || ! command -v xcrun >/dev/null 2>&1; then
		log "Device reboot: iOS simulator toolchain unavailable; skipping iOS."
		return 0
	fi

	local udids
	udids="$(xcrun simctl list devices booted 2>/dev/null | grep -oE '[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}' || true)"
	if [[ -z "${udids}" ]]; then
		log "Device reboot: no booted iOS simulator; skipping iOS."
		return 0
	fi

	local udid
	while IFS= read -r udid; do
		[[ -n "${udid}" ]] || continue
		log "Device reboot: rebooting iOS simulator ${udid}."
		xcrun simctl shutdown "${udid}" >/dev/null 2>&1 || true
		if xcrun simctl bootstatus "${udid}" -b >/dev/null 2>&1; then
			log "Device reboot: iOS simulator ${udid} booted."
		else
			log "Device reboot: iOS simulator ${udid} failed to report boot; continuing anyway."
		fi
	done <<<"${udids}"
	return 0
}

if [[ "$#" -eq 0 ]]; then
	printf 'Usage: %s [android] [ios]\n' "$0" >&2
	exit 1
fi

for platform in "$@"; do
	case "${platform}" in
		android) reboot_android ;;
		ios) reboot_ios ;;
		*)
			printf 'Unknown platform: %s (expected android or ios)\n' "${platform}" >&2
			exit 1
			;;
	esac
done
