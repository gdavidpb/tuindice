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
PRODUCTION_BRANCH="${PRODUCTION_BRANCH:-production}"
SKIP_PRODUCTION_TIP_CHECK="${SKIP_PRODUCTION_TIP_CHECK:-0}"
DEPLOY_PRODUCTION_PHASE="${DEPLOY_PRODUCTION_PHASE:-all}"
PRODUCTION_RELEASE_ARTIFACT_DIR="${PRODUCTION_RELEASE_ARTIFACT_DIR:-build/production-release}"
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

resolve_production_tip_sha() {
	if git fetch --quiet origin "$PRODUCTION_BRANCH" 2>/dev/null; then
		git rev-parse FETCH_HEAD
		return 0
	fi

	if git rev-parse --verify --quiet "refs/remotes/origin/${PRODUCTION_BRANCH}" >/dev/null; then
		git rev-parse "refs/remotes/origin/${PRODUCTION_BRANCH}"
		return 0
	fi

	return 1
}

# Two pushes in a row stage in parallel and their deploys queue by completion
# order, not by commit order: the losing deploy must not publish or tag behind
# the branch tip.
deploy_is_superseded() {
	local tip_sha

	if [[ "$DRY_RUN" == "1" || "$SKIP_PRODUCTION_TIP_CHECK" == "1" ]]; then
		return 1
	fi

	if ! tip_sha="$(resolve_production_tip_sha)"; then
		warn "Unable to resolve the ${PRODUCTION_BRANCH} tip; deploying ${TARGET_GIT_SHA} without the superseded-deploy guard."
		return 1
	fi

	if [[ "$tip_sha" == "$TARGET_GIT_SHA" ]]; then
		return 1
	fi

	info "Skipping deploy of ${TARGET_GIT_SHA}: ${PRODUCTION_BRANCH} already advanced to ${tip_sha}."
	return 0
}

create_release_tag() {
	local existing_target

	existing_target="$(existing_tag_target "$TAG_NAME" || true)"
	if [[ -n "$existing_target" ]]; then
		if [[ "$existing_target" == "$TARGET_GIT_SHA" ]]; then
			info "Release tag ${TAG_NAME} already exists on ${TARGET_GIT_SHA}."
			return 0
		fi
		die "Release tag ${TAG_NAME} already points at ${existing_target}: ${VERSION_NAME} was released from another commit before this deploy of ${TARGET_GIT_SHA} reached the tag phase. Bump $(app_version_file) if this commit needs its own release."
	fi

	info "Creating release tag ${TAG_NAME} on ${TARGET_GIT_SHA}."
	git tag -a "$TAG_NAME" "$TARGET_GIT_SHA" -m "TuIndice app ${VERSION_NAME}"
	git push origin "refs/tags/${TAG_NAME}"
}

run_deploy_preflight() {
	local detect_output_file
	local has_relevant_changes
	local app_version_changed
	local has_release_impact
	local requires_e2e_certification
	local should_deploy
	local skip_e2e_status_check

	BEFORE_SHA="$(resolve_deploy_diff_base_sha)"
	[[ -n "$BEFORE_SHA" ]] || die "Unable to resolve previous production SHA."

	DETECT_STATE_DIR="${STATE_DIR}/detect"
	mkdir -p "$DETECT_STATE_DIR"
	detect_output_file="${STATE_DIR}/detect-output.env"

	GITHUB_OUTPUT="$detect_output_file" \
	STATE_DIR="$DETECT_STATE_DIR" \
		bash "${SCRIPT_DIR}/detect-changed-app.sh" "$BEFORE_SHA" "$TARGET_GIT_SHA"

	has_relevant_changes="$(awk -F= '$1 == "has_relevant_changes" { print $2 }' "$detect_output_file")"
	app_version_changed="$(awk -F= '$1 == "app_version_changed" { print $2 }' "$detect_output_file")"
	has_release_impact="$(awk -F= '$1 == "has_release_impact" { print $2 }' "$detect_output_file")"
	requires_e2e_certification="$(awk -F= '$1 == "requires_e2e_certification" { print $2 }' "$detect_output_file")"

	# Deploy when the diff carries release changes, or when the current app
	# version was never tagged: a cancelled or failed deploy must not be lost
	# just because a later push has no release impact of its own.
	should_deploy=false
	if [[ "$app_version_changed" == "true" || "$has_release_impact" == "true" ]]; then
		should_deploy=true
	elif [[ -z "$(existing_tag_target "$TAG_NAME" || true)" ]]; then
		info "Release tag ${TAG_NAME} does not exist yet; resuming pending deploy for ${VERSION_NAME}."
		should_deploy=true
		has_relevant_changes=true
		has_release_impact=true
	fi

	if [[ -n "${GITHUB_OUTPUT:-}" ]]; then
		printf 'should_deploy=%s\n' "$should_deploy" >>"$GITHUB_OUTPUT"
	fi

	# E2E statuses are revalidated against the deploy SHA; fingerprint reuse
	# accepts evidence certified on the merged PR head. Without a token (local
	# preflight) the check is skipped with a warning.
	skip_e2e_status_check="${SKIP_E2E_STATUS_CHECK:-}"
	if [[ -z "$skip_e2e_status_check" ]]; then
		if [[ -n "${GITHUB_TOKEN:-${GH_TOKEN:-}}" && -n "${GITHUB_REPOSITORY:-}" ]]; then
			skip_e2e_status_check=0
		else
			warn "Skipping E2E status revalidation: GITHUB_TOKEN/GITHUB_REPOSITORY are not available."
			skip_e2e_status_check=1
		fi
	fi

	MISSING_VERSION_BUMP_FILE="${DETECT_STATE_DIR}/missing-version-bump.txt" \
	E2E_ANDROID_CONTEXTS_FILE="${DETECT_STATE_DIR}/e2e-android-contexts.txt" \
	E2E_IOS_CONTEXTS_FILE="${DETECT_STATE_DIR}/e2e-ios-contexts.txt" \
	REQUIRES_E2E_CERTIFICATION="${requires_e2e_certification:-false}" \
	HAS_RELEVANT_CHANGES="$has_relevant_changes" \
	APP_VERSION_CHANGED="$app_version_changed" \
	HAS_RELEASE_IMPACT="$has_release_impact" \
	TARGET_GIT_SHA="$TARGET_GIT_SHA" \
	E2E_REUSE_BASE_SHA="$BEFORE_SHA" \
	SKIP_E2E_STATUS_CHECK="$skip_e2e_status_check" \
		bash "${SCRIPT_DIR}/preflight-production.sh"

	if [[ "$should_deploy" != "true" ]]; then
		info "No app version or runtime release changes detected; production deploy jobs should be skipped."
	fi
}

