#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

REPO_ROOT="$(git rev-parse --show-toplevel)"
cd "$REPO_ROOT"

# shellcheck source=.github/scripts/common.sh
source ".github/scripts/common.sh"

DRY_RUN=false
if [[ "${1:-}" == "--dry-run" ]]; then
	DRY_RUN=true
	shift
fi

[[ "$#" -eq 0 ]] || die "Usage: $0 [--dry-run]"

resolve_merge_base() {
	local head_sha="$1"
	local ref
	local merge_base

	for ref in origin/production production; do
		merge_base="$(git merge-base "$ref" "$head_sha" 2>/dev/null || true)"
		if [[ -n "$merge_base" ]]; then
			printf '%s\n' "$merge_base"
			return 0
		fi
	done

	return 1
}

github_output_value() {
	local key="$1"
	local file="$2"

	awk -F= -v key="$key" '$1 == key { print substr($0, length(key) + 2); exit }' "$file"
}

print_command() {
	printf '  '
	printf '%q ' "$@"
	printf '\n'
}

load_github_env_file() {
	local file="$1"
	local key
	local value

	[[ -s "$file" ]] || return 0

	while IFS='=' read -r key value; do
		[[ "$key" =~ ^[A-Za-z_][A-Za-z0-9_]*$ ]] || continue
		export "${key}=${value}"
	done <"$file"
}

sanitize_sensitive_environment() {
	local key

	while IFS= read -r key; do
		case "$key" in
			*PASSWORD*|*SECRET*|*TOKEN*|*PRIVATE_KEY*|*CERTIFICATE*|*PROVISION*|*CREDENTIAL*|\
			APP_STORE_CONNECT_*|ASC_*|FASTLANE_*|MATCH_*|SIGH_*|GYM_*|PILOT_*|\
			FIREBASE_*|GOOGLE_SERVICE*|GOOGLE_SERVICES*|GOOGLE_APPLICATION_CREDENTIALS|\
			ANDROID_GOOGLE_SERVICES_JSON_BASE64|IOS_GOOGLE_SERVICE_INFO_PLIST_BASE64|\
			TU_INDICE_KEY_*|GITHUB_TOKEN|GH_TOKEN|ACTIONS_ID_TOKEN_REQUEST_TOKEN)
				unset "$key"
				;;
		esac
	done < <(compgen -e)
}

require_clean_tree() {
	local status

	status="$(git status --porcelain)"
	[[ -z "$status" ]] || die "Working tree must be clean before preflight parity checks."
}

require_clean_tree

HEAD_SHA="${TARGET_GIT_SHA:-$(git rev-parse HEAD)}"
BEFORE_SHA="${BASE_SHA:-$(resolve_merge_base "$HEAD_SHA" || true)}"
[[ -n "$BEFORE_SHA" ]] || die "Unable to resolve merge-base with production."

STATE_DIR="$(mktemp -d "${RUNNER_TEMP:-/tmp}/tuindice-preflight-parity.XXXXXX")"
GITHUB_OUTPUT_FILE="${STATE_DIR}/detect-output.env"

info "Resolving PR preflight scope between ${BEFORE_SHA} and ${HEAD_SHA}."
GITHUB_OUTPUT="$GITHUB_OUTPUT_FILE" \
	STATE_DIR="${STATE_DIR}/state" \
	bash ./.github/scripts/detect-changed-app.sh "$BEFORE_SHA" "$HEAD_SHA"

ANDROID_TASKS="$(github_output_value android_tasks "$GITHUB_OUTPUT_FILE")"
IOS_TASKS="$(github_output_value ios_tasks "$GITHUB_OUTPUT_FILE")"
SEMGREP_REQUIRED="$(github_output_value semgrep_required "$GITHUB_OUTPUT_FILE")"
CI_CONFIG_TOUCHED="$(github_output_value ci_config_touched "$GITHUB_OUTPUT_FILE")"
IOS_CI_SCRIPTS_TOUCHED="$(github_output_value ios_ci_scripts_touched "$GITHUB_OUTPUT_FILE")"
HAS_RELEVANT_CHANGES="$(github_output_value has_relevant_changes "$GITHUB_OUTPUT_FILE")"
APP_VERSION_CHANGED="$(github_output_value app_version_changed "$GITHUB_OUTPUT_FILE")"
HAS_RELEASE_IMPACT="$(github_output_value has_release_impact "$GITHUB_OUTPUT_FILE")"

info "Android preflight tasks: ${ANDROID_TASKS:-<none>}"
info "iOS preflight tasks: ${IOS_TASKS:-<none>}"

if [[ "$HAS_RELEVANT_CHANGES" != "true" ]]; then
	info "No deployable app changes were detected; preflight parity checks are not required."
	exit 0
fi

