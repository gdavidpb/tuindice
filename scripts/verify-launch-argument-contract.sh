#!/usr/bin/env bash
# Validates the debug launch-argument contract.
#
# Source of truth: maincore/.../debug/DebugLaunchArguments.kt declares every
# TUINDICE_E2E_* key once. Rules encoded here:
# 1. No TUINDICE_E2E_* literal in any .kt or .swift file outside that definition,
#    test source sets and the iOS UI-test runner (iosApp/UITests, whose literals
#    are checked by rule 2 instead); everything else reads the constants.
# 2. Every TUINDICE_E2E_* literal in e2e/, testkit/e2e/, .codex/ and iosApp/UITests/
#    is declared in the definition. No file is skipped for its size; binary files are not text.
# 3. The iOS host reads the process environment, the process arguments and
#    UserDefaults (ProcessInfo, CommandLine.arguments, getenv, reads of UserDefaults.standard)
#    only inside an #if DEBUG block, so release builds take no input from launch.
#    A condition with `!` or `||` is not a DEBUG frame (`#if !DEBUG` is one only in its #else).
# 4. Kotlin for iOS (iosMain and the other native source sets) never reads the process
#    environment, arguments or user defaults: it has no #if, so the host passes the values in.
# 5. The constants of DebugLaunchArguments are used only from debug sources, the iOS bootstrap
#    that applies them, the :scenarios module and test source sets, never from code that ships.
#
# Test seam: LAUNCH_CONTRACT_ROOT (the tree to check; default: the repository of this script).

set -euo pipefail

ROOT_DIR="${LAUNCH_CONTRACT_ROOT:-$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)}"
DEFINITION="${ROOT_DIR}/maincore/src/commonMain/kotlin/com/gdavidpb/tuindice/debug/DebugLaunchArguments.kt"
IOS_HOST_DIR="${ROOT_DIR}/iosApp/Sources"
KEY_PATTERN='TUINDICE_E2E_[A-Z_]+'
# Reads of UserDefaults.standard (the argument domain is part of it), or any alias of it: writes such as the locale
# defaults the host sets in every build are not launch input.
SWIFT_LAUNCH_INPUT='ProcessInfo|CommandLine\.arguments|getenv\(|UserDefaults\.standard\.(string|bool|integer|double|float|object|array|dictionary|stringArray|data|url|value|dictionaryRepresentation)|UserDefaults\.standard([^.A-Za-z0-9_]|$)'
KOTLIN_LAUNCH_INPUT='NSProcessInfo|NSUserDefaults\.standardUserDefaults|platform\.posix\.getenv|[^A-Za-z_.]getenv\('

[[ -f "$DEFINITION" ]] || {
	echo "FAIL: missing launch argument definition: $DEFINITION"
	exit 1
}

[[ -d "$IOS_HOST_DIR" ]] || {
	echo "FAIL: missing iOS host sources: $IOS_HOST_DIR"
	exit 1
}

status=0

# Lists files, skipping build output and vendored trees. Arguments are extra `find` tests.
list_sources() {
	local base="$1"
	shift

	find "$base" \
		\( -name build -o -name Pods -o -name .git -o -name .gradle -o -name .claude -o -name graphify-out -o -name node_modules \) -prune \
		-o -type f "$@" -print
}

