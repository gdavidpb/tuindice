#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

log "No iOS-only E2E edge suites are registered yet."
log "Register XCUITest suites under e2e/platform/ios when Maestro cannot cover a case."