if [[ "$DRY_RUN" == "true" ]]; then
	# Siempre: la deriva working-tree vs git (script ignorado por /scripts/*)
	# no depende de que el diff toque CI.
	print_command bash ./.github/scripts/verify-workflow-refs.sh
	if [[ "$CI_CONFIG_TOUCHED" == "true" ]]; then
		print_command bash ./.github/scripts/validate-ci-config.sh
	fi
	if [[ "$SEMGREP_REQUIRED" == "true" ]]; then
		# Paridad con el step "Run semgrep architecture checks" del preflight:
		# incluye detekt vía ANDROID_TASKS y semgrep vía este script.
		print_command bash ./scripts/semgrep-architecture.sh
	fi
	if [[ -n "$ANDROID_TASKS" ]]; then
		IFS=' ' read -r -a android_task_array <<<"$ANDROID_TASKS"
		print_command bash ./.github/scripts/run-gradle-with-retry.sh \
			./gradlew --continue --console=plain --max-workers=2 "${android_task_array[@]}"
	fi
	if [[ "$IOS_CI_SCRIPTS_TOUCHED" == "true" ]]; then
		print_command bash ./.github/scripts/test-appstore-connect-check.sh
	fi
	if [[ -n "$IOS_TASKS" ]]; then
		IFS=' ' read -r -a ios_task_array <<<"$IOS_TASKS"
		print_command bash ./.github/scripts/run-gradle-with-retry.sh \
			./gradlew \
			-I .github/gradle/ios-host-cache.init.gradle.kts \
			--continue \
			--console=plain \
			--max-workers=2 \
			-Pcompose.ios.resources.platform=iphoneos \
			-Pcompose.ios.resources.archs=arm64 \
			"${ios_task_array[@]}"
	fi
	exit 0
fi

# Siempre, no solo con CI tocado: atrapa archivos referenciados por workflows
# que existen localmente pero no están trackeados (la parity corre contra el
# working tree; CI corre contra el checkout de git).
bash ./.github/scripts/verify-workflow-refs.sh

if [[ "$CI_CONFIG_TOUCHED" == "true" ]]; then
	bash ./.github/scripts/validate-ci-config.sh
fi

if [[ "$SEMGREP_REQUIRED" == "true" ]]; then
	# Paridad con el step "Run semgrep architecture checks" del preflight de CI.
	# detekt ya corre en paridad dentro de ANDROID_TASKS (:modulo:detekt o detekt
	# completo cuando cambia config/baseline), igual que en el job Android de CI.
	command -v semgrep >/dev/null 2>&1 \
		|| die "semgrep CLI is required for preflight parity (brew install semgrep)."
	bash ./scripts/semgrep-architecture.sh
fi

if [[ -n "$ANDROID_TASKS" ]]; then
	(
		sanitize_sensitive_environment

		IFS=' ' read -r -a android_task_array <<<"$ANDROID_TASKS"
		if [[ "$ANDROID_TASKS" == *":app:bundleRelease"* ]]; then
			android_env_file="${STATE_DIR}/android-github-env"
			GITHUB_ENV="$android_env_file" \
				CI_PLACEHOLDER_IOS=0 \
				bash ./.github/scripts/materialize-ci-placeholders.sh
			load_github_env_file "$android_env_file"
		fi

		if [[ "$APP_VERSION_CHANGED" != "true" && "$HAS_RELEASE_IMPACT" != "true" ]]; then
			export SKIP_APP_VERSION_TAG_CONFLICT_CHECK=1
		else
			export SKIP_APP_VERSION_TAG_CONFLICT_CHECK=0
		fi

		bash ./.github/scripts/run-gradle-with-retry.sh \
			./gradlew --continue --console=plain --max-workers=2 "${android_task_array[@]}"
	)
fi

if [[ "$IOS_CI_SCRIPTS_TOUCHED" == "true" ]]; then
	(
		sanitize_sensitive_environment
		bash ./.github/scripts/test-appstore-connect-check.sh
	)
fi

if [[ -n "$IOS_TASKS" ]]; then
	(
		sanitize_sensitive_environment

		IFS=' ' read -r -a ios_task_array <<<"$IOS_TASKS"
		if [[ "$IOS_TASKS" == *"verifyIosHostBuildDeviceRelease"* ]]; then
			CI_PLACEHOLDER_ANDROID=0 bash ./.github/scripts/materialize-ci-placeholders.sh
		fi

		TUINDICE_IOS_HOST_BUILD=1 \
		TUINDICE_IOS_HOST_E2E=1 \
		PLATFORM_NAME=iphoneos \
		ARCHS=arm64 \
		BUILT_PRODUCTS_DIR="${REPO_ROOT}/iosApp/.build/ios-host-device-release/Build/Products/Release-iphoneos" \
		UNLOCALIZED_RESOURCES_FOLDER_PATH=TuIndiceHost.app \
		TUINDICE_IOS_HOST_DEVICE_CODE_SIGNING_ALLOWED=NO \
			bash ./.github/scripts/run-gradle-with-retry.sh \
				./gradlew \
				-I .github/gradle/ios-host-cache.init.gradle.kts \
				--continue \
				--console=plain \
				--max-workers=2 \
				-Pcompose.ios.resources.platform=iphoneos \
				-Pcompose.ios.resources.archs=arm64 \
				"${ios_task_array[@]}"
	)
fi

require_clean_tree
info "Preflight parity checks passed for ${HEAD_SHA}."