validate_production_release_artifact() {
	RELEASE_ARTIFACT_DIR="$PRODUCTION_RELEASE_ARTIFACT_DIR" \
	TARGET_GIT_SHA="$TARGET_GIT_SHA" \
		bash "${SCRIPT_DIR}/production-release-artifact.sh" validate-manifest
}

production_release_manifest_value() {
	local jq_filter="$1"
	jq -r "$jq_filter" "${PRODUCTION_RELEASE_ARTIFACT_DIR}/release-manifest.json"
}

run_android_deploy() {
	local release_exists_file="${STATE_DIR}/android-release-exists.env"
	local staged_aab_path

	if deploy_is_superseded; then
		return 0
	fi

	if [[ "$DRY_RUN" != "1" ]]; then
		GOOGLE_PLAY_CHECK_ONLY=1 \
		GOOGLE_PLAY_RELEASE_EXISTS_FILE="$release_exists_file" \
			bash "${SCRIPT_DIR}/publish-google-play-draft.sh"

		if grep -q '^exists=true$' "$release_exists_file"; then
			return 0
		fi
	fi

	validate_production_release_artifact
	staged_aab_path="${PRODUCTION_RELEASE_ARTIFACT_DIR}/$(production_release_manifest_value '.android.aabPath')"

	if [[ "$DRY_RUN" == "1" ]]; then
		info "DRY_RUN=1: validated staged Android App Bundle at ${staged_aab_path}; skipping Google Play upload."
		return 0
	fi

	ANDROID_AAB_PATH="$staged_aab_path" \
	bash "${SCRIPT_DIR}/publish-google-play-draft.sh"
}

run_ios_deploy() {
	local build_exists_file="${STATE_DIR}/ios-build-exists.env"
	local staged_ipa_path

	if deploy_is_superseded; then
		return 0
	fi

	if [[ "$DRY_RUN" != "1" ]]; then
		APP_STORE_CONNECT_CHECK_ONLY=1 \
		APP_STORE_CONNECT_BUILD_EXISTS_FILE="$build_exists_file" \
			bash "${PWD}/iosApp/scripts/ci-upload-ios-appstore.sh"

		if grep -q '^exists=true$' "$build_exists_file"; then
			return 0
		fi
	fi

	validate_production_release_artifact
	staged_ipa_path="${PRODUCTION_RELEASE_ARTIFACT_DIR}/$(production_release_manifest_value '.ios.ipaPath')"

	DRY_RUN="$DRY_RUN" \
	IOS_IPA_PATH="$staged_ipa_path" \
		bash "${PWD}/iosApp/scripts/ci-upload-ios-appstore.sh"
}

run_release_tag() {
	if deploy_is_superseded; then
		return 0
	fi

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
		if [[ "$DRY_RUN" == "1" && "$OSTYPE" != darwin* ]]; then
			warn "DRY_RUN=1: skipping iOS archive because this host is not macOS."
		else
			run_ios_deploy
		fi
		run_release_tag
		if [[ "$DRY_RUN" == "1" ]]; then
			info "Production dry-run completed for ${VERSION_NAME} (${TAG_NAME}): artifacts built, nothing published."
		else
			info "Production deploy completed for ${VERSION_NAME} (${TAG_NAME})."
		fi
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
