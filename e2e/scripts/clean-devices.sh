#!/usr/bin/env bash
set -euo pipefail

# Clears the XCUITest diagnostics that testmanagerd accumulates inside the
# target iOS simulator. Maestro drives iOS through XCTest, so every suite run
# grows data/Containers/Data/InternalDaemon/<testmanagerd>/tmp without bound;
# it reached 172 GB on the certification simulator before the 2026-07-19
# manual cleanup. The clean is threshold-guarded so routine runs pay nothing,
# and tolerant by design: a missing toolchain, device, or container logs a
# skip instead of failing the run.
#
# Usage: e2e/scripts/clean-devices.sh [android] [ios]

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

IOS_DIAGNOSTICS_LIMIT_GB="${E2E_IOS_DIAGNOSTICS_LIMIT_GB:-10}"

clean_android() {
	log "Device clean: Android has no unbounded diagnostics growth identified; nothing to clean."
	return 0
}

resolve_ios_udid() {
	local udid="${E2E_IOS_DEVICE_ID:-booted}"
	if [[ "${udid}" != "booted" ]]; then
		printf '%s\n' "${udid}"
		return 0
	fi
	xcrun simctl list devices booted 2>/dev/null |
		grep -oE '[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}' |
		head -n 1 || true
}

clean_ios() {
	if [[ "${E2E_DEVICE_CLEAN_DIAGNOSTICS:-1}" != "1" ]]; then
		log "Device clean: disabled via E2E_DEVICE_CLEAN_DIAGNOSTICS=0; skipping iOS."
		return 0
	fi
	if ! is_macos || ! command -v xcrun >/dev/null 2>&1; then
		log "Device clean: iOS simulator toolchain unavailable; skipping iOS."
		return 0
	fi

	local udid
	udid="$(resolve_ios_udid)"
	if [[ -z "${udid}" ]]; then
		log "Device clean: no target iOS simulator resolved; skipping iOS."
		return 0
	fi

	local daemon_root="${HOME}/Library/Developer/CoreSimulator/Devices/${udid}/data/Containers/Data/InternalDaemon"
	if [[ ! -d "${daemon_root}" ]]; then
		log "Device clean: no daemon containers for ${udid}; skipping iOS."
		return 0
	fi

	local container identifier tmp_dir size_kb
	local total_kb=0
	local -a tmp_dirs=()
	for container in "${daemon_root}"/*/; do
		[[ -d "${container}" ]] || continue
		identifier="$(/usr/libexec/PlistBuddy -c 'Print :MCMMetadataIdentifier' \
			"${container}.com.apple.mobile_container_manager.metadata.plist" 2>/dev/null || true)"
		[[ "${identifier}" == "com.apple.testmanagerd" ]] || continue
		tmp_dir="${container}tmp"
		[[ -d "${tmp_dir}" ]] || continue
		size_kb="$(du -sk "${tmp_dir}" 2>/dev/null | awk '{ print $1 }')"
		[[ "${size_kb}" =~ ^[0-9]+$ ]] || size_kb=0
		tmp_dirs+=("${tmp_dir}")
		total_kb=$((total_kb + size_kb))
	done

	if [[ "${#tmp_dirs[@]}" -eq 0 ]]; then
		log "Device clean: no testmanagerd container on ${udid}; nothing to clean."
		return 0
	fi

	local limit_kb=$((IOS_DIAGNOSTICS_LIMIT_GB * 1024 * 1024))
	local total_gb=$((total_kb / 1024 / 1024))
	if (( total_kb < limit_kb )); then
		log "Device clean: testmanagerd diagnostics at ${total_gb} GB (< ${IOS_DIAGNOSTICS_LIMIT_GB} GB limit); nothing to clean."
		return 0
	fi

	local was_booted=0
	if xcrun simctl list devices booted 2>/dev/null | grep -q "${udid}"; then
		was_booted=1
	fi

	log "Device clean: testmanagerd diagnostics at ${total_gb} GB (>= ${IOS_DIAGNOSTICS_LIMIT_GB} GB limit); cleaning ${udid}. Disable with E2E_DEVICE_CLEAN_DIAGNOSTICS=0."
	if [[ "${was_booted}" == "1" ]]; then
		xcrun simctl shutdown "${udid}" >/dev/null 2>&1 || true
	fi
	for tmp_dir in "${tmp_dirs[@]}"; do
		find "${tmp_dir}" -mindepth 1 -delete 2>/dev/null || true
	done
	if [[ "${was_booted}" == "1" ]]; then
		if xcrun simctl bootstatus "${udid}" -b >/dev/null 2>&1; then
			log "Device clean: iOS simulator ${udid} booted back after cleaning."
		else
			log "Device clean: iOS simulator ${udid} failed to report boot after cleaning; continuing anyway."
		fi
	fi
	log "Device clean: freed ~${total_gb} GB of XCUITest diagnostics on ${udid}."
	return 0
}

if [[ "$#" -eq 0 ]]; then
	printf 'Usage: %s [android] [ios]\n' "$0" >&2
	exit 1
fi

for platform in "$@"; do
	case "${platform}" in
		android) clean_android ;;
		ios) clean_ios ;;
		*)
			printf 'Unknown platform: %s (expected android or ios)\n' "${platform}" >&2
			exit 1
			;;
	esac
done
