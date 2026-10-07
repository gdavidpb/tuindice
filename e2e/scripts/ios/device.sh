#!/usr/bin/env bash
# iOS device policy: the dedicated simulator from e2e/toolchain/ios.lock.
#
# Verbs (JSON on stdout, diagnostics on stderr, exit 3 when the environment is not right):
#   toolchain  what the lock pins, read from Xcode and simctl (no simulator is created or booted)
#   ensure     create the simulator from the lock if absent, boot it erased if it is shut down, reuse it if booted,
#              apply the keyboard, language and hardware-keyboard settings and read them back
#   serial     the UDID of the simulator
#   health     the simulator is booted
#   recover    shut down, boot and apply the settings again
#   stop       shut down only this simulator
# A simulator that is already booted is never rebooted by `ensure`, except when its data directory outgrew the limit.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
source "${SCRIPT_DIR}/../shared/lib.sh"

LOCK_FILE="${E2E_FAKE_LOCK_FILE:-${REPO_ROOT}/e2e/toolchain/ios.lock}"
LOCK_KEYS=(XCODE_VERSION XCODE_BUILD IOS_RUNTIME_ID IOS_RUNTIME_BUILD IOS_DEVICE_TYPE_ID IOS_SIMULATOR_NAME IOS_LANGUAGE IOS_LOCALE)
DATA_LIMIT_GB="${E2E_FAKE_IOS_DATA_LIMIT_GB:-10}" # above this the simulator is erased: testmanagerd diagnostics grow unbounded
DEVICES_DIR="${E2E_FAKE_SIM_DEVICES_DIR:-${HOME}/Library/Developer/CoreSimulator/Devices}"
HOST_DOMAIN="${E2E_FAKE_HOST_DOMAIN:-com.apple.iphonesimulator}" # the Simulator app's own preferences
KEYBOARD_KEYS=(KeyboardAutocorrection KeyboardPrediction KeyboardShowPredictionBar KeyboardAutocapitalization
	KeyboardCheckSpelling KeyboardPeriodShortcut)
TEXT_KEYS=(NSAutomaticSpellingCorrectionEnabled NSAutomaticTextCompletionEnabled NSUseSpellCheckerForCompletions)

fail() {
	printf '%s\n' "$*" >&2
	exit 3
}

# emit_json key=s:string key=j:raw-json ...
emit_json() {
	python3 -c '
import json, sys
out = {}
for item in sys.argv[1:]:
    key, _, rest = item.partition("=")
    kind, _, value = rest.partition(":")
    out[key] = json.loads(value) if kind == "j" else value
print(json.dumps(out))' "$@"
}

