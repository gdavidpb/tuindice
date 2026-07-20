#!/usr/bin/env bash
set -euo pipefail

# Boots the local test devices when evidence needs to run again after
# stop-devices.sh already shut them down (e.g. a harness-only fix changed the
# E2E fingerprint and forced a fresh evidence run post-PR). Tolerant by
# design: a missing toolchain, an already-booted device, or no AVD/simulator
# to pick logs a skip instead of failing the run.
#
# Usage: e2e/scripts/boot-devices.sh [android] [ios]

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

ANDROID_BOOT_TIMEOUT_SECONDS="${E2E_ANDROID_BOOT_TIMEOUT_SECONDS:-240}"

resolve_android_sdk_dir() {
	if [[ -n "${ANDROID_HOME:-}" ]]; then
		printf '%s\n' "${ANDROID_HOME}"
		return 0
	fi
	if [[ -n "${ANDROID_SDK_ROOT:-}" ]]; then
		printf '%s\n' "${ANDROID_SDK_ROOT}"
		return 0
	fi

	local local_properties="${REPO_ROOT}/local.properties"
	local sdk_dir
	if [[ -f "${local_properties}" ]]; then
		sdk_dir="$(awk -F= '/^sdk\.dir=/ { print $2; exit }' "${local_properties}")"
		if [[ -n "${sdk_dir}" ]]; then
			printf '%s\n' "${sdk_dir}"
			return 0
		fi
	fi

	printf '%s/Library/Android/sdk\n' "${HOME}"
}

boot_android() {
	if ! command -v adb >/dev/null 2>&1; then
		log "Device boot: adb unavailable; skipping Android."
		return 0
	fi
	if [[ "$(adb get-state 2>/dev/null || true)" == "device" ]]; then
		log "Device boot: Android emulator already online; skipping."
		return 0
	fi

	local sdk_dir emulator_bin avd_name
	sdk_dir="$(resolve_android_sdk_dir)"
	emulator_bin="${sdk_dir}/emulator/emulator"
	if [[ ! -x "${emulator_bin}" ]]; then
		# `command -v emulator` on PATH usually resolves the legacy
		# tools/emulator launcher, which is broken on Apple Silicon: it looks
		# for a darwin-x86_64 qemu binary that does not exist on arm64 Macs.
		# Always target the modern emulator/emulator binary directly.
		log "Device boot: emulator binary not found at ${emulator_bin}; skipping Android."
		return 0
	fi

	avd_name="${E2E_ANDROID_AVD_NAME:-}"
	if [[ -z "${avd_name}" ]]; then
		avd_name="$("${emulator_bin}" -list-avds 2>/dev/null | head -n 1)"
	fi
	if [[ -z "${avd_name}" ]]; then
		log "Device boot: no Android AVD available; skipping Android."
		return 0
	fi

	log "Device boot: booting Android AVD ${avd_name}."
	nohup "${emulator_bin}" -avd "${avd_name}" -no-snapshot-save \
		>"${E2E_TMP_DIR}-emulator-boot.log" 2>&1 &
	disown

	local deadline=$(($(date +%s) + ANDROID_BOOT_TIMEOUT_SECONDS))
	while (( $(date +%s) < deadline )); do
		if [[ "$(adb get-state 2>/dev/null || true)" == "device" ]] &&
			[[ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" == "1" ]]; then
			log "Device boot: Android booted."
			return 0
		fi
		sleep 2
	done

	log "Device boot: Android did not report boot_completed within ${ANDROID_BOOT_TIMEOUT_SECONDS}s; continuing anyway."
	return 0
}

boot_ios() {
	if ! is_macos || ! command -v xcrun >/dev/null 2>&1; then
		log "Device boot: iOS simulator toolchain unavailable; skipping iOS."
		return 0
	fi

	local booted_udid
	booted_udid="$(xcrun simctl list devices booted 2>/dev/null | grep -oE '[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}' | head -n 1 || true)"
	if [[ -n "${booted_udid}" ]]; then
		log "Device boot: iOS simulator already booted (${booted_udid}); skipping."
		return 0
	fi

	local udid="${E2E_IOS_DEVICE_ID:-}"
	if [[ -z "${udid}" || "${udid}" == "booted" ]]; then
		udid="$(xcrun simctl list devices available 2>/dev/null | awk '
			/^-- iOS/ { in_ios_runtime = 1; next }
			/^-- / { in_ios_runtime = 0; next }
			in_ios_runtime && /iPhone/ { print; exit }
		' | grep -oE '[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}')"
	fi
	if [[ -z "${udid}" ]]; then
		log "Device boot: no available iOS simulator found; skipping iOS."
		return 0
	fi

	log "Device boot: booting iOS simulator ${udid}."
	xcrun simctl boot "${udid}" >/dev/null 2>&1 || true
	if xcrun simctl bootstatus "${udid}" -b >/dev/null 2>&1; then
		log "Device boot: iOS simulator ${udid} booted."
	else
		log "Device boot: iOS simulator ${udid} failed to report boot; continuing anyway."
	fi
	open -a Simulator >/dev/null 2>&1 || true
	return 0
}

if [[ "$#" -eq 0 ]]; then
	printf 'Usage: %s [android] [ios]\n' "$0" >&2
	exit 1
fi

for platform in "$@"; do
	case "${platform}" in
		android) boot_android ;;
		ios) boot_ios ;;
		*)
			printf 'Unknown platform: %s (expected android or ios)\n' "${platform}" >&2
			exit 1
			;;
	esac
done
