#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

cd "$REPO_ROOT"

declare -a cache_inputs=(
	"gradle/wrapper/gradle-wrapper.properties"
	"gradle/libs.versions.toml"
	"gradle.properties"
	"settings.gradle.kts"
	"build.gradle.kts"
	"iosApp/scripts/build-kmp-framework.sh"
	"academiccore/build.gradle.kts"
	"academiccore/src"
	"base/build.gradle.kts"
	"base/src"
	"persistence/build.gradle.kts"
	"persistence/src"
	"auth/build.gradle.kts"
	"auth/src"
	"about/build.gradle.kts"
	"about/src"
	"summary/build.gradle.kts"
	"summary/src"
	"record/build.gradle.kts"
	"record/src"
	"evaluations/build.gradle.kts"
	"evaluations/src"
	"enrollmentproof/build.gradle.kts"
	"enrollmentproof/src"
	"subjects/build.gradle.kts"
	"subjects/src"
	"pensum/build.gradle.kts"
	"pensum/src"
	"wizard/build.gradle.kts"
	"wizard/src"
	"maincore/build.gradle.kts"
	"maincore/src"
)

git ls-files -- "${cache_inputs[@]}" \
	| LC_ALL=C sort \
	| while IFS= read -r file_path; do
		shasum -a 256 "$file_path"
	done \
	| shasum -a 256 \
	| awk '{ print $1 }'