load_lock() {
	local key line value
	[[ -f "${LOCK_FILE}" ]] || { printf 'Missing lock %s\n' "${LOCK_FILE}" >&2; exit 2; }
	while IFS= read -r line || [[ -n "${line}" ]]; do
		[[ -z "${line}" || "${line}" == \#* ]] && continue
		[[ "${line%%=*}" =~ ^[A-Z_]+$ ]] || { printf 'Bad line in %s: %s\n' "${LOCK_FILE}" "${line}" >&2; exit 2; }
		printf -v "${line%%=*}" '%s' "${line#*=}"
	done < "${LOCK_FILE}"
	for key in "${LOCK_KEYS[@]}"; do
		value="${!key:-}"
		[[ -n "${value}" ]] || { printf 'The lock %s has no %s\n' "${LOCK_FILE}" "${key}" >&2; exit 2; }
	done
}

# simctl_query <simctl list kind> <python expression over `data`>: prints the value, or nothing when simctl fails
simctl_query() {
	local kind="$1" expression="$2" out
	if out="$(xcrun simctl list "${kind}" -j 2> /dev/null)"; then
		python3 -c 'import json, sys; data = json.loads(sys.stdin.read()); print(eval(sys.argv[1]))' "${expression}" <<< "${out}"
	fi
}

# Sets UDID and STATE for the simulator named in the lock (under the pinned runtime); false when it does not exist.
find_simulator() {
	local line
	line="$(simctl_query devices "next((d['udid'] + ' ' + d['state'] for d in data['devices'].get('${IOS_RUNTIME_ID}', []) if d['name'] == '${IOS_SIMULATOR_NAME}'), '')")"
	[[ -n "${line}" ]] || return 1
	read -r UDID STATE <<< "${line}"
}

pref_set() { # domain key type value...
	xcrun simctl spawn "${UDID}" defaults write "$@"
}

pref_get() { # domain key
	local out
	if out="$(xcrun simctl spawn "${UDID}" defaults read "$1" "$2" 2> /dev/null)"; then
		printf '%s' "${out}" | tr -d ' \n()'
	fi
}

check_pref() { # domain key expected; appends to PAIRS
	local actual
	actual="$(pref_get "$1" "$2")"
	[[ "${actual}" == "$3" ]] || fail "The preference $1 $2 reads back '${actual}', expected '$3'"
	PAIRS+=("$1.$2=s:${actual}")
}

apply_settings() { # sets SETTINGS_JSON; exits 3 on a read-back mismatch
	local key domain
	PAIRS=()
	for domain in NSGlobalDomain com.apple.Preferences; do
		for key in "${KEYBOARD_KEYS[@]}"; do
			pref_set "${domain/NSGlobalDomain/-g}" "${key}" -bool false
			check_pref "${domain/NSGlobalDomain/-g}" "${key}" 0
		done
	done
	for key in "${TEXT_KEYS[@]}"; do
		pref_set -g "${key}" -bool false
		check_pref -g "${key}" 0
	done
	pref_set -g AppleLanguages -array "${IOS_LANGUAGE}"
	check_pref -g AppleLanguages "${IOS_LANGUAGE}"
	pref_set -g AppleLocale -string "${IOS_LOCALE}"
	check_pref -g AppleLocale "${IOS_LOCALE}"
	defaults write "${HOST_DOMAIN}" DevicePreferences -dict-add "${UDID}" '<dict><key>ConnectHardwareKeyboard</key><false/></dict>'
	local hardware
	hardware="$(defaults export "${HOST_DOMAIN}" - | plutil -extract "DevicePreferences.${UDID}.ConnectHardwareKeyboard" raw -o - -)"
	[[ "${hardware}" == "false" ]] || fail "ConnectHardwareKeyboard reads back '${hardware}' for ${UDID}, expected false"
	PAIRS+=("ConnectHardwareKeyboard=s:${hardware}")
	SETTINGS_JSON="$(emit_json "${PAIRS[@]}")"
}

boot_erased() {
	xcrun simctl erase "${UDID}"
	boot_simulator
}

boot_simulator() {
	xcrun simctl boot "${UDID}"
	xcrun simctl bootstatus "${UDID}" -b > /dev/null
	STATE="Booted"
}

data_gb() {
	local dir="${DEVICES_DIR}/${UDID}/data"
	if [[ -d "${dir}" ]]; then
		du -sk "${dir}" | awk '{ printf "%.6f", $1 / 1048576 }'
	else
		printf '0.000000'
	fi
}

cmd_toolchain() {
	local xcode version build runtime_build type_id runtime_id="" name=""
	xcode="$(xcrun xcodebuild -version)"
	version="$(awk 'NR == 1 { print $2 }' <<< "${xcode}")"
	build="$(awk '/^Build version/ { print $3 }' <<< "${xcode}")"
	runtime_build="$(simctl_query runtimes "next((r['buildversion'] for r in data['runtimes'] if r['identifier'] == '${IOS_RUNTIME_ID}' and r['isAvailable']), '')")"
	type_id="$(simctl_query devicetypes "next((t['identifier'] for t in data['devicetypes'] if t['identifier'] == '${IOS_DEVICE_TYPE_ID}'), '')")"
	# The runtime id is reported only when that runtime is installed and available.
	if [[ -n "${runtime_build}" ]]; then
		runtime_id="${IOS_RUNTIME_ID}"
	fi
	# The simulator, its language and its locale are verified by `ensure`: here they are null unless it already exists.
	name="j:null"
	if find_simulator; then
		name="s:${IOS_SIMULATOR_NAME}"
	fi
	emit_json "XCODE_VERSION=s:${version}" "XCODE_BUILD=s:${build}" "IOS_RUNTIME_ID=s:${runtime_id}" \
		"IOS_RUNTIME_BUILD=s:${runtime_build}" "IOS_DEVICE_TYPE_ID=s:${type_id}" "IOS_SIMULATOR_NAME=${name}" \
		"IOS_LANGUAGE=j:null" "IOS_LOCALE=j:null" "macOS=s:$(sw_vers -productVersion)"
}

cmd_ensure() {
	local created=false booted_by_harness=false model size
	if ! find_simulator; then
		[[ -n "$(simctl_query runtimes "next((r['identifier'] for r in data['runtimes'] if r['identifier'] == '${IOS_RUNTIME_ID}' and r['isAvailable']), '')")" ]] ||
			fail "The runtime ${IOS_RUNTIME_ID} is not installed"
		UDID="$(xcrun simctl create "${IOS_SIMULATOR_NAME}" "${IOS_DEVICE_TYPE_ID}" "${IOS_RUNTIME_ID}")"
		STATE="Shutdown"
		created=true
	fi
	case "${STATE}" in
		Shutdown)
			boot_erased
			booted_by_harness=true
			;;
		Booted)
			size="$(data_gb)"
			if awk -v size="${size:-0}" -v limit="${DATA_LIMIT_GB}" 'BEGIN { exit !(size > limit) }'; then
				log "The simulator data directory is ${size} GB (limit ${DATA_LIMIT_GB}); erasing and booting it again."
				xcrun simctl shutdown "${UDID}"
				boot_erased
				booted_by_harness=true
			fi
			;;
		*) fail "The simulator ${UDID} is '${STATE}'; wait for it to settle and run ensure again" ;;
	esac
	apply_settings
	model="$(simctl_query devicetypes "next((t['name'] for t in data['devicetypes'] if t['identifier'] == '${IOS_DEVICE_TYPE_ID}'), '')")"
	emit_json "id=s:${UDID}" "model=s:${model}" "created=j:${created}" "bootedByHarness=j:${booted_by_harness}" \
		"bootedAt=j:null" "dataDirGb=j:$(data_gb)" "settings=j:${SETTINGS_JSON}"
}

