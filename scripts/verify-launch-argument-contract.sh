#!/usr/bin/env bash
# Validates the debug launch-argument contract.
#
# Source of truth: maincore/.../debug/DebugLaunchArguments.kt declares every
# TUINDICE_E2E_* key once. Rules encoded here:
# 1. No TUINDICE_E2E_* literal in any .kt or .swift file outside that definition,
#    test source sets and the iOS UI-test runner (iosApp/UITests, whose literals
#    are checked by rule 2 instead); everything else reads the constants.
# 2. Every TUINDICE_E2E_* literal in e2e/, testkit/e2e/, .codex/ and iosApp/UITests/
#    is declared in the definition.
# 3. The iOS host reads the process environment, the process arguments and
#    UserDefaults strings only inside an #if DEBUG block, so release builds
#    take no input from launch.

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DEFINITION="${ROOT_DIR}/maincore/src/commonMain/kotlin/com/gdavidpb/tuindice/debug/DebugLaunchArguments.kt"
IOS_HOST_DIR="${ROOT_DIR}/iosApp/Sources/TuIndiceHost"
KEY_PATTERN='TUINDICE_E2E_[A-Z_]+'

[[ -f "$DEFINITION" ]] || {
	echo "FAIL: missing launch argument definition: $DEFINITION"
	exit 1
}

[[ -d "$IOS_HOST_DIR" ]] || {
	echo "FAIL: missing iOS host sources: $IOS_HOST_DIR"
	exit 1
}

status=0

# Lists files of the given extension, skipping build output and vendored trees.
list_sources() {
	local base="$1"
	shift

	find "$base" \
		\( -name build -o -name Pods -o -name .git -o -name .gradle -o -name .claude -o -name graphify-out -o -name node_modules \) -prune \
		-o -type f "$@" -print
}

declared_keys=$(grep -oE "$KEY_PATTERN" "$DEFINITION" | sort -u)

# Rule 1: literals outside the definition and the test source sets.
while IFS= read -r file; do
	[[ "$file" == "$DEFINITION" ]] && continue
	case "$file" in
		*/iosApp/UITests/* | */commonTest/* | */androidHostTest/* | */iosTest/* | */androidTest/* | */androidUnitTest/* | */test/*) continue ;;
	esac

	if hits=$(grep -nE "$KEY_PATTERN" "$file"); then
		echo "FAIL [rule 1]: launch argument literal outside DebugLaunchArguments.kt: ${file#"$ROOT_DIR"/}"
		echo "$hits" | sed 's/^/  /'
		status=1
	fi
done < <(list_sources "$ROOT_DIR" \( -name '*.kt' -o -name '*.swift' \))

# Rule 2: literals in the E2E assets must be declared.
for dir in e2e testkit/e2e .codex iosApp/UITests; do
	[[ -d "${ROOT_DIR}/${dir}" ]] || continue

	while IFS= read -r file; do
		for key in $(grep -ohE "$KEY_PATTERN" "$file" | sort -u); do
			if ! echo "$declared_keys" | grep -qx "$key"; then
				echo "FAIL [rule 2]: ${key} in ${file#"$ROOT_DIR"/} is not declared in DebugLaunchArguments.kt"
				status=1
			fi
		done
	done < <(list_sources "${ROOT_DIR}/${dir}" -size -2000k)
done

# Rule 3: launch input reads in the iOS host need a DEBUG frame on the #if stack.
while IFS= read -r file; do
	if hits=$(awk '
		function isDebug(cond) { return cond ~ /(^|[^!A-Za-z0-9_])DEBUG([^A-Za-z0-9_]|$)/ }
		/^[[:space:]]*#if[[:space:]]/ {
			cond = $0
			sub(/^[[:space:]]*#if[[:space:]]+/, "", cond)
			depth++
			frame[depth] = isDebug(cond)
			alternate[depth] = (cond ~ /^!DEBUG/)
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
		/ProcessInfo\.processInfo\.(environment|arguments)|UserDefaults\.standard\.string\(forKey:/ {
			inDebug = 0
			for (i = 1; i <= depth; i++) if (frame[i]) inDebug = 1
			if (!inDebug) printf "%d:%s\n", NR, $0
		}
	' "$file"); then
		if [[ -n "$hits" ]]; then
			echo "FAIL [rule 3]: launch input read outside #if DEBUG: ${file#"$ROOT_DIR"/}"
			echo "$hits" | sed 's/^/  /'
			status=1
		fi
	fi
done < <(list_sources "$IOS_HOST_DIR" -name '*.swift')

if [[ $status -eq 0 ]]; then
	echo "OK: launch argument contract holds ($(echo "$declared_keys" | wc -l | tr -d ' ') keys declared)."
fi

exit $status
