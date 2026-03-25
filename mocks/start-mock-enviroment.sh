#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
wiremock_version="3.13.2"
wiremock_jar="${script_dir}/wiremock-standalone-${wiremock_version}.jar"
generated_root="${WIREMOCK_GENERATED_ROOT:-/tmp/tuindice-wiremock}"
runtime_dir="${WIREMOCK_RUNTIME_DIR:-${generated_root}/runtime}"
extension_build_dir="${WIREMOCK_EXTENSION_BUILD_DIR:-${generated_root}/build/mocks-extension}"
extension_source_dir="${script_dir}/extensions/src"
generator_script="${script_dir}/scripts/generate_record_wiremock.py"
kotlinc_bin="${KOTLINC_BIN:-kotlinc}"
kotlin_bin="${KOTLIN_BIN:-kotlin}"
port="${PORT:-8080}"
max_revision="${WIREMOCK_RECORD_MAX_REVISION:-20}"
wiremock_main_class="wiremock.Run"
extension_factory_classes=(
	"com.gdavidpb.tuindice.mocks.RecordScenarioExtensionFactory"
	"com.gdavidpb.tuindice.mocks.EvaluationsResponseTransformerFactory"
)

if [ ! -f "${wiremock_jar}" ]; then
	echo "Missing ${wiremock_jar} in ${script_dir}." >&2
	exit 1
fi

if [ ! -f "${generator_script}" ]; then
	echo "Missing generator script at ${generator_script}." >&2
	exit 1
fi

if [ ! -d "${extension_source_dir}" ]; then
	echo "Missing extension sources at ${extension_source_dir}." >&2
	exit 1
fi

if ! command -v "${kotlinc_bin}" >/dev/null 2>&1; then
	echo "Missing Kotlin compiler command: ${kotlinc_bin}." >&2
	exit 1
fi

if ! command -v "${kotlin_bin}" >/dev/null 2>&1; then
	echo "Missing Kotlin runtime command: ${kotlin_bin}." >&2
	exit 1
fi

python3 "${generator_script}" \
	--source-root "${script_dir}" \
	--runtime-root "${runtime_dir}" \
	--max-revision "${max_revision}"

rm -rf "${extension_build_dir}"
mkdir -p "${extension_build_dir}"

extension_sources=()
while IFS= read -r source; do
	extension_sources+=("${source}")
done < <(find "${extension_source_dir}" -name "*.kt" | sort)

if [ "${#extension_sources[@]}" -eq 0 ]; then
	echo "No WireMock extension sources found in ${extension_source_dir}." >&2
	exit 1
fi

"${kotlinc_bin}" -cp "${wiremock_jar}" -d "${extension_build_dir}" "${extension_sources[@]}"

mkdir -p "${extension_build_dir}/META-INF/services"
{
	for extension_factory_class in "${extension_factory_classes[@]}"; do
		printf "%s\n" "${extension_factory_class}"
	done
} > "${extension_build_dir}/META-INF/services/com.github.tomakehurst.wiremock.extension.ExtensionFactory"

"${kotlin_bin}" \
	-cp "${wiremock_jar}:${extension_build_dir}" \
	"${wiremock_main_class}" \
	--root-dir "${runtime_dir}" \
	--port "${port}" \
	--verbose \
	--local-response-templating
