#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=.github/scripts/common.sh
source "${SCRIPT_DIR}/common.sh"

BEFORE_SHA="${1:-${GIT_BEFORE_SHA:-}}"
AFTER_SHA="${2:-${GIT_AFTER_SHA:-${GITHUB_SHA:-HEAD}}}"
STATE_DIR="${STATE_DIR:-$(mktemp -d "${RUNNER_TEMP:-/tmp}/tuindice-changes.XXXXXX")}"

CHANGED_FILES_FILE="${CHANGED_FILES_FILE:-${STATE_DIR}/changed-files.txt}"
IMPACTED_MODULES_FILE="${IMPACTED_MODULES_FILE:-${STATE_DIR}/impacted-modules.txt}"
RELEASE_IMPACTED_MODULES_FILE="${RELEASE_IMPACTED_MODULES_FILE:-${STATE_DIR}/release-impacted-modules.txt}"
MISSING_VERSION_BUMP_FILE="${MISSING_VERSION_BUMP_FILE:-${STATE_DIR}/missing-version-bump.txt}"
ANDROID_TASKS_FILE="${ANDROID_TASKS_FILE:-${STATE_DIR}/android-gradle-tasks.txt}"
IOS_TASKS_FILE="${IOS_TASKS_FILE:-${STATE_DIR}/ios-gradle-tasks.txt}"
E2E_SUITES_FILE="${E2E_SUITES_FILE:-${STATE_DIR}/e2e-suites.txt}"
E2E_SCOPE_FILE="${E2E_SCOPE_FILE:-${STATE_DIR}/e2e-scope.csv}"
E2E_ANDROID_CONTEXTS_FILE="${E2E_ANDROID_CONTEXTS_FILE:-${STATE_DIR}/e2e-android-contexts.txt}"
E2E_IOS_CONTEXTS_FILE="${E2E_IOS_CONTEXTS_FILE:-${STATE_DIR}/e2e-ios-contexts.txt}"

mkdir -p "$STATE_DIR"
: >"$CHANGED_FILES_FILE"
: >"$IMPACTED_MODULES_FILE"
: >"$RELEASE_IMPACTED_MODULES_FILE"
: >"$MISSING_VERSION_BUMP_FILE"
: >"$ANDROID_TASKS_FILE"
: >"$IOS_TASKS_FILE"
: >"$E2E_SUITES_FILE"
: >"$E2E_SCOPE_FILE"
: >"$E2E_ANDROID_CONTEXTS_FILE"
: >"$E2E_IOS_CONTEXTS_FILE"

APP_VERSION_TOUCHED=false
APP_VERSION_CHANGED=false
APP_VERSION_NAME_CHANGED=false
ANDROID_VERSION_CODE_CHANGED=false
IOS_BUILD_NUMBER_CHANGED=false
CI_CONFIG_TOUCHED=false
IOS_CI_SCRIPTS_TOUCHED=false
E2E_CONTRACT_TOUCHED=false
HAS_RELEVANT_CHANGES=false
HAS_RELEASE_IMPACT=false
REQUIRES_E2E_CERTIFICATION=false
PROCESSED_RUNTIME_MODULES=$'\n'
PROCESSED_E2E_SUITES=$'\n'
PROCESSED_E2E_SCOPES=$'\n'

value_seen_in_newline_set() {
	local set_value="$1"
	local value="$2"

	[[ "$set_value" == *$'\n'"${value}"$'\n'* ]]
}

mark_runtime_module_processed() {
	local module="$1"

	PROCESSED_RUNTIME_MODULES="${PROCESSED_RUNTIME_MODULES}${module}"$'\n'
}

append_e2e_suite() {
	local suite="$1"

	if [[ -n "$suite" ]] && ! value_seen_in_newline_set "$PROCESSED_E2E_SUITES" "$suite"; then
		PROCESSED_E2E_SUITES="${PROCESSED_E2E_SUITES}${suite}"$'\n'
		append_unique_line "$E2E_SUITES_FILE" "$suite"
	fi
}

