#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=.github/scripts/common.sh
source "${SCRIPT_DIR}/common.sh"

e2e_load_source_sets

BEFORE_SHA="${1:-${GIT_BEFORE_SHA:-}}"
AFTER_SHA="${2:-${GIT_AFTER_SHA:-${GITHUB_SHA:-HEAD}}}"
STATE_DIR="${STATE_DIR:-$(mktemp -d "${RUNNER_TEMP:-/tmp}/tuindice-changes.XXXXXX")}"

CHANGED_FILES_FILE="${CHANGED_FILES_FILE:-${STATE_DIR}/changed-files.txt}"
IMPACTED_MODULES_FILE="${IMPACTED_MODULES_FILE:-${STATE_DIR}/impacted-modules.txt}"
RELEASE_IMPACTED_MODULES_FILE="${RELEASE_IMPACTED_MODULES_FILE:-${STATE_DIR}/release-impacted-modules.txt}"
MISSING_VERSION_BUMP_FILE="${MISSING_VERSION_BUMP_FILE:-${STATE_DIR}/missing-version-bump.txt}"
ANDROID_TASKS_FILE="${ANDROID_TASKS_FILE:-${STATE_DIR}/android-gradle-tasks.txt}"
IOS_TASKS_FILE="${IOS_TASKS_FILE:-${STATE_DIR}/ios-gradle-tasks.txt}"
IOS_TEST_TASKS_FILE="${IOS_TEST_TASKS_FILE:-${STATE_DIR}/ios-test-gradle-tasks.txt}"
IOS_HOST_TASKS_FILE="${IOS_HOST_TASKS_FILE:-${STATE_DIR}/ios-host-gradle-tasks.txt}"
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
: >"$IOS_TEST_TASKS_FILE"
: >"$IOS_HOST_TASKS_FILE"
: >"$E2E_SUITES_FILE"
: >"$E2E_SCOPE_FILE"
: >"$E2E_ANDROID_CONTEXTS_FILE"
: >"$E2E_IOS_CONTEXTS_FILE"

APP_VERSION_TOUCHED=false
APP_VERSION_CHANGED=false
APP_VERSION_NAME_CHANGED=false
ANDROID_VERSION_CODE_CHANGED=false
IOS_BUILD_NUMBER_CHANGED=false
APP_RELEASE_BUILD_NUMBERS_CHANGED=false
CI_CONFIG_TOUCHED=false
IOS_CI_SCRIPTS_TOUCHED=false
MODULE_GRAPH_TOUCHED=false
E2E_CONTRACT_TOUCHED=false
DETEKT_CONFIG_TOUCHED=false
SEMGREP_CONFIG_TOUCHED=false
HAS_RELEVANT_CHANGES=false
HAS_RELEASE_IMPACT=false
REQUIRES_E2E_CERTIFICATION=false
PROCESSED_RUNTIME_MODULES=$'\n'
PROCESSED_E2E_SUITES=$'\n'
PROCESSED_E2E_SCOPES=$'\n'
IOS_UITEST_BUILD_REQUIRED=false

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
	local reason="${2:-changed-runtime}"

	if [[ "$platform" == "all" ]]; then
		append_e2e_scope android "$reason"
		append_e2e_scope ios "$reason"
		return 0
	fi

	case "$platform" in
		android|ios)
			;;
		none)
			return 0
			;;
		*)
			die "Unsupported E2E platform scope '${platform}'."
			;;
	esac

	if ! value_seen_in_newline_set "$PROCESSED_E2E_SCOPES" "$platform"; then
		PROCESSED_E2E_SCOPES="${PROCESSED_E2E_SCOPES}${platform}"$'\n'
		append_unique_line "$E2E_SCOPE_FILE" "${platform},${E2E_SUITE_ID},${reason}"
	fi

	append_e2e_suite "$E2E_SUITE_ID"
	REQUIRES_E2E_CERTIFICATION=true
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
		app/src/test/*|app/src/test[A-Z]*/*|app/src/androidTest/*|app/src/androidTest[A-Z]*/*)
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

# A runtime source or build file of a KMP module is one the E2E fingerprint reads (the source-set lists live in
# common.sh and feed the fingerprint script too).
is_kmp_runtime_source_or_build_file() {
	[[ "$(e2e_platform_for_kmp_file "$1" "$2")" != "none" ]]
}

append_ios_test_task() {
	append_unique_line "$IOS_TASKS_FILE" "$1"
	append_unique_line "$IOS_TEST_TASKS_FILE" "$1"
}