# Test source sets: where the contract does not apply.
is_test_source() {
	case "$1" in
		*/src/commonTest/* | */src/androidHostTest/* | */src/iosTest/* | */src/androidTest/* | */src/androidUnitTest/* | */src/test/* | */src/testDebug/*) return 0 ;;
	esac
	return 1
}

# report_matches <rule> <message> <pattern> <file>: a FAIL line when the pattern matches. A grep that cannot read
# the file is a FAIL too: a check that cannot run must not read as a pass.
report_matches() {
	local rule="$1"
	local message="$2"
	local pattern="$3"
	local file="$4"
	local hits=""
	local code=0

	hits=$(grep -nE "$pattern" "$file") || code=$?
	if (( code > 1 )); then
		echo "FAIL [rule ${rule}]: cannot read ${file#"$ROOT_DIR"/}"
		status=1
	elif (( code == 0 )); then
		echo "FAIL [rule ${rule}]: ${message}: ${file#"$ROOT_DIR"/}"
		echo "$hits" | sed 's/^/  /'
		status=1
	fi
}

declared_keys=$(grep -oE "$KEY_PATTERN" "$DEFINITION" | sort -u)

# Rule 1: literals outside the definition and the test source sets.
while IFS= read -r file; do
	[[ "$file" == "$DEFINITION" ]] && continue
	case "$file" in
		*/iosApp/UITests/*) continue ;;
	esac
	is_test_source "$file" && continue

	report_matches 1 "launch argument literal outside DebugLaunchArguments.kt" "$KEY_PATTERN" "$file"
done < <(list_sources "$ROOT_DIR" \( -name '*.kt' -o -name '*.swift' \))

# Rule 2: literals in the E2E assets must be declared.
for dir in e2e testkit/e2e .codex iosApp/UITests; do
	[[ -d "${ROOT_DIR}/${dir}" ]] || continue

	while IFS= read -r file; do
		for key in $(grep -IohE "$KEY_PATTERN" "$file" | sort -u); do
			if ! echo "$declared_keys" | grep -qx "$key"; then
				echo "FAIL [rule 2]: ${key} in ${file#"$ROOT_DIR"/} is not declared in DebugLaunchArguments.kt"
				status=1
			fi
		done
	done < <(list_sources "${ROOT_DIR}/${dir}")
done

# Rule 3: launch input reads in the iOS host need a DEBUG frame on the #if stack.
while IFS= read -r file; do
	hits=""
	if ! hits=$(LAUNCH_INPUT="$SWIFT_LAUNCH_INPUT" awk '
		# Only a plain DEBUG condition (optionally `DEBUG && ...`) is a debug frame: a `!` or an `||` lets release in.
		function isDebug(cond) {
			if (cond ~ /!/ || cond ~ /\|\|/) return 0
			return cond ~ /(^|[^A-Za-z0-9_])DEBUG([^A-Za-z0-9_]|$)/
		}
		/^[[:space:]]*#if[[:space:]]/ {
			cond = $0
			sub(/^[[:space:]]*#if[[:space:]]+/, "", cond)
			depth++
			frame[depth] = isDebug(cond)
			alternate[depth] = (cond ~ /^!DEBUG[[:space:]]*(\/\/.*)?$/)
			next
		}
		/^[[:space:]]*#elseif[[:space:]]/ {
			cond = $0
			sub(/^[[:space:]]*#elseif[[:space:]]+/, "", cond)
			frame[depth] = isDebug(cond)
			alternate[depth] = 0
			next
		}
		/^[[:space:]]*#else/ {
			frame[depth] = alternate[depth]
			alternate[depth] = 0
			next
		}
		/^[[:space:]]*#endif/ { depth--; next }
		/^[[:space:]]*\/\// { next }
		$0 ~ ENVIRON["LAUNCH_INPUT"] {
			inDebug = 0
			for (i = 1; i <= depth; i++) if (frame[i]) inDebug = 1
			if (!inDebug) printf "%d:%s\n", NR, $0
		}
	' "$file"); then
		echo "FAIL [rule 3]: cannot scan ${file#"$ROOT_DIR"/}"
		status=1
	elif [[ -n "$hits" ]]; then
		echo "FAIL [rule 3]: launch input read outside #if DEBUG: ${file#"$ROOT_DIR"/}"
		echo "$hits" | sed 's/^/  /'
		status=1
	fi
done < <(list_sources "$IOS_HOST_DIR" -name '*.swift')

# Rule 4: Kotlin for iOS reads no launch input (it has no #if to keep it out of release).
while IFS= read -r file; do
	is_test_source "$file" && continue
	report_matches 4 "launch input read in Kotlin for iOS" "$KOTLIN_LAUNCH_INPUT" "$file"
done < <(list_sources "$ROOT_DIR" -name '*.kt' \( -path '*/src/ios*Main/*' -o -path '*/src/appleMain/*' -o -path '*/src/nativeMain/*' \))

# Rule 5: the constants of DebugLaunchArguments stay out of code that ships.
while IFS= read -r file; do
	[[ "$file" == "$DEFINITION" ]] && continue
	is_test_source "$file" && continue
	case "$file" in
		*/src/debug/* | */src/androidDebug/*) continue ;;
		"$ROOT_DIR"/scenarios/*) continue ;;
		"$ROOT_DIR"/maincore/src/iosMain/*/IosAppHostBootstrap.kt) continue ;;
	esac

	report_matches 5 "DebugLaunchArguments used outside debug sources, the iOS bootstrap, :scenarios and tests" '\bDebugLaunchArguments\b' "$file"
done < <(list_sources "$ROOT_DIR" -name '*.kt')

if [[ $status -eq 0 ]]; then
	echo "OK: launch argument contract holds ($(echo "$declared_keys" | wc -l | tr -d ' ') keys declared)."
fi

exit $status
