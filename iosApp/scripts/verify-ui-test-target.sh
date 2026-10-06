#!/usr/bin/env bash
# Verifies the TuIndiceUITests target is wired the way the E2E design requires:
#   1. project.pbxproj is a valid property list.
#   2. xcodebuild lists the TuIndiceUITests target and scheme.
#   3. Debug.xcconfig and Release.xcconfig never mention ScenarioKit.
#   4. With a built app (APP=<path to TuIndiceHost.app> or --app <path>), the app binaries carry
#      no ScenarioKit symbols or strings. Without one, this check is reported as skipped.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
PROJECT="$ROOT_DIR/TuIndiceHost.xcodeproj"
APP="${APP:-}"
status=0

while [[ $# -gt 0 ]]; do
	case "$1" in
		--app)
			APP="${2:?--app needs a path}"
			shift 2
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

if [[ -z "$APP" ]]; then
	echo "SKIP: no built app given (APP=... or --app ...); binary symbol check not run."
elif [[ ! -d "$APP" ]]; then
	fail "app bundle not found: $APP"
else
	hits=0
	# Debug builds keep the code in TuIndiceHost.debug.dylib next to the launcher, so scan every Mach-O.
	while IFS= read -r binary; do
		file "$binary" | grep -q "Mach-O" || continue
		symbols="$(nm -a "$binary" 2>/dev/null | grep -ci "scenariokit" || true)"
		strings_found="$(strings -a "$binary" 2>/dev/null | grep -c "scenariokit" || true)"
		hits=$((hits + symbols + strings_found))
	done < <(find "$APP" -type f \( -perm -u+x -o -name '*.dylib' \))

	if [[ "$hits" -eq 0 ]]; then
		echo "OK: the app binaries contain no ScenarioKit symbols or strings."
	else
		fail "the app binaries mention ScenarioKit ($hits matches)."
	fi
fi

exit $status