append_ios_host_task() {
	append_unique_line "$IOS_TASKS_FILE" "$1"
	append_unique_line "$IOS_HOST_TASKS_FILE" "$1"
}

append_ios_signing_config_validation() {
	CI_CONFIG_TOUCHED=true
	HAS_RELEVANT_CHANGES=true
	append_ios_host_task "verifyIosHostBuildDeviceRelease"
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

classify_changed_file() {
	local file="$1"
	local top_level="${file%%/*}"

	case "$file" in
		.DS_Store|*/.DS_Store)
			return 0
			;;
		.github/scripts/materialize-firebase-configs.sh|.github/scripts/sync-app-version.sh)
			# e2e/scripts/ios/build.sh runs both while building the app and the UI test bundle, so they are part of
			# what an iOS scenario executes (the iOS fingerprint reads them) and of the UI test build job.
			CI_CONFIG_TOUCHED=true
			HAS_RELEVANT_CHANGES=true
			IOS_UITEST_BUILD_REQUIRED=true
			append_e2e_scope ios "e2e-ios-build-scripts"
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
		config/detekt/*|.editorconfig|*/detekt-baseline.xml)
			# Solo afecta análisis estático: corre detekt completo sin marcar
			# impacto de runtime ni suites E2E.
			DETEKT_CONFIG_TOUCHED=true
			HAS_RELEVANT_CHANGES=true
			return 0
			;;
		config/semgrep/*|.semgrepignore|scripts/semgrep-architecture.sh)
			# Solo afecta análisis estático: corre el ruleset semgrep de
			# arquitectura sin marcar impacto de runtime ni suites E2E.
			SEMGREP_CONFIG_TOUCHED=true
			HAS_RELEVANT_CHANGES=true
			return 0
			;;
		e2e/catalog/*)
			E2E_CONTRACT_TOUCHED=true
			HAS_RELEVANT_CHANGES=true
			IOS_UITEST_BUILD_REQUIRED=true
			append_e2e_scope all "e2e-catalog"
			return 0
			;;
		e2e/scripts/shared/*)
			E2E_CONTRACT_TOUCHED=true
			HAS_RELEVANT_CHANGES=true
			append_e2e_scope all "e2e-harness-shared"
			return 0
			;;
		e2e/scripts/android/*|e2e/toolchain/android.lock)
			E2E_CONTRACT_TOUCHED=true
			HAS_RELEVANT_CHANGES=true
			append_e2e_scope android "e2e-harness-android"
			return 0
			;;
		e2e/scripts/ios/*|e2e/toolchain/ios.lock)
			E2E_CONTRACT_TOUCHED=true
			HAS_RELEVANT_CHANGES=true
			# build.sh is what the ios-uitest-preflight job runs.
			if [[ "$file" == "e2e/scripts/ios/build.sh" ]]; then
				IOS_UITEST_BUILD_REQUIRED=true
			fi
			append_e2e_scope ios "e2e-harness-ios"
			return 0
			;;
		e2e/tools/*|e2e/platform/*|e2e/*.md)
			# Verifiers, their tests and the platform notes sit outside the fingerprint:
			# they cannot change what a scenario does, and verifyE2eContract re-runs them.
			E2E_CONTRACT_TOUCHED=true
			HAS_RELEVANT_CHANGES=true
			return 0
			;;
		e2e/maestro/*|e2e/scripts/*)
			# Legacy Maestro flows and the pre-v5 scripts under e2e/scripts: the v5 fingerprint
			# does not read them. verifyE2eContract still validates them until they are removed.
			E2E_CONTRACT_TOUCHED=true
			HAS_RELEVANT_CHANGES=true
			return 0
			;;
		testkit/e2e/validate-*.sh|testkit/e2e/*.md)
			# Validators and docs cannot change what a scenario does at runtime;
			# verifyE2eContract re-runs them on every preflight regardless, so
			# requiring evidence here would force a rotation that could not
			# have changed its outcome.
			E2E_CONTRACT_TOUCHED=true
			HAS_RELEVANT_CHANGES=true
			return 0
			;;
		testkit/e2e/*)
			# Other files here are fixture contracts and legacy catalogs. They stay in scope
			# as before; new files default to this branch.
			E2E_CONTRACT_TOUCHED=true
			HAS_RELEVANT_CHANGES=true
			append_e2e_scope all "e2e-contract"
			return 0
			;;
		mocks/*)
			E2E_CONTRACT_TOUCHED=true
			HAS_RELEVANT_CHANGES=true
			append_e2e_scope all "mocks"
			return 0
			;;
		scenariorunner/*)
			# The Android test APK: instrumentation runner of the scenarios. It builds with
			# :app, so assembling it is the Android check of the change.
			E2E_CONTRACT_TOUCHED=true
			HAS_RELEVANT_CHANGES=true
			append_runtime_module scenariorunner
			append_unique_line "$ANDROID_TASKS_FILE" ":scenariorunner:assembleDebug"
			append_e2e_scope android "e2e-android-runner"
			return 0
			;;
		iosApp/UITests/*|iosApp/Config/UITests.xcconfig)
			# Before the iosApp test-source rule below, which would swallow the UI test
			# target as a unit test.
			E2E_CONTRACT_TOUCHED=true
			HAS_RELEVANT_CHANGES=true
			IOS_UITEST_BUILD_REQUIRED=true
			append_e2e_scope ios "e2e-ios-uitests"
			return 0
			;;
		gradle/e2e-tasks.gradle.kts)
			# The registration of the e2e*, verifyE2e* and related tasks, applied from the root build file. It sits
			# outside the E2E fingerprint on purpose: editing a verification task cannot change what a scenario does,
			# and the contract checks re-run on it (the coverage verifier of verifyE2eHarness fails if the root file
			# registers an E2E task or stops applying this script).
			E2E_CONTRACT_TOUCHED=true
			CI_CONFIG_TOUCHED=true
			HAS_RELEVANT_CHANGES=true
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
			MODULE_GRAPH_TOUCHED=true
			DETEKT_CONFIG_TOUCHED=true
			# Every file of this group is read by the fingerprint of both platforms (compiler arguments, classpath,
			# Gradle distribution and daemon JVM); `gradle/*` files it does not read cannot change a scenario.
			case "$file" in
				settings.gradle.kts|build.gradle.kts|gradle.properties|gradlew|gradlew.bat|gradle/libs.versions.toml|gradle/gradle-daemon-jvm.properties|gradle/wrapper/*)
					append_e2e_scope all "root-build"
					;;
			esac
			return 0
			;;
		scripts/module-graph.txt|scripts/validate-module-graph.sh)
			CI_CONFIG_TOUCHED=true
			MODULE_GRAPH_TOUCHED=true
			HAS_RELEVANT_CHANGES=true
			return 0
			;;
		iosApp/Config/Release.xcconfig|iosApp/TuIndiceHost.xcodeproj/project.pbxproj)
			if is_ios_signing_only_config_change "$file"; then
				append_ios_signing_config_validation
				return 0
			fi

			# Anything beyond signing is host runtime: a Swift file added to the
			# project, OTHER_LDFLAGS, entitlements, the bundle id. `;;` would end
			# the case here rather than fall through to iosApp/*, and the trailing
			# fallback only accepts KMP modules, which iosApp is not — so the file
			# would produce no scope at all and preflight would pass green.
			append_runtime_module iosApp
			HAS_RELEVANT_CHANGES=true
			HAS_RELEASE_IMPACT=true
			# The project file defines the UI test target as well as the host.
			case "$file" in
				iosApp/TuIndiceHost.xcodeproj/*) IOS_UITEST_BUILD_REQUIRED=true ;;
			esac
			append_e2e_scope ios "ios-host-runtime"
			return 0
			;;
		iosApp/scripts/ci-build-ios-host.sh)
			# The ci-* scripts run in CI only; the E2E build never calls them, so the fingerprint leaves them out.
			CI_CONFIG_TOUCHED=true
			IOS_CI_SCRIPTS_TOUCHED=true
			HAS_RELEVANT_CHANGES=true
			append_ios_host_task "verifyIosHostBuildDeviceRelease"
			return 0
			;;
		iosApp/scripts/ci-typecheck-ios-host.sh)
			CI_CONFIG_TOUCHED=true
			IOS_CI_SCRIPTS_TOUCHED=true
			HAS_RELEVANT_CHANGES=true
			append_ios_host_task "verifyIosHostTypecheck"
			return 0
			;;
		iosApp/scripts/ci-*)
			CI_CONFIG_TOUCHED=true
			IOS_CI_SCRIPTS_TOUCHED=true
			HAS_RELEVANT_CHANGES=true
			return 0
			;;
		iosApp/scripts/*)
			# build-kmp-framework.sh, build-scenario-kit.sh, add-ui-test-target.rb, verify-ui-test-target.sh: how the
			# framework and the UI test bundle are built, so they are in the iOS fingerprint and in the UI test build job.
			CI_CONFIG_TOUCHED=true
			IOS_CI_SCRIPTS_TOUCHED=true
			HAS_RELEVANT_CHANGES=true
			IOS_UITEST_BUILD_REQUIRED=true
			if [[ "$file" == "iosApp/scripts/build-kmp-framework.sh" ]]; then
				append_ios_host_task "verifyIosHostBuildDeviceRelease"
			fi
			append_e2e_scope ios "e2e-ios-build-scripts"
			return 0
			;;
		iosApp/*)
			if is_ios_app_test_source_file "$file"; then
				append_changed_test_module iosApp
				HAS_RELEVANT_CHANGES=true
				return 0
			fi

			# The scheme and the Podfile decide how the UI test target builds.
			case "$file" in
				iosApp/TuIndiceHost.xcodeproj/*|iosApp/Podfile*) IOS_UITEST_BUILD_REQUIRED=true ;;
			esac

			append_runtime_module iosApp
			HAS_RELEVANT_CHANGES=true
			HAS_RELEASE_IMPACT=true
			append_e2e_scope ios "ios-host-runtime"
			return 0
			;;
		app/.gitignore|iosApp/.gitignore)
			return 0
			;;
		app/proguard-rules.pro)
			# Only the release build minifies: it ships (release impact and version bump) but no debug scenario sees it.
			append_runtime_module app
			HAS_RELEVANT_CHANGES=true
			HAS_RELEASE_IMPACT=true
			return 0
			;;
		app/*)
			if is_app_test_source_file "$file"; then
				append_changed_test_module app
				HAS_RELEVANT_CHANGES=true
				return 0
			fi

			if [[ "$file" == "app/build.gradle.kts" ]]; then
				MODULE_GRAPH_TOUCHED=true
			fi

			append_runtime_module app
			HAS_RELEVANT_CHANGES=true
			HAS_RELEASE_IMPACT=true
			append_e2e_scope android "android-host-runtime"
			return 0
			;;
	esac

	if ! module_is_kmp "$top_level"; then
		# Nothing matched. Defaulting to "no scope" is how an unclassified path
		# reaches production behind a preflight that passed without validating
		# anything, so treat it as relevant and say so instead of staying silent.
		warn "Unclassified path '${file}': no scope rule matched. Treating it as a relevant change; add an explicit rule if that is wrong."
		HAS_RELEVANT_CHANGES=true
		return 0
	fi

	HAS_RELEVANT_CHANGES=true

	if is_kmp_test_source_file "$top_level" "$file"; then
		append_changed_test_module "$top_level"
		return 0
	fi

	if [[ "$file" == "${top_level}/build.gradle.kts" ]]; then
		MODULE_GRAPH_TOUCHED=true
	fi

	append_runtime_module "$top_level"

	if module_is_runtime "$top_level"; then
		HAS_RELEASE_IMPACT=true
		if is_kmp_runtime_source_or_build_file "$top_level" "$file"; then
			append_e2e_scope "$(e2e_platform_for_kmp_file "$top_level" "$file")" "module-runtime"
		fi
	else
		# The scenario kit and catalog never reach a release build, but the scenarios run
		# through them: their sources are part of the evidence of the platform they compile for.
		case "$top_level" in
			scenariokit|scenarios)
				E2E_CONTRACT_TOUCHED=true
				e2e_platform="$(e2e_platform_for_kmp_file "$top_level" "$file")"
				append_e2e_scope "$e2e_platform" "e2e-${top_level}"
				# The iOS UI test bundle links ScenarioKit: the protocol its Swift driver implements.
				if [[ "$top_level" == "scenariokit" && ( "$e2e_platform" == "all" || "$e2e_platform" == "ios" ) ]]; then
					IOS_UITEST_BUILD_REQUIRED=true
				fi
				;;
		esac
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
	if [[ "$ANDROID_VERSION_CODE_CHANGED" == "true" && "$IOS_BUILD_NUMBER_CHANGED" == "true" ]]; then
		APP_RELEASE_BUILD_NUMBERS_CHANGED=true
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

if [[ "$HAS_RELEASE_IMPACT" == "true" || "$APP_VERSION_CHANGED" == "true" ]]; then
	# Diff-based half of the release version invariant: all three values must
	# move relative to the PR base. The history-based half lives in
	# validate-app-version.sh, which rejects a versionName already published as
	# `app-<versionName>`. They are complementary, not redundant: this one
	# cannot see release history, and the tag check cannot see a versionName
	# that was reused without ever having been tagged.
	if [[ "$APP_VERSION_NAME_CHANGED" != "true" ]]; then
		append_unique_line "$MISSING_VERSION_BUMP_FILE" versionName
	fi
	if [[ "$ANDROID_VERSION_CODE_CHANGED" != "true" ]]; then
		append_unique_line "$MISSING_VERSION_BUMP_FILE" androidVersionCode
	fi
	if [[ "$IOS_BUILD_NUMBER_CHANGED" != "true" ]]; then
		append_unique_line "$MISSING_VERSION_BUMP_FILE" iosBuildNumber
	fi
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
			append_unique_line "$ANDROID_TASKS_FILE" ":app:detekt"
			append_unique_line "$ANDROID_TASKS_FILE" ":app:testDebugUnitTest"
			if [[ "$HAS_RELEASE_IMPACT" == "true" || "$APP_VERSION_CHANGED" == "true" ]]; then
				append_unique_line "$ANDROID_TASKS_FILE" ":app:bundleRelease"
			fi
			;;
		iosApp)
			if [[ "$HAS_RELEASE_IMPACT" == "true" || "$APP_VERSION_CHANGED" == "true" ]]; then
				append_ios_host_task "verifyIosHostBuildDeviceRelease"
			else
				append_ios_host_task "verifyIosHostTypecheck"
			fi
			;;
		*)
			if module_is_kmp "$module"; then
				append_unique_line "$ANDROID_TASKS_FILE" ":${module}:compileAndroidMain"
				append_unique_line "$ANDROID_TASKS_FILE" ":${module}:detekt"
				if [[ "$module" != "testkit" ]]; then
					append_unique_line "$ANDROID_TASKS_FILE" ":${module}:testAndroidHostTest"
				fi
				append_ios_test_task ":${module}:compileKotlinIosSimulatorArm64"
				if [[ "$module" != "testkit" ]]; then
					append_ios_test_task ":${module}:iosSimulatorArm64Test"
				fi
			fi
			;;
	esac
done <"$IMPACTED_MODULES_FILE"

if [[ "$E2E_CONTRACT_TOUCHED" == "true" ]]; then
	append_unique_line "$ANDROID_TASKS_FILE" "verifyE2eContract"
fi

# Touching either version source is the only way to desynchronize the generated
# Version.xcconfig from app-version.properties. A hand edit that leaves every
# property equal keeps app_version_changed=false, so preflight-production.sh
# never reaches validate-app-version.sh; scheduling the task here is what makes
# that diff fail instead of shipping a stale xcconfig.
if [[ "$CI_CONFIG_TOUCHED" == "true" || "$APP_VERSION_TOUCHED" == "true" ]]; then
	append_unique_line "$ANDROID_TASKS_FILE" "verifyAppVersionSync"
fi

if [[ "$MODULE_GRAPH_TOUCHED" == "true" ]]; then
	append_unique_line "$ANDROID_TASKS_FILE" "verifyModuleGraph"
fi

if [[ "$DETEKT_CONFIG_TOUCHED" == "true" ]]; then
	append_unique_line "$ANDROID_TASKS_FILE" "detekt"
fi

# Semgrep valida arquitectura repo-wide: corre cuando cambió código de algún
# módulo o cuando cambió la propia configuración del ruleset.
SEMGREP_REQUIRED=false
if [[ "$SEMGREP_CONFIG_TOUCHED" == "true" ]] || [[ -s "$IMPACTED_MODULES_FILE" ]]; then
	SEMGREP_REQUIRED=true
fi

sort_file_if_present "$ANDROID_TASKS_FILE"
sort_file_if_present "$IOS_TASKS_FILE"
sort_file_if_present "$IOS_TEST_TASKS_FILE"
sort_file_if_present "$IOS_HOST_TASKS_FILE"
sort_file_if_present "$E2E_SCOPE_FILE"

# The context files hold the one status each required platform must carry, as defined once in common.sh.
while IFS=, read -r platform suite reason; do
	[[ -n "$platform" && -n "$suite" ]] || continue
	case "$platform" in
		android)
			append_unique_line "$E2E_ANDROID_CONTEXTS_FILE" "$(e2e_status_context android)"
			;;
		ios)
			append_unique_line "$E2E_IOS_CONTEXTS_FILE" "$(e2e_status_context ios)"
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
info "App release build numbers changed: ${APP_RELEASE_BUILD_NUMBERS_CHANGED}"
info "Missing version bump: $(file_to_csv "$MISSING_VERSION_BUMP_FILE" || true)"
info "CI/CD configuration touched: ${CI_CONFIG_TOUCHED}"
info "iOS CI scripts touched: ${IOS_CI_SCRIPTS_TOUCHED}"
info "Module graph touched: ${MODULE_GRAPH_TOUCHED}"
info "Detekt config touched: ${DETEKT_CONFIG_TOUCHED}"
info "Semgrep config touched: ${SEMGREP_CONFIG_TOUCHED}"
info "Semgrep required: ${SEMGREP_REQUIRED}"
info "iOS UI test build required: ${IOS_UITEST_BUILD_REQUIRED}"
info "E2E suites requiring local certification:$(file_to_csv "$E2E_SUITES_FILE" || true)"
info "E2E scope: $(file_to_csv "$E2E_SCOPE_FILE" || true)"
info "E2E Android contexts: $(file_to_csv "$E2E_ANDROID_CONTEXTS_FILE" || true)"
info "E2E iOS contexts: $(file_to_csv "$E2E_IOS_CONTEXTS_FILE" || true)"
info "Android Gradle tasks: $(file_to_space_list "$ANDROID_TASKS_FILE" || true)"
info "iOS Gradle tasks: $(file_to_space_list "$IOS_TASKS_FILE" || true)"
info "iOS test Gradle tasks: $(file_to_space_list "$IOS_TEST_TASKS_FILE" || true)"
info "iOS host Gradle tasks: $(file_to_space_list "$IOS_HOST_TASKS_FILE" || true)"

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
		printf 'ios_test_tasks=%s\n' "$(file_to_space_list "$IOS_TEST_TASKS_FILE" || true)"
		printf 'ios_host_tasks=%s\n' "$(file_to_space_list "$IOS_HOST_TASKS_FILE" || true)"
		printf 'android_tasks_file=%s\n' "$ANDROID_TASKS_FILE"
		printf 'ios_tasks_file=%s\n' "$IOS_TASKS_FILE"
		printf 'ios_test_tasks_file=%s\n' "$IOS_TEST_TASKS_FILE"
		printf 'ios_host_tasks_file=%s\n' "$IOS_HOST_TASKS_FILE"
		printf 'app_version_touched=%s\n' "$APP_VERSION_TOUCHED"
		printf 'app_version_changed=%s\n' "$APP_VERSION_CHANGED"
		printf 'app_release_build_numbers_changed=%s\n' "$APP_RELEASE_BUILD_NUMBERS_CHANGED"
		printf 'ci_config_touched=%s\n' "$CI_CONFIG_TOUCHED"
		printf 'ios_ci_scripts_touched=%s\n' "$IOS_CI_SCRIPTS_TOUCHED"
		printf 'module_graph_touched=%s\n' "$MODULE_GRAPH_TOUCHED"
		printf 'e2e_contract_touched=%s\n' "$E2E_CONTRACT_TOUCHED"
		printf 'ios_uitest_build_required=%s\n' "$IOS_UITEST_BUILD_REQUIRED"
		printf 'requires_e2e_certification=%s\n' "$REQUIRES_E2E_CERTIFICATION"
		printf 'e2e_suites_file=%s\n' "$E2E_SUITES_FILE"
		printf 'e2e_suites_csv=%s\n' "$(file_to_csv "$E2E_SUITES_FILE" || true)"
		printf 'e2e_scope_file=%s\n' "$E2E_SCOPE_FILE"
		printf 'e2e_scope_csv=%s\n' "$(file_to_csv "$E2E_SCOPE_FILE" || true)"
		printf 'e2e_android_contexts_file=%s\n' "$E2E_ANDROID_CONTEXTS_FILE"
		printf 'e2e_ios_contexts_file=%s\n' "$E2E_IOS_CONTEXTS_FILE"
		printf 'semgrep_config_touched=%s\n' "$SEMGREP_CONFIG_TOUCHED"
		printf 'semgrep_required=%s\n' "$SEMGREP_REQUIRED"
		printf 'has_relevant_changes=%s\n' "$HAS_RELEVANT_CHANGES"
		printf 'has_release_impact=%s\n' "$HAS_RELEASE_IMPACT"
	} >>"$GITHUB_OUTPUT"
fi
