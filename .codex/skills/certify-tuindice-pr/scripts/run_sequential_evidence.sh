#!/usr/bin/env bash
set -euo pipefail

# Diagnosed fallback for `e2eMaestroEvidenceLocal`: runs Android and then iOS one
# at a time, each with the other platform's device fully OFF. Use it only for the
# contention signature described in the runbook's "Running Evidence" (different
# flows failing on each parallel attempt, garbled typed credentials in the
# WireMock log, load average far above the core count) and say so to the user.
#
# If the Android `adb reverse` tunnel is dead (probe fails), export before calling:
#   E2E_ANDROID_TUNNEL=off
# and the run points the app at the emulator host alias 10.0.2.2 instead.

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../../.." && pwd)"
cd "${REPO_ROOT}"

android_port="${E2E_SEQ_ANDROID_PORT:-18626}"
ios_port="${E2E_SEQ_IOS_PORT:-18627}"

wait_for_device_off() {
	local pattern="$1" attempt
	for attempt in $(seq 1 30); do
		pgrep -f "${pattern}" >/dev/null 2>&1 || return 0
		sleep 2
	done
	printf 'Device still running after 60s (%s); refusing to start the next platform.\n' "${pattern}" >&2
	return 1
}

android_env=(E2E_WIREMOCK_PORT="${android_port}")
if [[ "${E2E_ANDROID_TUNNEL:-on}" == "off" ]]; then
	android_env+=(
		E2E_ANDROID_API_BASE_URL="http://10.0.2.2:${android_port}/"
		E2E_ANDROID_WEB_BASE_URL="http://10.0.2.2:${android_port}"
	)
fi

e2e/scripts/stop-devices.sh ios
env "${android_env[@]}" ./gradlew --console=plain e2eMaestroEvidenceAndroid

e2e/scripts/stop-devices.sh android
wait_for_device_off 'qemu-system-aarch64'
e2e/scripts/boot-devices.sh ios
E2E_WIREMOCK_PORT="${ios_port}" ./gradlew --console=plain e2eMaestroEvidenceIos
