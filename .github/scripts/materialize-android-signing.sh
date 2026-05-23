#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=.github/scripts/common.sh
source "${SCRIPT_DIR}/common.sh"

decode_base64_to_file() {
	local value="$1"
	local target_file="$2"

	mkdir -p "$(dirname "$target_file")"
	if base64 --help 2>/dev/null | grep -q -- '--decode'; then
		printf '%s' "$value" | base64 --decode >"$target_file"
	else
		printf '%s' "$value" | base64 -D >"$target_file"
	fi
	chmod 600 "$target_file"
}

if [[ -n "${ANDROID_RELEASE_KEYSTORE_BASE64:-}" ]]; then
	TU_INDICE_KEY_STORE_PATH="${TU_INDICE_KEY_STORE_PATH:-${RUNNER_TEMP:-/tmp}/tuindice-release.jks}"
	info "Materializing Android release keystore."
	decode_base64_to_file "$ANDROID_RELEASE_KEYSTORE_BASE64" "$TU_INDICE_KEY_STORE_PATH"
fi

[[ -n "${TU_INDICE_KEY_STORE_PATH:-}" ]] || die "TU_INDICE_KEY_STORE_PATH or ANDROID_RELEASE_KEYSTORE_BASE64 is required."
[[ -s "$TU_INDICE_KEY_STORE_PATH" ]] || die "Android release keystore not found at ${TU_INDICE_KEY_STORE_PATH}."
[[ -n "${TU_INDICE_KEY_ALIAS:-}" ]] || die "TU_INDICE_KEY_ALIAS is required."
[[ -n "${TU_INDICE_KEY_PASSWORD:-}" ]] || die "TU_INDICE_KEY_PASSWORD is required."
[[ -n "${TU_INDICE_KEY_STORE_PASSWORD:-}" ]] || die "TU_INDICE_KEY_STORE_PASSWORD is required."

if [[ -n "${GITHUB_ENV:-}" ]]; then
	printf 'TU_INDICE_KEY_STORE_PATH=%s\n' "$TU_INDICE_KEY_STORE_PATH" >>"$GITHUB_ENV"
fi

info "Android release signing inputs are available."
