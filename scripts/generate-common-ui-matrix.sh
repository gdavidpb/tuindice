#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUTPUT_FILE="$ROOT_DIR/docs/testing/common-ui-matrix.md"
DEFAULT_THRESHOLD="${1:-2}"

threshold_for_module() {
	local module="$1"
	if [[ "$module" == "maincore" ]]; then
		echo 3
	else
		echo "$DEFAULT_THRESHOLD"
	fi
}

count_composables() {
	local file="$1"
	local matches
	matches="$(grep -E '^[[:space:]]*@Composable([[:space:]]|$|\()' "$file" || true)"
	if [[ -z "$matches" ]]; then
		echo 0
	else
		printf "%s\n" "$matches" | wc -l | tr -d ' '
	fi
}

count_when_cases() {
	local file="$1"
	local matches
	if [[ ! -f "$file" ]]; then
		echo 0
		return
	fi
	matches="$(grep -E '^[[:space:]]*fun[[:space:]]+when_' "$file" || true)"
	if [[ -z "$matches" ]]; then
		echo 0
	else
		printf "%s\n" "$matches" | wc -l | tr -d ' '
	fi
}

expected_test_path() {
	local source_path="$1"
	local relative_path="${source_path#"$ROOT_DIR"/}"
	relative_path="$(printf "%s" "$relative_path" | sed 's#src/commonMain/kotlin#src/commonTest/kotlin#')"
	echo "${relative_path%.kt}UiTest.kt"
}

find_composable_files() {
	local module="$1"
	local module_main="$ROOT_DIR/$module/src/commonMain/kotlin"
	if [[ ! -d "$module_main" ]]; then
		return
	fi
	find "$module_main" -type f -name '*.kt' | sort | while IFS= read -r file; do
		if (( $(count_composables "$file") > 0 )); then
			echo "$file"
		fi
	done
}

modules="$(
	find "$ROOT_DIR" -path '*/src/commonMain/kotlin/*.kt' -type f | while IFS= read -r file; do
		if (( $(count_composables "$file") > 0 )); then
			relative_path="${file#"$ROOT_DIR"/}"
			echo "${relative_path%%/*}"
		fi
	done | sort -u
)"

tmp_file="$(mktemp)"
trap 'rm -f "$tmp_file"' EXIT

{
	echo "# Common UI Coverage Matrix"
	echo
	echo "_Generated automatically on $(date '+%Y-%m-%d %H:%M:%S %z')_"
	echo
	echo "## Summary by module"
	echo
	echo "| Module | when_ threshold | commonMain composable files | @Composable occurrences | commonTest UI tests | Nominal tests | UiTest files meeting threshold |"
	echo "|---|---:|---:|---:|---:|---:|---:|"

	for module in $modules; do
		threshold="$(threshold_for_module "$module")"
		composable_files="$(find_composable_files "$module")"
		composable_file_count=0
		composable_occurrences=0
		nominal_tests=0
		passing_tests=0
		ui_tests=0

		if [[ -d "$ROOT_DIR/$module/src/commonTest" ]]; then
			ui_tests="$(find "$ROOT_DIR/$module/src/commonTest" -type f -name '*UiTest.kt' | wc -l | tr -d ' ')"
		fi

		while IFS= read -r file; do
			[[ -z "$file" ]] && continue
			composable_file_count=$((composable_file_count + 1))
			composable_occurrences=$((composable_occurrences + $(count_composables "$file")))
			test_relative_path="$(expected_test_path "$file")"
			test_file="$ROOT_DIR/$test_relative_path"
			when_cases="$(count_when_cases "$test_file")"

			if [[ -f "$test_file" ]]; then
				nominal_tests=$((nominal_tests + 1))
			fi
			if (( when_cases >= threshold )); then
				passing_tests=$((passing_tests + 1))
			fi
		done <<< "$composable_files"

		echo "| \`$module\` | $threshold | $composable_file_count | $composable_occurrences | $ui_tests | $nominal_tests | $passing_tests |"
	done

	echo
	echo "## Detail by composable file"

	for module in $modules; do
		threshold="$(threshold_for_module "$module")"
		composable_files="$(find_composable_files "$module")"
		echo
		echo "### \`$module\`"
		echo
		echo "| File | @Composable | Expected test | when_ cases | Status |"
		echo "|---|---:|---|---:|---|"

		while IFS= read -r file; do
			[[ -z "$file" ]] && continue
			source_relative_path="${file#"$ROOT_DIR"/}"
			composable_count="$(count_composables "$file")"
			test_relative_path="$(expected_test_path "$file")"
			test_file="$ROOT_DIR/$test_relative_path"
			when_cases="$(count_when_cases "$test_file")"

			if [[ ! -f "$test_file" ]]; then
				status="MISSING TEST"
			elif (( when_cases >= threshold )); then
				status="PASS threshold ($threshold)"
			else
				status="FAIL threshold ($threshold)"
			fi

			echo "| \`$source_relative_path\` | $composable_count | \`$test_relative_path\` | $when_cases | $status |"
		done <<< "$composable_files"
	done
} > "$tmp_file"

mv "$tmp_file" "$OUTPUT_FILE"
echo "[INFO] Wrote docs/testing/common-ui-matrix.md"