append_e2e_scope() {
	local platform="$1"
	local suite="$2"
	local reason="${3:-changed-runtime}"
	local key

	if [[ "$platform" == "all" ]]; then
		append_e2e_scope android "$suite" "$reason"
		append_e2e_scope ios "$suite" "$reason"
		return 0
	fi

	case "$platform" in
		android|ios)
			;;
		*)
			die "Unsupported E2E platform scope '${platform}'."
			;;
	esac

	if [[ -z "$suite" ]]; then
		return 0
	fi

	key="${platform}|${suite}"
	if ! value_seen_in_newline_set "$PROCESSED_E2E_SCOPES" "$key"; then
		PROCESSED_E2E_SCOPES="${PROCESSED_E2E_SCOPES}${key}"$'\n'
		append_unique_line "$E2E_SCOPE_FILE" "${platform},${suite},${reason}"
	fi

	append_e2e_suite "$suite"
	REQUIRES_E2E_CERTIFICATION=true
}

append_e2e_scope_for_module() {
	local platform="$1"
	local module="$2"
	local reason="${3:-changed-runtime}"
	local suite

	suite="$(module_e2e_suite "$module" || true)"
	if [[ -n "$suite" ]]; then
		append_e2e_scope "$platform" "$suite" "$reason"
	fi
}

append_e2e_scopes_for_modules() {
	local platform="$1"
	local reason="$2"
	shift 2

	local module
	for module in "$@"; do
		append_e2e_scope_for_module "$platform" "$module" "$reason"
	done
}

append_runtime_module() {
	local module="$1"

	if value_seen_in_newline_set "$PROCESSED_RUNTIME_MODULES" "$module"; then
		return 0
	fi

	mark_runtime_module_processed "$module"
	append_module_closure "$module" "$IMPACTED_MODULES_FILE"
	if module_is_runtime "$module"; then
		append_module_closure "$module" "$RELEASE_IMPACTED_MODULES_FILE"
	fi
}

append_changed_test_module() {
	local module="$1"

	append_unique_line "$IMPACTED_MODULES_FILE" "$module"
}

is_app_test_source_file() {
	local file="$1"

	case "$file" in
		app/src/test/*|app/src/androidTest/*)
			return 0
			;;
	esac

	return 1
}

is_ios_app_test_source_file() {
	local file="$1"

	case "$file" in
		iosApp/Tests/*|iosApp/*Tests/*)
			return 0
			;;
	esac

	return 1
}

is_kmp_test_source_file() {
	local module="$1"
	local file="$2"

	case "$file" in
		"$module/src/"*Test/*|"$module/src/test/"*)
			return 0
			;;
	esac

	return 1
}

is_kmp_runtime_source_or_build_file() {
	local module="$1"
	local file="$2"

	case "$file" in
		"$module/build.gradle.kts"|"$module/src/commonMain/"*|"$module/src/androidMain/"*|"$module/src/iosMain/"*|"$module/src/appleMain/"*|"$module/src/nativeMain/"*|"$module/src/iosArm64Main/"*|"$module/src/iosSimulatorArm64Main/"*|"$module/src/iosX64Main/"*)
			return 0
			;;
	esac

	return 1
}

mark_e2e_suite_for_module() {
	local module="$1"

	append_e2e_scope_for_module all "$module" "module-runtime"
}

append_ios_signing_config_validation() {
	CI_CONFIG_TOUCHED=true
	HAS_RELEVANT_CHANGES=true
	append_unique_line "$IOS_TASKS_FILE" "verifyIosHostBuildDeviceRelease"
}

is_ios_signing_only_config_change() {
	local file="$1"
	local diff_line
	local line
	local trimmed
	local key
	local has_signing_change=false

	while IFS= read -r diff_line; do
		case "$diff_line" in
			---*|+++*|@@*)
				continue
				;;
			[+-]*)
				line="${diff_line:1}"
				trimmed="$(printf '%s\n' "$line" | sed -E 's/^[[:space:]]+//;s/[[:space:]]+$//')"

				case "$trimmed" in
					""|//*|/\**|\**|\*/)
						continue
						;;
				esac

				key="$(printf '%s\n' "$trimmed" | sed -E 's/[[:space:]]*=.*$//;s/[[:space:]]+$//')"
				case "$key" in
					CODE_SIGN_IDENTITY|CODE_SIGN_STYLE|DEVELOPMENT_TEAM|PROVISIONING_PROFILE|PROVISIONING_PROFILE_SPECIFIER|TUINDICE_CODE_SIGN_IDENTITY|TUINDICE_CODE_SIGN_STYLE|TUINDICE_DEVELOPMENT_TEAM|TUINDICE_PROVISIONING_PROFILE_SPECIFIER|TUINDICE_PROVISIONING_PROFILE_UUID)
						has_signing_change=true
						;;
					*)
						return 1
						;;
				esac
				;;
		esac
	done < <(git diff --unified=0 "$BEFORE_SHA" "$AFTER_SHA" -- "$file")

	[[ "$has_signing_change" == "true" ]]
}

