#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=.github/scripts/common.sh
source "${SCRIPT_DIR}/common.sh"

require_tool git
require_tool jq

TARGET_GIT_SHA="${TARGET_GIT_SHA:-${GITHUB_SHA:-$(git rev-parse HEAD)}}"
DEPLOY_DIFF_BASE_SHA="${DEPLOY_DIFF_BASE_SHA:-}"
STATE_DIR="${STATE_DIR:-$(mktemp -d "${RUNNER_TEMP:-/tmp}/tuindice-deploy.XXXXXX")}"
DRY_RUN="${DRY_RUN:-0}"
DEPLOY_PRODUCTION_PHASE="${DEPLOY_PRODUCTION_PHASE:-all}"
VERSION_NAME="$(get_app_version_name)"
TAG_NAME="$(app_tag_name "$VERSION_NAME")"

resolve_deploy_diff_base_sha() {
	local before_sha="${DEPLOY_DIFF_BASE_SHA:-}"

	if [[ -n "$before_sha" ]]; then
		printf '%s\n' "$before_sha"
		return
	fi

	if [[ -n "${GITHUB_EVENT_PATH:-}" && -f "${GITHUB_EVENT_PATH:-}" ]]; then
		before_sha="$(jq -r '.before // empty' "$GITHUB_EVENT_PATH")"
	fi

	if is_zero_sha "$before_sha"; then
		before_sha="$(git rev-parse "${TARGET_GIT_SHA}^" 2>/dev/null || true)"
	fi

	printf '%s\n' "$before_sha"
}

create_release_tag() {
	local existing_target

	existing_target="$(existing_tag_target "$TAG_NAME" || true)"
	if [[ -n "$existing_target" ]]; then
		if [[ "$existing_target" == "$TARGET_GIT_SHA" ]]; then
			info "Release tag ${TAG_NAME} already exists on ${TARGET_GIT_SHA}."
			return 0
		fi
		die "Release tag ${TAG_NAME} already exists at ${existing_target}. Bump $(app_version_file) before deploying."
	fi

	info "Creating release tag ${TAG_NAME} on ${TARGET_GIT_SHA}."
	git tag -a "$TAG_NAME" "$TARGET_GIT_SHA" -m "TuIndice app ${VERSION_NAME}"
	git push origin "refs/tags/${TAG_NAME}"
}

run_deploy_preflight() {
	BEFORE_SHA="$(resolve_deploy_diff_base_sha)"
	[[ -n "$BEFORE_SHA" ]] || die "Unable to resolve previous production SHA."

	DETECT_STATE_DIR="${STATE_DIR}/detect"
	mkdir -p "$DETECT_STATE_DIR"

	STATE_DIR="$DETECT_STATE_DIR" bash "${SCRIPT_DIR}/detect-changed-app.sh" "$BEFORE_SHA" "$TARGET_GIT_SHA"

	MISSING_VERSION_BUMP_FILE="${DETECT_STATE_DIR}/missing-version-bump.txt" \
	E2E_ANDROID_CONTEXTS_FILE="${DETECT_STATE_DIR}/e2e-android-contexts.txt" \
	E2E_IOS_CONTEXTS_FILE="${DETECT_STATE_DIR}/e2e-ios-contexts.txt" \
	REQUIRES_E2E_CERTIFICATION="false" \
	HAS_RELEVANT_CHANGES="true" \
	TARGET_GIT_SHA="$TARGET_GIT_SHA" \
	SKIP_E2E_STATUS_CHECK=1 \
		bash "${SCRIPT_DIR}/preflight-production.sh"
}

run_android_deploy() {
	local release_exists_file="${STATE_DIR}/android-release-exists.env"

	if [[ "$DRY_RUN" != "1" ]]; then
		GOOGLE_PLAY_CHECK_ONLY=1 \
		GOOGLE_PLAY_RELEASE_EXISTS_FILE="$release_exists_file" \
			bash "${SCRIPT_DIR}/publish-google-play-draft.sh"

		if grep -q '^exists=true$' "$release_exists_file"; then
			return 0
		fi
	fi

	REQUIRE_ANDROID_FIREBASE_CONFIG=1 bash "${SCRIPT_DIR}/materialize-firebase-configs.sh"
	export TU_INDICE_KEY_STORE_PATH="${TU_INDICE_KEY_STORE_PATH:-${RUNNER_TEMP:-/tmp}/tuindice-release.jks}"
	bash "${SCRIPT_DIR}/materialize-android-signing.sh"

	info "Building signed Android App Bundle."
	./gradlew --console=plain :app:bundleRelease

	if [[ "$DRY_RUN" == "1" ]]; then
		info "DRY_RUN=1: skipping Google Play upload."
		return 0
	fi

	bash "${SCRIPT_DIR}/publish-google-play-draft.sh"
}

run_ios_deploy() {
	if [[ "$DRY_RUN" == "1" ]]; then
		info "DRY_RUN=1: skipping App Store Connect upload."
		return 0
	fi

	local build_exists_file="${STATE_DIR}/ios-build-exists.env"
	APP_STORE_CONNECT_CHECK_ONLY=1 \
	APP_STORE_CONNECT_BUILD_EXISTS_FILE="$build_exists_file" \
		bash "${PWD}/iosApp/scripts/ci-upload-ios-appstore.sh"

	if grep -q '^exists=true$' "$build_exists_file"; then
		return 0
	fi

	REQUIRE_IOS_FIREBASE_CONFIG=1 bash "${SCRIPT_DIR}/materialize-firebase-configs.sh"

	bash "${PWD}/iosApp/scripts/ci-upload-ios-appstore.sh"
}

run_release_tag() {
	if [[ "$DRY_RUN" == "1" ]]; then
		info "DRY_RUN=1: skipping release tag creation."
		return 0
	fi

	create_release_tag
}

case "$DEPLOY_PRODUCTION_PHASE" in
	all)
		run_deploy_preflight
		run_android_deploy
		if [[ "$DRY_RUN" == "1" ]]; then
			info "DRY_RUN=1: skipping App Store Connect upload and release tag creation."
			exit 0
		fi
		run_ios_deploy
		run_release_tag
		info "Production deploy completed for ${VERSION_NAME} (${TAG_NAME})."
		;;
	preflight)
		run_deploy_preflight
		;;
	android)
		run_android_deploy
		;;
	ios)
		run_ios_deploy
		;;
	tag)
		run_release_tag
		;;
	*)
		die "Unsupported DEPLOY_PRODUCTION_PHASE '${DEPLOY_PRODUCTION_PHASE}'. Expected all, preflight, android, ios, or tag."
		;;
esac
