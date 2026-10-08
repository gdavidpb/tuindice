#!/usr/bin/env bash
# Verifies the TuIndiceUITests target is wired the way the E2E design requires:
#   1. project.pbxproj is a valid property list.
#   2. xcodebuild lists the TuIndiceUITests target and scheme.
#   3. Debug.xcconfig and Release.xcconfig never mention ScenarioKit.
#   4. With a built app (APP=<path to TuIndiceHost.app> or --app <path>, repeatable: the Debug and the Release app),
#      the app binaries carry no ScenarioKit symbols or strings. Without one, this check is reported as skipped,
#      unless --require-app is given (the caller just built the app): then a missing app is a failure. A binary that
#      `nm` or `strings` cannot read is a failure, never a clean count.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
PROJECT="$ROOT_DIR/TuIndiceHost.xcodeproj"
APPS=()
if [[ -n "${APP:-}" ]]; then
	APPS+=("$APP")
fi
REQUIRE_APP=0
status=0

while [[ $# -gt 0 ]]; do
	case "$1" in
		--app)
			APPS+=("${2:?--app needs a path}")
			shift 2
			;;
		--require-app)
			REQUIRE_APP=1
			shift
			;;
		*)
			echo "Unknown argument: $1" >&2
			exit 2
			;;
	esac
done

fail() {
	echo "FAIL: $*"
	status=1
}

if plutil -lint "$PROJECT/project.pbxproj" >/dev/null; then
	echo "OK: project.pbxproj is a valid property list."
else
	fail "project.pbxproj does not lint."
fi

listing="$(xcodebuild -list -json -project "$PROJECT" 2>/dev/null)" || listing=""
if [[ -z "$listing" ]]; then
	fail "xcodebuild -list produced no output."
else
	if python3 -c '
import json, sys
data = json.loads(sys.stdin.read())["project"]
sys.exit(0 if "TuIndiceUITests" in data["targets"] and "TuIndiceUITests" in data["schemes"] else 1)
' <<<"$listing"; then
		echo "OK: target and scheme TuIndiceUITests are listed."
	else
		fail "xcodebuild -list does not show target and scheme TuIndiceUITests."
	fi
fi

for config in Debug Release; do
	if grep -q "ScenarioKit" "$ROOT_DIR/Config/$config.xcconfig"; then
		fail "Config/$config.xcconfig mentions ScenarioKit."
	else
		echo "OK: Config/$config.xcconfig does not mention ScenarioKit."
	fi
done

# Counts the lines of <command> output that mention scenariokit (case-insensitive); fails when the command does.
count_mentions() { # <binary> <nm|strings>
	local binary="$1" tool="$2" listing
	listing="$(mktemp)"
	if [[ "$tool" == "nm" ]]; then
		nm -a "$binary" > "$listing" 2> /dev/null || { rm -f "$listing"; return 1; }
	else
		strings -a "$binary" > "$listing" 2> /dev/null || { rm -f "$listing"; return 1; }
	fi
	grep -ci "scenariokit" "$listing" || true
	rm -f "$listing"
}

check_app() { # <path to a built TuIndiceHost.app>
	local app="$1" hits=0 unreadable=0 binary symbols strings_found
	if [[ ! -d "$app" ]]; then
		fail "app bundle not found: $app"
		return
	fi
	# Debug builds keep the code in TuIndiceHost.debug.dylib next to the launcher, so scan every Mach-O.
	while IFS= read -r binary; do
		file "$binary" | grep -q "Mach-O" || continue
		if ! symbols="$(count_mentions "$binary" nm)"; then
			fail "nm could not read $binary; the symbol check would be a false green."
			unreadable=1
			continue
		fi
		if ! strings_found="$(count_mentions "$binary" strings)"; then
			fail "strings could not read $binary; the string check would be a false green."
			unreadable=1
			continue
		fi
		hits=$((hits + symbols + strings_found))
	done < <(find "$app" -type f \( -perm -u+x -o -name '*.dylib' \))

	if [[ "$unreadable" -eq 1 ]]; then
		return
	elif [[ "$hits" -eq 0 ]]; then
		echo "OK: the app binaries in $app contain no ScenarioKit symbols or strings."
	else
		fail "the app binaries in $app mention ScenarioKit ($hits matches)."
	fi
}

if [[ "${#APPS[@]}" -eq 0 && "$REQUIRE_APP" -eq 1 ]]; then
	fail "--require-app was given but no built app (APP=... or --app ...)."
elif [[ "${#APPS[@]}" -eq 0 ]]; then
	echo "SKIP: no built app given (APP=... or --app ...); binary symbol check not run."
else
	for app in "${APPS[@]}"; do
		check_app "$app"
	done
fi

exit $status