append_e2e_scope_for_mock_path() {
	local file="$1"

	case "$file" in
		mocks/mappings/login/*)
			append_e2e_scope all auth-suite "mock-login"
			;;
		mocks/mappings/enrollmentproof/*)
			append_e2e_scope all enrollmentproof-suite "mock-enrollmentproof"
			;;
		mocks/mappings/evaluations/*)
			append_e2e_scope all evaluations-suite "mock-evaluations"
			;;
		mocks/mappings/pensums/*|mocks/__files/pensums/*)
			append_e2e_scope all pensum-suite "mock-pensum"
			;;
		mocks/mappings/record/*)
			append_e2e_scope all record-suite "mock-record"
			;;
		mocks/mappings/subjects/*|mocks/__files/subjects/*)
			append_e2e_scope all subjects-suite "mock-subjects"
			;;
		mocks/mappings/summary/*|mocks/__files/summary/*)
			append_e2e_scope all summary-suite "mock-summary"
			;;
		*)
			append_e2e_scope all local-certification-suite "mock-shared"
			;;
	esac
}

append_e2e_scope_for_shared_module_path() {
	local module="$1"
	local file="$2"

	case "$module" in
		academiccore)
			append_e2e_scopes_for_modules all "academiccore-runtime" record evaluations pensum wizard
			;;
		persistence)
			append_e2e_scopes_for_modules all "persistence-runtime" summary record evaluations enrollmentproof subjects pensum
			case "$file" in
				persistence/build.gradle.kts|persistence/src/*Main/kotlin/*/di/*|persistence/src/*Main/kotlin/*/data/source/*|persistence/src/*Main/kotlin/*/data/room/schema/*)
					append_e2e_scope all maincore-suite "persistence-bootstrap"
					;;
			esac
			;;
		base)
			case "$file" in
				base/src/*Main/kotlin/*/domain/model/quarter/*|base/src/*Main/kotlin/*/domain/model/subject/*)
					append_e2e_scopes_for_modules all "base-academic-models" summary record evaluations subjects pensum wizard
					;;
				base/src/*Main/kotlin/*/domain/model/mutation/*)
					append_e2e_scopes_for_modules all "base-mutation-models" auth record evaluations
					;;
				*)
					append_e2e_scope all local-certification-suite "base-shared-runtime"
					;;
			esac
			;;
		maincore)
			append_e2e_scope all local-certification-suite "maincore-runtime"
			;;
	esac
}

classify_changed_file() {
	local file="$1"
	local top_level="${file%%/*}"
	local suite_name

	case "$file" in
		.DS_Store|*/.DS_Store)
			return 0
			;;
		.github/workflows/*|.github/scripts/*|.github/actions/*)
			CI_CONFIG_TOUCHED=true
			HAS_RELEVANT_CHANGES=true
			return 0
			;;
		"$(app_version_file)")
			APP_VERSION_TOUCHED=true
			HAS_RELEVANT_CHANGES=true
			return 0
			;;
		iosApp/Config/Version.xcconfig)
			APP_VERSION_TOUCHED=true
			HAS_RELEVANT_CHANGES=true
			return 0
			;;
		AGENTS.md|README.md|LICENSE|docs/*|.codex/*)
			return 0
			;;
		e2e/maestro/flows/suites/*-suite.yaml)
			E2E_CONTRACT_TOUCHED=true
			HAS_RELEVANT_CHANGES=true
			suite_name="$(basename "$file" .yaml)"
			append_e2e_scope all "$suite_name" "e2e-suite"
			return 0
			;;
		e2e/maestro/flows/*/*)
			E2E_CONTRACT_TOUCHED=true
			HAS_RELEVANT_CHANGES=true
			case "$file" in
				e2e/maestro/flows/auth/*) append_e2e_scope all auth-suite "e2e-flow-auth" ;;
				e2e/maestro/flows/about/*) append_e2e_scope all about-suite "e2e-flow-about" ;;
				e2e/maestro/flows/enrollmentproof/*) append_e2e_scope all enrollmentproof-suite "e2e-flow-enrollmentproof" ;;
				e2e/maestro/flows/evaluations/*) append_e2e_scope all evaluations-suite "e2e-flow-evaluations" ;;
				e2e/maestro/flows/maincore/*) append_e2e_scope all maincore-suite "e2e-flow-maincore" ;;
				e2e/maestro/flows/pensum/*) append_e2e_scope all pensum-suite "e2e-flow-pensum" ;;
				e2e/maestro/flows/record/*) append_e2e_scope all record-suite "e2e-flow-record" ;;
				e2e/maestro/flows/subjects/*) append_e2e_scope all subjects-suite "e2e-flow-subjects" ;;
				e2e/maestro/flows/summary/*) append_e2e_scope all summary-suite "e2e-flow-summary" ;;
				e2e/maestro/flows/wizard/*) append_e2e_scope all wizard-suite "e2e-flow-wizard" ;;
				*) append_e2e_scope all local-certification-suite "e2e-flow-shared" ;;
			esac
			return 0
			;;
		testkit/e2e/*)
			E2E_CONTRACT_TOUCHED=true
			HAS_RELEVANT_CHANGES=true
			append_e2e_scope all local-certification-suite "e2e-contract"
			return 0
			;;
		e2e/scripts/*|e2e/platform/*)
			E2E_CONTRACT_TOUCHED=true
			HAS_RELEVANT_CHANGES=true
			append_e2e_scope all local-certification-suite "e2e-runner"
			return 0
			;;
		mocks/*)
			E2E_CONTRACT_TOUCHED=true
			HAS_RELEVANT_CHANGES=true
			append_e2e_scope_for_mock_path "$file"
			return 0
			;;
		settings.gradle.kts|build.gradle.kts|gradle.properties|gradlew|gradlew.bat|gradle/*)
			while IFS= read -r module; do
				append_runtime_module "$module"
			done < <(kmp_modules)
			append_runtime_module app
			append_runtime_module iosApp
			HAS_RELEVANT_CHANGES=true
			HAS_RELEASE_IMPACT=true
			append_e2e_scope all local-certification-suite "root-build"
			return 0
			;;
		iosApp/Config/Release.xcconfig|iosApp/TuIndiceHost.xcodeproj/project.pbxproj)
			if is_ios_signing_only_config_change "$file"; then
				append_ios_signing_config_validation
				return 0
			fi
			;;
		iosApp/scripts/*)
			CI_CONFIG_TOUCHED=true
			IOS_CI_SCRIPTS_TOUCHED=true
			HAS_RELEVANT_CHANGES=true
			return 0
			;;
		iosApp/*)
			if is_ios_app_test_source_file "$file"; then
				append_changed_test_module iosApp
				HAS_RELEVANT_CHANGES=true
				return 0
			fi

			append_runtime_module iosApp
			HAS_RELEVANT_CHANGES=true
			HAS_RELEASE_IMPACT=true
			append_e2e_scope ios local-certification-suite "ios-host-runtime"
			return 0
			;;
		app/*)
			if is_app_test_source_file "$file"; then
				append_changed_test_module app
				HAS_RELEVANT_CHANGES=true
				return 0
			fi

			append_runtime_module app
			HAS_RELEVANT_CHANGES=true
			HAS_RELEASE_IMPACT=true
			append_e2e_scope android local-certification-suite "android-host-runtime"
			return 0
			;;
	esac

	if module_is_kmp "$top_level"; then
		HAS_RELEVANT_CHANGES=true

		if is_kmp_test_source_file "$top_level" "$file"; then
			append_changed_test_module "$top_level"
			return 0
		fi

		append_runtime_module "$top_level"

		if module_is_runtime "$top_level"; then
			HAS_RELEASE_IMPACT=true
			if is_kmp_runtime_source_or_build_file "$top_level" "$file"; then
				if [[ "$top_level" == "base" || "$top_level" == "persistence" || "$top_level" == "academiccore" || "$top_level" == "maincore" ]]; then
					append_e2e_scope_for_shared_module_path "$top_level" "$file"
				else
					mark_e2e_suite_for_module "$top_level"
				fi
			fi
		fi
	fi
}

if [[ -n "${DETECT_CHANGED_APP_CHANGED_FILES_FILE:-}" ]]; then
	cp "$DETECT_CHANGED_APP_CHANGED_FILES_FILE" "$CHANGED_FILES_FILE"
else
	changed_files_between_refs "$BEFORE_SHA" "$AFTER_SHA" >"$CHANGED_FILES_FILE"
fi

while IFS= read -r changed_file; do
	[[ -n "$changed_file" ]] || continue
	classify_changed_file "$changed_file"
done <"$CHANGED_FILES_FILE"

if [[ "$APP_VERSION_TOUCHED" == "true" ]]; then
	base_version="$(get_app_version_property_at_git_ref versionName "$BEFORE_SHA")"
	head_version="$(get_app_version_property_at_git_ref versionName "$AFTER_SHA")"
	base_android_code="$(get_app_version_property_at_git_ref androidVersionCode "$BEFORE_SHA")"
	head_android_code="$(get_app_version_property_at_git_ref androidVersionCode "$AFTER_SHA")"
	base_ios_build="$(get_app_version_property_at_git_ref iosBuildNumber "$BEFORE_SHA")"
	head_ios_build="$(get_app_version_property_at_git_ref iosBuildNumber "$AFTER_SHA")"

	if [[ "$base_version" != "$head_version" ]]; then
		APP_VERSION_NAME_CHANGED=true
	fi
	if [[ "$base_android_code" != "$head_android_code" ]]; then
		ANDROID_VERSION_CODE_CHANGED=true
	fi
	if [[ "$base_ios_build" != "$head_ios_build" ]]; then
		IOS_BUILD_NUMBER_CHANGED=true
	fi

	if [[ "$APP_VERSION_NAME_CHANGED" == "true" || "$ANDROID_VERSION_CODE_CHANGED" == "true" || "$IOS_BUILD_NUMBER_CHANGED" == "true" ]]; then
		APP_VERSION_CHANGED=true
	fi
fi

if [[ "$APP_VERSION_NAME_CHANGED" == "true" || "$ANDROID_VERSION_CODE_CHANGED" == "true" ]]; then
	append_runtime_module app
fi

if [[ "$APP_VERSION_NAME_CHANGED" == "true" || "$IOS_BUILD_NUMBER_CHANGED" == "true" ]]; then
	append_runtime_module iosApp
fi

if [[ "$HAS_RELEASE_IMPACT" == "true" && "$APP_VERSION_CHANGED" != "true" ]]; then
	append_unique_line "$MISSING_VERSION_BUMP_FILE" app
fi

if [[ "$APP_VERSION_CHANGED" == "true" ]]; then
	HAS_RELEVANT_CHANGES=true
fi

sort_file_if_present "$IMPACTED_MODULES_FILE"
sort_file_if_present "$RELEASE_IMPACTED_MODULES_FILE"
sort_file_if_present "$MISSING_VERSION_BUMP_FILE"
sort_file_if_present "$E2E_SUITES_FILE"

while IFS= read -r module; do
	[[ -n "$module" ]] || continue
	case "$module" in
		app)
			append_unique_line "$ANDROID_TASKS_FILE" ":app:testDebugUnitTest"
			if [[ "$HAS_RELEASE_IMPACT" == "true" || "$APP_VERSION_CHANGED" == "true" ]]; then
				append_unique_line "$ANDROID_TASKS_FILE" ":app:bundleRelease"
			fi
			;;
		iosApp)
			if [[ "$HAS_RELEASE_IMPACT" == "true" || "$APP_VERSION_CHANGED" == "true" ]]; then
				append_unique_line "$IOS_TASKS_FILE" "verifyIosHostBuildDeviceRelease"
			else
				append_unique_line "$IOS_TASKS_FILE" "verifyIosHostTypecheck"
			fi
			;;
		*)
			if module_is_kmp "$module"; then
				append_unique_line "$ANDROID_TASKS_FILE" ":${module}:compileAndroidMain"
				if [[ "$module" != "testkit" ]]; then
					append_unique_line "$ANDROID_TASKS_FILE" ":${module}:testAndroidHostTest"
				fi
				append_unique_line "$IOS_TASKS_FILE" ":${module}:compileKotlinIosSimulatorArm64"
				if [[ "$module" != "testkit" ]]; then
					append_unique_line "$IOS_TASKS_FILE" ":${module}:iosSimulatorArm64Test"
				fi
			fi
			;;
	esac
done <"$IMPACTED_MODULES_FILE"

if [[ "$E2E_CONTRACT_TOUCHED" == "true" ]]; then
	append_unique_line "$ANDROID_TASKS_FILE" "verifyE2eContract"
fi

if [[ "$CI_CONFIG_TOUCHED" == "true" ]]; then
	append_unique_line "$ANDROID_TASKS_FILE" "verifyAppVersionSync"
fi

sort_file_if_present "$ANDROID_TASKS_FILE"
sort_file_if_present "$IOS_TASKS_FILE"
sort_file_if_present "$E2E_SCOPE_FILE"

ANDROID_SCOPE_HAS_LOCAL_CERTIFICATION=false
IOS_SCOPE_HAS_LOCAL_CERTIFICATION=false
if grep -q '^android,local-certification-suite,' "$E2E_SCOPE_FILE"; then
	ANDROID_SCOPE_HAS_LOCAL_CERTIFICATION=true
fi
if grep -q '^ios,local-certification-suite,' "$E2E_SCOPE_FILE"; then
	IOS_SCOPE_HAS_LOCAL_CERTIFICATION=true
fi

while IFS=, read -r platform suite reason; do
	[[ -n "$platform" && -n "$suite" ]] || continue
	case "$platform" in
		android)
			if [[ "$ANDROID_SCOPE_HAS_LOCAL_CERTIFICATION" == "true" && "$suite" != "local-certification-suite" ]]; then
				continue
			fi
			append_unique_line "$E2E_ANDROID_CONTEXTS_FILE" "local-e2e/android/${suite}"
			;;
		ios)
			if [[ "$IOS_SCOPE_HAS_LOCAL_CERTIFICATION" == "true" && "$suite" != "local-certification-suite" ]]; then
				continue
			fi
			append_unique_line "$E2E_IOS_CONTEXTS_FILE" "local-e2e/ios/${suite}"
			;;
		*)
			die "Unsupported E2E platform in scope file: ${platform}"
			;;
	esac
done <"$E2E_SCOPE_FILE"

sort_file_if_present "$E2E_ANDROID_CONTEXTS_FILE"
sort_file_if_present "$E2E_IOS_CONTEXTS_FILE"

info "Impacted modules: $(file_to_csv "$IMPACTED_MODULES_FILE" || true)"
info "Release impacted modules: $(file_to_csv "$RELEASE_IMPACTED_MODULES_FILE" || true)"
info "App version touched: ${APP_VERSION_TOUCHED}"
info "App version changed: ${APP_VERSION_CHANGED}"
info "Missing version bump: $(file_to_csv "$MISSING_VERSION_BUMP_FILE" || true)"
info "CI/CD configuration touched: ${CI_CONFIG_TOUCHED}"
info "iOS CI scripts touched: ${IOS_CI_SCRIPTS_TOUCHED}"
info "E2E suites requiring local certification: $(file_to_csv "$E2E_SUITES_FILE" || true)"
info "E2E scope: $(file_to_csv "$E2E_SCOPE_FILE" || true)"
info "E2E Android contexts: $(file_to_csv "$E2E_ANDROID_CONTEXTS_FILE" || true)"
info "E2E iOS contexts: $(file_to_csv "$E2E_IOS_CONTEXTS_FILE" || true)"
info "Android Gradle tasks: $(file_to_space_list "$ANDROID_TASKS_FILE" || true)"
info "iOS Gradle tasks: $(file_to_space_list "$IOS_TASKS_FILE" || true)"

if [[ -n "${GITHUB_OUTPUT:-}" ]]; then
	{
		printf 'state_dir=%s\n' "$STATE_DIR"
		printf 'changed_files_file=%s\n' "$CHANGED_FILES_FILE"
		printf 'impacted_modules_file=%s\n' "$IMPACTED_MODULES_FILE"
		printf 'impacted_modules_csv=%s\n' "$(file_to_csv "$IMPACTED_MODULES_FILE" || true)"
		printf 'release_impacted_modules_file=%s\n' "$RELEASE_IMPACTED_MODULES_FILE"
		printf 'release_impacted_modules_csv=%s\n' "$(file_to_csv "$RELEASE_IMPACTED_MODULES_FILE" || true)"
		printf 'missing_version_bump_file=%s\n' "$MISSING_VERSION_BUMP_FILE"
		printf 'missing_version_bump_csv=%s\n' "$(file_to_csv "$MISSING_VERSION_BUMP_FILE" || true)"
		printf 'android_tasks=%s\n' "$(file_to_space_list "$ANDROID_TASKS_FILE" || true)"
		printf 'ios_tasks=%s\n' "$(file_to_space_list "$IOS_TASKS_FILE" || true)"
		printf 'android_tasks_file=%s\n' "$ANDROID_TASKS_FILE"
		printf 'ios_tasks_file=%s\n' "$IOS_TASKS_FILE"
		printf 'app_version_touched=%s\n' "$APP_VERSION_TOUCHED"
		printf 'app_version_changed=%s\n' "$APP_VERSION_CHANGED"
		printf 'ci_config_touched=%s\n' "$CI_CONFIG_TOUCHED"
		printf 'ios_ci_scripts_touched=%s\n' "$IOS_CI_SCRIPTS_TOUCHED"
		printf 'e2e_contract_touched=%s\n' "$E2E_CONTRACT_TOUCHED"
		printf 'requires_e2e_certification=%s\n' "$REQUIRES_E2E_CERTIFICATION"
		printf 'e2e_suites_file=%s\n' "$E2E_SUITES_FILE"
		printf 'e2e_suites_csv=%s\n' "$(file_to_csv "$E2E_SUITES_FILE" || true)"
		printf 'e2e_scope_file=%s\n' "$E2E_SCOPE_FILE"
		printf 'e2e_scope_csv=%s\n' "$(file_to_csv "$E2E_SCOPE_FILE" || true)"
		printf 'e2e_android_contexts_file=%s\n' "$E2E_ANDROID_CONTEXTS_FILE"
		printf 'e2e_ios_contexts_file=%s\n' "$E2E_IOS_CONTEXTS_FILE"
		printf 'has_relevant_changes=%s\n' "$HAS_RELEVANT_CHANGES"
		printf 'has_release_impact=%s\n' "$HAS_RELEASE_IMPACT"
	} >>"$GITHUB_OUTPUT"
fi
