#!/usr/bin/env bash
# Builds the debug app and the scenario runner APK for one WireMock port.
#   build.sh <port>
# stdout: {"app": <apk>, "appId": <id>, "test": <apk>, "testId": <id>}; everything else goes to stderr.
# The APK paths and ids are read from the output-metadata.json the build wrote, never assumed.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
source "${SCRIPT_DIR}/../shared/lib.sh"
source "${SCRIPT_DIR}/../shared/layout.env"

port="${1:?Usage: build.sh <wiremock port>}"
host="10.0.2.2"
if [[ "${E2E_ANDROID_TUNNEL:-host-alias}" == "reverse" ]]; then
	host="localhost"
fi
web_base_url="http://${host}:${port}"

log "Building the debug app (API http://${host}:${port}/) and ${E2E_ANDROID_TEST_MODULE}."
"${REPO_ROOT}/gradlew" --console=plain :app:assembleDebug "${E2E_ANDROID_TEST_ASSEMBLE_TASK}" \
	-Ptuindice.apiBaseUrl="${web_base_url}/" \
	-Ptuindice.privacyPolicyUrl="${web_base_url}/e2e/privacy.html" \
	-Ptuindice.termsAndConditionsUrl="${web_base_url}/e2e/terms.html" \
	-Ptuindice.supportUrl="${web_base_url}/e2e/support.html" >&2

python3 "${SCRIPT_DIR}/../shared/adapter_tools.py" apk-outputs \
	"${REPO_ROOT}/app/build/outputs/apk/debug" "${REPO_ROOT}/${E2E_ANDROID_TEST_MODULE}/build/outputs/apk/debug"
