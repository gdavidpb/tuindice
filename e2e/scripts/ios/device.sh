#!/usr/bin/env bash
# iOS device policy: the dedicated simulator from e2e/toolchain/ios.lock.
#
# Verbs (JSON on stdout, diagnostics on stderr, exit 3 when the environment is not right):
#   toolchain  what the lock pins, read from Xcode and simctl (no simulator is created or booted)
#   ensure     create the simulator from the lock if absent, boot it erased if it is shut down, reuse it if booted,
#              apply the keyboard, keyboard-first-use-sheet, language, active-keyboard-list and hardware-keyboard settings and read them back
#   serial     the UDID of the simulator
#   health     the simulator is booted and still serves preferences (one `defaults read` of a key `ensure` pinned)
#   recover    shut down, boot and apply the settings again
#   stop       shut down only this simulator
# A simulator that is already booted is never rebooted by `ensure`, except when its data directory outgrew the limit (it is erased)
# or when it does not accept the settings (one shutdown and boot, reported as `recoveredAtEnsure`).
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
source "${SCRIPT_DIR}/../shared/lib.sh"

LOCK_FILE="${E2E_FAKE_LOCK_FILE:-${REPO_ROOT}/e2e/toolchain/ios.lock}"
LOCK_KEYS=(XCODE_VERSION XCODE_BUILD IOS_RUNTIME_ID IOS_RUNTIME_BUILD IOS_DEVICE_TYPE_ID IOS_SIMULATOR_NAME IOS_LANGUAGE IOS_LOCALE IOS_KEYBOARDS)
DATA_LIMIT_GB="${E2E_FAKE_IOS_DATA_LIMIT_GB:-10}" # above this the simulator is erased: testmanagerd diagnostics grow unbounded
DEVICES_DIR="${E2E_FAKE_SIM_DEVICES_DIR:-${HOME}/Library/Developer/CoreSimulator/Devices}"
HOST_DOMAIN="${E2E_FAKE_HOST_DOMAIN:-com.apple.iphonesimulator}" # the Simulator app's own preferences
KEYBOARD_KEYS=(KeyboardAutocorrection KeyboardPrediction KeyboardShowPredictionBar KeyboardAutocapitalization
	KeyboardCheckSpelling KeyboardPeriodShortcut)
TEXT_KEYS=(NSAutomaticSpellingCorrectionEnabled NSAutomaticTextCompletionEnabled NSUseSpellCheckerForCompletions)
# The first-use sheet of the keyboard ("Speed up your typing", QuickPath) appears once on a freshly erased simulator, covers the keys
# and ends the first test that types; the flag the keyboard sets when the sheet has been seen is this one.
KEYBOARD_INTRO_DOMAIN="com.apple.keyboard.preferences"
KEYBOARD_INTRO_KEY="DidShowContinuousPathIntroduction"

# simctl_query <simctl list kind> <python expression over `data`>: prints the value; fails when simctl itself fails
simctl_query() {
	local kind="$1" expression="$2" out
	out="$(xcrun simctl list "${kind}" -j 2> /dev/null)" || return 1
	python3 -c 'import json, sys; data = json.loads(sys.stdin.read()); print(eval(sys.argv[1]))' "${expression}" <<< "${out}"
}

# Sets UDID, STATE and TYPE for the simulator named in the lock (under the pinned runtime); false when it does not exist.
# A simctl that fails is not an absent simulator: that would create a second one.
find_simulator() {
	local line
	line="$(simctl_query devices "next((d['udid'] + ' ' + d['state'] + ' ' + d.get('deviceTypeIdentifier', '-') for d in data['devices'].get('${IOS_RUNTIME_ID}', []) if d['name'] == '${IOS_SIMULATOR_NAME}'), '')")" ||
		fail "simctl list devices failed; the harness does not assume the simulator ${IOS_SIMULATOR_NAME} is absent"
	[[ -n "${line}" ]] || return 1
	read -r UDID STATE TYPE <<< "${line}"
}

pref_set() { # domain key type value...
	xcrun simctl spawn "${UDID}" defaults write "$@" || { printf 'defaults write %s failed\n' "$*" >&2; return 1; }
}

pref_get() { # domain key
	local out
	if out="$(xcrun simctl spawn "${UDID}" defaults read "$1" "$2" 2> /dev/null)"; then
		printf '%s' "${out}" | tr -d ' \n()"'
	fi
}

check_pref() { # domain key expected; appends to PAIRS
	local actual
	actual="$(pref_get "$1" "$2")"
	[[ "${actual}" == "$3" ]] || { printf "The preference %s %s reads back '%s', expected '%s'\n" "$1" "$2" "${actual}" "$3" >&2; return 1; }
	PAIRS+=("$1.$2=s:${actual}")
}

