#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DEFAULT_THRESHOLD="${1:-2}"
FAILURES=""
CHECKED=0

threshold_for_module() {
	local module="$1"
	if [[ "$module" == "maincore" ]]; then
		echo 3
	else
		echo "$DEFAULT_THRESHOLD"
	fi
}

count_when_cases() {
	local file="$1"
	local matches
	matches="$(grep -E '^[[:space:]]*fun[[:space:]]+when_' "$file" || true)"
	if [[ -z "$matches" ]]; then
		echo 0
	else
		printf "%s\n" "$matches" | wc -l | tr -d ' '
	fi
}

while IFS= read -r file; do
	relative_path="${file#"$ROOT_DIR"/}"
	module="${relative_path%%/*}"
	threshold="$(threshold_for_module "$module")"
	count="$(count_when_cases "$file")"
	CHECKED=$((CHECKED + 1))

	if (( count < threshold )); then
		FAILURES="${FAILURES}${relative_path}: ${count}/${threshold} when_ test cases"$'\n'
	fi
done < <(find "$ROOT_DIR" -path '*/src/commonTest/*UiTest.kt' -type f | sort)

if [[ -n "$FAILURES" ]]; then
	echo "[ERROR] Common UI test density is below threshold:"
	printf "%s" "$FAILURES"
	exit 1
fi

echo "[INFO] Common UI test density verified for ${CHECKED} UiTest files."
