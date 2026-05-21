#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

log "Building Android debug app with API base URL ${E2E_ANDROID_API_BASE_URL}."
"${REPO_ROOT}/gradlew" \
	--console=plain \
	:app:assembleDebug \
	-Ptuindice.apiBaseUrl="${E2E_ANDROID_API_BASE_URL}" \
	-Ptuindice.privacyPolicyUrl="${E2E_ANDROID_WEB_BASE_URL}/e2e/privacy.html" \
	-Ptuindice.termsAndConditionsUrl="${E2E_ANDROID_WEB_BASE_URL}/e2e/terms.html" \
	-Ptuindice.supportUrl="${E2E_ANDROID_WEB_BASE_URL}/e2e/support.html"

printf '%s\n' "${REPO_ROOT}/app/build/outputs/apk/debug/app-debug.apk"