# Sets SETTINGS_JSON. Returns 1 (with the reason on stderr) when a write fails or a value does not read back, so that
# `ensure` can recover a simulator that is booted but not answering before it gives up.
apply_settings() {
	local key domain hardware
	PAIRS=()
	for domain in NSGlobalDomain com.apple.Preferences; do
		for key in "${KEYBOARD_KEYS[@]}"; do
			pref_set "${domain/NSGlobalDomain/-g}" "${key}" -bool false || return 1
			check_pref "${domain/NSGlobalDomain/-g}" "${key}" 0 || return 1
		done
	done
	for key in "${TEXT_KEYS[@]}"; do
		pref_set -g "${key}" -bool false || return 1
		check_pref -g "${key}" 0 || return 1
	done
	pref_set "${KEYBOARD_INTRO_DOMAIN}" "${KEYBOARD_INTRO_KEY}" -bool true || return 1
	check_pref "${KEYBOARD_INTRO_DOMAIN}" "${KEYBOARD_INTRO_KEY}" 1 || return 1
	pref_set -g AppleLanguages -array "${IOS_LANGUAGE}" || return 1
	check_pref -g AppleLanguages "${IOS_LANGUAGE}" || return 1
	pref_set -g AppleLocale -string "${IOS_LOCALE}" || return 1
	check_pref -g AppleLocale "${IOS_LOCALE}" || return 1
	pref_set -g AppleKeyboards -array "${IOS_KEYBOARDS}" || return 1
	check_pref -g AppleKeyboards "${IOS_KEYBOARDS}" || return 1
	defaults write "${HOST_DOMAIN}" DevicePreferences -dict-add "${UDID}" '<dict><key>ConnectHardwareKeyboard</key><false/></dict>' || return 1
	hardware="$(defaults export "${HOST_DOMAIN}" - | plutil -extract "DevicePreferences.${UDID}.ConnectHardwareKeyboard" raw -o - -)" || return 1
	[[ "${hardware}" == "false" ]] || { printf "ConnectHardwareKeyboard reads back '%s' for %s, expected false\n" "${hardware}" "${UDID}" >&2; return 1; }
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
		dir_gb "${dir}" 6
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
		"IOS_LANGUAGE=j:null" "IOS_LOCALE=j:null" "IOS_KEYBOARDS=j:null" "macOS=s:$(sw_vers -productVersion)"
}

cmd_ensure() {
	local created=false booted_by_harness=false recovered=false type_verified=true model size
	if ! find_simulator; then
		[[ -n "$(simctl_query runtimes "next((r['identifier'] for r in data['runtimes'] if r['identifier'] == '${IOS_RUNTIME_ID}' and r['isAvailable']), '')")" ]] ||
			fail "The runtime ${IOS_RUNTIME_ID} is not installed"
		UDID="$(xcrun simctl create "${IOS_SIMULATOR_NAME}" "${IOS_DEVICE_TYPE_ID}" "${IOS_RUNTIME_ID}")"
		STATE="Shutdown"
		TYPE="${IOS_DEVICE_TYPE_ID}"
		created=true
	fi
	if [[ "${TYPE}" == "-" ]]; then
		type_verified=false
		log "simctl did not report the device type of ${UDID}: ${IOS_DEVICE_TYPE_ID} from the lock is not verified for it."
	elif [[ "${TYPE}" != "${IOS_DEVICE_TYPE_ID}" ]]; then
		fail "The simulator ${IOS_SIMULATOR_NAME} (${UDID}) is of type ${TYPE}, the lock pins ${IOS_DEVICE_TYPE_ID}; delete it so that ensure creates the pinned one"
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
	if ! apply_settings; then
		# A simulator that was already booted may be up but not answering (`defaults write` cannot reach its domains): one
		# recovery, recorded in the result. A simulator this run booted, or one that fails again, is the environment's fault.
		[[ "${booted_by_harness}" == "false" ]] || fail "The simulator ${UDID} refused its settings right after the harness booted it"
		log "The settings could not be applied to the booted simulator ${UDID}; shutting it down, booting it and trying once more."
		xcrun simctl shutdown "${UDID}" || fail "simctl shutdown ${UDID} failed during the recovery"
		boot_simulator
		recovered=true
		apply_settings || fail "The simulator ${UDID} refused its settings again after one recovery"
	fi
	model="$(simctl_query devicetypes "next((t['name'] for t in data['devicetypes'] if t['identifier'] == '${IOS_DEVICE_TYPE_ID}'), '')")"
	emit_json "id=s:${UDID}" "model=s:${model}" "created=j:${created}" "bootedByHarness=j:${booted_by_harness}" \
		"recoveredAtEnsure=j:${recovered}" "deviceTypeVerified=j:${type_verified}" "bootedAt=j:null" "dataDirGb=j:$(data_gb)" "settings=j:${SETTINGS_JSON}"
}

cmd_health() {
	local locale
	find_simulator || fail "The simulator ${IOS_SIMULATOR_NAME} does not exist"
	[[ "${STATE}" == "Booted" ]] || fail "The simulator ${UDID} is '${STATE}', not Booted"
	# After a couple of hours of runs the simulator's preferences daemon stops answering while the simulator stays Booted: the
	# scenario would spend its whole timeout on a screen that cannot be read. One cheap read of a key `ensure` pinned says so first.
	locale="$(pref_get -g AppleLocale)"
	[[ "${locale}" == "${IOS_LOCALE}" ]] ||
		fail "simulator degraded: ${UDID} stopped serving preferences (defaults read -g AppleLocale answered '${locale}', expected '${IOS_LOCALE}')"
	emit_json "ok=j:true" "id=s:${UDID}"
}

cmd_recover() {
	find_simulator || fail "The simulator ${IOS_SIMULATOR_NAME} does not exist"
	if [[ "${STATE}" == "Booted" ]]; then
		xcrun simctl shutdown "${UDID}"
	fi
	boot_simulator
	apply_settings || fail "The simulator ${UDID} refused its settings after the recovery"
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
