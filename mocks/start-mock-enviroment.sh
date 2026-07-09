#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
wiremock_version="3.13.2"
wiremock_jar="${script_dir}/wiremock-standalone-${wiremock_version}.jar"
generated_root="${WIREMOCK_GENERATED_ROOT:-/tmp/tuindice-wiremock}"
runtime_dir="${WIREMOCK_RUNTIME_DIR:-${generated_root}/runtime}"
extension_build_dir="${WIREMOCK_EXTENSION_BUILD_DIR:-${generated_root}/build/mocks-extension}"
extension_source_dir="${script_dir}/extensions/src"
kotlinc_bin="${KOTLINC_BIN:-kotlinc}"
kotlin_bin="${KOTLIN_BIN:-kotlin}"
port="${PORT:-8080}"
delay_profile="${WIREMOCK_DELAY_PROFILE:-legacy}"
# Custom entrypoint: wiremock.Run with a notifier that omits binary bodies
# (see extensions/src/.../RunMockEnvironment.kt).
wiremock_main_class="com.gdavidpb.tuindice.mocks.RunMockEnvironment"
extension_factory_classes=(
	"com.gdavidpb.tuindice.mocks.RecordResponseTransformerFactory"
	"com.gdavidpb.tuindice.mocks.EvaluationsResponseTransformerFactory"
)

if lsof -nP -iTCP:"${port}" -sTCP:LISTEN >/dev/null 2>&1; then
	# WireMock queda huérfano cuando un runner mata al wrapper de kotlin pero no
	# al java hijo; fallar aquí evita compilar extensiones para morir en el bind.
	echo "Port ${port} is already in use (stale WireMock from a previous run?)." >&2
	echo "Inspect it with: lsof -nP -iTCP:${port} -sTCP:LISTEN" >&2
	echo "Free it with:    kill \$(lsof -t -iTCP:${port} -sTCP:LISTEN)" >&2
	exit 1
fi

if [ ! -f "${wiremock_jar}" ]; then
	echo "Missing ${wiremock_jar} in ${script_dir}." >&2
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

rm -rf "${runtime_dir}"
mkdir -p "${runtime_dir}"
cp -R "${script_dir}/__files" "${runtime_dir}/__files"
cp -R "${script_dir}/config" "${runtime_dir}/config"
cp -R "${script_dir}/config" "${runtime_dir}/__files/config"
cp -R "${script_dir}/mappings" "${runtime_dir}/mappings"

# The fast profile's per-mapping delays live in each mapping's
# metadata.fastDelayMilliseconds (see scripts/apply-fast-delay-profile.sh).
case "${delay_profile}" in
	fast)
		"${script_dir}/scripts/apply-fast-delay-profile.sh" "${runtime_dir}/mappings"
		;;
	legacy)
		;;
	*)
		echo "Unsupported WIREMOCK_DELAY_PROFILE value: ${delay_profile}. Use fast or legacy." >&2
		exit 1
		;;
esac

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

exec "${kotlin_bin}" \
	-cp "${wiremock_jar}:${extension_build_dir}" \
	"${wiremock_main_class}" \
	--root-dir "${runtime_dir}" \
	--port "${port}" \
	--verbose