cmd_health() {
	find_simulator || fail "The simulator ${IOS_SIMULATOR_NAME} does not exist"
	[[ "${STATE}" == "Booted" ]] || fail "The simulator ${UDID} is '${STATE}', not Booted"
	emit_json "ok=j:true" "id=s:${UDID}"
}

cmd_recover() {
	find_simulator || fail "The simulator ${IOS_SIMULATOR_NAME} does not exist"
	if [[ "${STATE}" == "Booted" ]]; then
		xcrun simctl shutdown "${UDID}"
	fi
	boot_simulator
	apply_settings
	emit_json "ok=j:true" "id=s:${UDID}" "settings=j:${SETTINGS_JSON}"
}

cmd_stop() {
	if find_simulator && [[ "${STATE}" == "Booted" ]]; then
		xcrun simctl shutdown "${UDID}"
	fi
	emit_json "ok=j:true"
}

cmd_serial() {
	find_simulator || fail "The simulator ${IOS_SIMULATOR_NAME} does not exist"
	printf '%s\n' "${UDID}"
}

load_lock
case "${1:-}" in
	toolchain) cmd_toolchain ;;
	ensure) cmd_ensure ;;
	serial) cmd_serial ;;
	health) cmd_health ;;
	recover) cmd_recover ;;
	stop) cmd_stop ;;
	*) printf 'Usage: %s toolchain|ensure|serial|health|recover|stop\n' "$0" >&2; exit 64 ;;
esac
