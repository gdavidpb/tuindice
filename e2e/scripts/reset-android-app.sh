#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

require_command adb

log "Preparing Android package ${E2E_APP_ID}."
adb reverse "tcp:${E2E_WIREMOCK_PORT}" "tcp:${E2E_WIREMOCK_PORT}" >/dev/null

# `adb reverse` can report success, and even list the tunnel, while adbd never
# forwards a byte. The app then fails every networked flow with a product-shaped
# symptom -- stuck on sign-in, no request in the WireMock log -- which is an
# expensive thing to chase. Prove the tunnel end to end instead of trusting it.
if ! adb shell 'command -v toybox' >/dev/null 2>&1; then
	log "Skipping adb reverse probe: no toybox on the device to drive it."
elif ! adb shell "printf 'GET /__admin HTTP/1.0\r\n\r\n' | toybox nc -w 5 127.0.0.1 ${E2E_WIREMOCK_PORT}" 2>/dev/null | grep -q "HTTP/"; then
	printf 'adb reverse on tcp:%s mounted but carries no traffic.\n' "${E2E_WIREMOCK_PORT}" >&2
	printf 'Point the app at the emulator host alias instead:\n' >&2
	printf '  E2E_ANDROID_API_BASE_URL=http://10.0.2.2:%s/ E2E_ANDROID_WEB_BASE_URL=http://10.0.2.2:%s\n' \
		"${E2E_WIREMOCK_PORT}" "${E2E_WIREMOCK_PORT}" >&2
	exit 1
fi
disable_android_keyboard_helpers
adb shell pm clear "${E2E_APP_ID}" >/dev/null || true
