#!/usr/bin/env bash
# Android device policy: the pinned AVD from e2e/toolchain/android.lock.
#
# Verbs (JSON on stdout, diagnostics on stderr, exit 3 when the environment is not right):
#   toolchain  what the lock pins, read from files only (no device is touched)
#   ensure     verify the AVD, boot it without a snapshot if it is not online, readiness gate, settings
#   serial     the adb serial of the pinned emulator
#   health     fast probe used before every scenario: boot, hide_error_dialogs, no ANR, device load < 6.0
#   recover    reboot the pinned emulator and run the gate and the settings again
#   stop       shut down only the pinned emulator
# An emulator that is already online is never restarted by `ensure`; restarting is only recovery.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
source "${SCRIPT_DIR}/../shared/lib.sh"

LOCK_FILE="${E2E_FAKE_LOCK_FILE:-${REPO_ROOT}/e2e/toolchain/android.lock}"
LOCK_KEYS=(ANDROID_AVD_NAME ANDROID_SYSTEM_IMAGE_DIR ANDROID_SYSTEM_IMAGE_REVISION ANDROID_API_LEVEL
	ANDROID_DEVICE_PROFILE ANDROID_LOCALE ANDROID_EMULATOR_MEMORY_MB ANDROID_EMULATOR_CORES ANDROID_EMULATOR_HEADLESS)
# Provisional thresholds; revise from `e2e-profile.py --compare`.
DEVICE_LOAD_LIMIT="6.0"
GATE_TIMEOUT_SECONDS="${E2E_FAKE_GATE_SECONDS:-240}"
GATE_POLL_SECONDS="${E2E_FAKE_GATE_POLL_SECONDS:-5}"
HEALTH_LOAD_WAIT_SECONDS="${E2E_FAKE_HEALTH_LOAD_WAIT_SECONDS:-120}"
MEMORY_TOLERANCE_PERCENT=80 # the guest kernel reports less than the configured memory
# namespace key value
SETTINGS=("global window_animation_scale 0" "global transition_animation_scale 0" "global animator_duration_scale 0"
	"secure spell_checker_enabled 0" "secure autofill_service null" "secure show_ime_with_hard_keyboard 0")

fail() {
	printf '%s\n' "$*" >&2
	exit 3
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

resolve_sdk() {
	if [[ -n "${ANDROID_HOME:-}" ]]; then
		printf '%s\n' "${ANDROID_HOME}"
	elif [[ -n "${ANDROID_SDK_ROOT:-}" ]]; then
		printf '%s\n' "${ANDROID_SDK_ROOT}"
	elif [[ -f "${REPO_ROOT}/local.properties" ]] && grep -q '^sdk\.dir=' "${REPO_ROOT}/local.properties"; then
		awk -F= '/^sdk\.dir=/ { print $2; exit }' "${REPO_ROOT}/local.properties"
	else
		printf '%s/Library/Android/sdk\n' "${HOME}"
	fi
}

ini_value() { # file key
	[[ -f "$1" ]] || return 0
	awk -F= -v key="$2" '$1 == key { sub(/^[^=]*=/, ""); print; exit }' "$1"
}

# Sets AVD_CONFIG to the AVD's config.ini; false when the AVD does not exist.
locate_avd() {
	local ini="${ANDROID_AVD_HOME:-${HOME}/.android/avd}/${ANDROID_AVD_NAME}.ini"
	[[ -f "${ini}" ]] || return 1
	AVD_CONFIG="$(ini_value "${ini}" path)/config.ini"
	[[ -f "${AVD_CONFIG}" ]]
}

find_avd() {
	locate_avd || fail "The AVD ${ANDROID_AVD_NAME} does not exist or is incomplete. The harness never creates it; run: avdmanager create avd -n ${ANDROID_AVD_NAME} -k '${ANDROID_SYSTEM_IMAGE_DIR//\//;}' -d ${ANDROID_DEVICE_PROFILE}"
}

image_dir() { # prints the image directory of the AVD without the trailing slash
	local dir
	dir="$(ini_value "${AVD_CONFIG}" image.sysdir.1)"
	printf '%s\n' "${dir%/}"
}

adb_s() {
	adb -s "${SERIAL}" "$@"
}

# The output of an adb shell command without carriage returns; nothing when the command fails.
adb_out() {
	local out
	if out="$(adb_s shell "$@" 2> /dev/null)"; then
		printf '%s\n' "${out//$'\r'/}"
	fi
}

# Sets SERIAL to the online emulator running the pinned AVD; false when there is none.
find_serial() {
	local serial state
	while read -r serial state; do
		[[ "${serial}" == emulator-* && "${state}" == "device" ]] || continue
		if [[ "$(adb -s "${serial}" emu avd name 2>/dev/null | tr -d '\r' | head -n 1)" == "${ANDROID_AVD_NAME}" ]]; then
			SERIAL="${serial}"
			return 0
		fi
	done < <(adb devices 2>/dev/null | tail -n +2)
	return 1
}

device_load() {
	adb_out cat /proc/loadavg | awk '{ print $1 }'
}

load_is_low() {
	awk -v load="$1" -v limit="${DEVICE_LOAD_LIMIT}" 'BEGIN { exit !(load != "" && load < limit) }'
}

has_anr_window() {
	[[ "$(adb_out dumpsys window windows)" == *"Application Not Responding"* ]]
}

boot_emulator() {
	local emulator args log_dir="${E2E_TMP_ROOT:-${TMPDIR:-/tmp}/tuindice-e2e}/android"
	emulator="$(resolve_sdk)/emulator/emulator"
	[[ -x "${emulator}" ]] || fail "The emulator binary ${emulator} was not found"
	args=(-avd "${ANDROID_AVD_NAME}" -no-snapshot -no-boot-anim -no-audio
		-memory "${ANDROID_EMULATOR_MEMORY_MB}" -cores "${ANDROID_EMULATOR_CORES}")
	[[ "${ANDROID_EMULATOR_HEADLESS}" != "1" ]] || args+=(-no-window)
	mkdir -p "${log_dir}"
	log "Booting ${ANDROID_AVD_NAME}: emulator ${args[*]}"
	nohup "${emulator}" "${args[@]}" > "${log_dir}/emulator.log" 2>&1 < /dev/null &
	disown
}

# Readiness gate: boot completed, error dialogs hidden, then two consecutive good samples.
readiness_gate() {
	local deadline=$((SECONDS + GATE_TIMEOUT_SECONDS)) good=0 load
	# With -no-boot-anim the bootanim service does not exist and its property stays unset: only "running" blocks.
	until find_serial && [[ "$(adb_out getprop sys.boot_completed)" == "1" ]] &&
		[[ "$(adb_out getprop init.svc.bootanim)" != "running" ]]; do
		(( SECONDS < deadline )) || fail "The emulator did not finish booting within ${GATE_TIMEOUT_SECONDS}s"
		sleep "${GATE_POLL_SECONDS}"
	done
	adb_s shell settings put global hide_error_dialogs 1
	while (( good < 2 )); do
		load="$(device_load)"
		if [[ "$(adb_out pm path android)" == package:* ]] && ! has_anr_window &&
			[[ "$(adb_out dumpsys activity activities)" == *ResumedActivity* ]] && load_is_low "${load}"; then
			good=$((good + 1))
		else
			good=0
		fi
		if (( good < 2 )); then
			(( SECONDS < deadline )) || fail "The emulator was not ready within ${GATE_TIMEOUT_SECONDS}s (device load ${load:-unknown}, limit ${DEVICE_LOAD_LIMIT}; an ANR window, no resumed activity or no package manager also block)"
			sleep "${GATE_POLL_SECONDS}"
		fi
	done
}

apply_settings() { # sets SETTINGS_JSON; exits 3 on a read-back mismatch
	local entry namespace key value actual pairs=()
	for entry in "${SETTINGS[@]}"; do
		read -r namespace key value <<< "${entry}"
		adb_s shell settings put "${namespace}" "${key}" "${value}"
		actual="$(adb_out settings get "${namespace}" "${key}")"
		[[ "${actual}" == "${value}" ]] || fail "Setting ${namespace} ${key} reads back '${actual}', expected '${value}'"
		pairs+=("${namespace}.${key}=s:${actual}")
	done
	SETTINGS_JSON="$(emit_json "${pairs[@]}")"
}

# The emulator that is online must have the pinned shape; flags only matter at boot, so they are measured here.
verify_shape() {
	local value memory_kb
	value="$(adb_out getprop ro.build.version.sdk)"
	[[ "${value}" == "${ANDROID_API_LEVEL}" ]] || fail "The device API level is '${value}', the lock pins ${ANDROID_API_LEVEL}"
	value="$(adb_out getprop persist.sys.locale)"
	[[ -n "${value}" ]] || value="$(adb_out getprop ro.product.locale)"
	case "${value}" in
		"${ANDROID_LOCALE}" | "${ANDROID_LOCALE}"[-_]*) ;;
		*) fail "The device locale is '${value}', the lock pins ${ANDROID_LOCALE}" ;;
	esac
	value="$(adb_out nproc)"
	[[ "${value}" == "${ANDROID_EMULATOR_CORES}" ]] || fail "The emulator has ${value} cores, the lock pins ${ANDROID_EMULATOR_CORES}; stop it and run ensure again"
	memory_kb="$(adb_out cat /proc/meminfo | awk '/^MemTotal:/ { print $2; exit }')"
	awk -v kb="${memory_kb}" -v mb="${ANDROID_EMULATOR_MEMORY_MB}" -v pct="${MEMORY_TOLERANCE_PERCENT}" \
		'BEGIN { exit !(kb / 1024 >= mb * pct / 100) }' ||
		fail "The emulator reports ${memory_kb} kB of memory, the lock pins ${ANDROID_EMULATOR_MEMORY_MB} MB; stop it and run ensure again"
}

tunnel() {
	local port="${E2E_ANDROID_WIREMOCK_PORT:-18626}"
	[[ "${E2E_ANDROID_TUNNEL:-host-alias}" == "reverse" ]] || return 0
	adb_s reverse "tcp:${port}" "tcp:${port}" > /dev/null
	[[ "$(adb_s reverse --list)" == *"tcp:${port}"* ]] || fail "The adb reverse tunnel for tcp:${port} is not in place"
}

wait_until_gone() { # seconds
	local deadline=$((SECONDS + $1))
	while find_serial; do
		(( SECONDS < deadline )) || fail "The emulator ${SERIAL} did not go away within $1s"
		sleep 1
	done
}

verify_avd() {
	local value
	find_avd
	value="$(image_dir)"
	[[ "${value}" == "${ANDROID_SYSTEM_IMAGE_DIR}" ]] || fail "The AVD uses the image '${value}', the lock pins ${ANDROID_SYSTEM_IMAGE_DIR}"
	value="$(ini_value "${AVD_CONFIG}" hw.device.name)"
	[[ "${value}" == "${ANDROID_DEVICE_PROFILE}" ]] || fail "The AVD device profile is '${value}', the lock pins ${ANDROID_DEVICE_PROFILE}"
	value="$(ini_value "$(resolve_sdk)/${ANDROID_SYSTEM_IMAGE_DIR}/source.properties" Pkg.Revision)"
	[[ "${value}" == "${ANDROID_SYSTEM_IMAGE_REVISION}" ]] || fail "The installed system image is revision '${value}', the lock pins ${ANDROID_SYSTEM_IMAGE_REVISION}"
}

first_line_of() { # the first output line of a command; nothing when it fails
	local out
	if out="$("$@" 2> /dev/null)"; then
		printf '%s\n' "${out%%$'\n'*}"
	fi
}

# Files only: no device is touched. Keys that need the running device are null and `ensure` verifies them.
cmd_toolchain() {
	local avd="" image="" profile="" properties
	if locate_avd; then
		avd="${ANDROID_AVD_NAME}"
		image="$(image_dir)"
		profile="$(ini_value "${AVD_CONFIG}" hw.device.name)"
	fi
	properties="$(resolve_sdk)/${image}/source.properties"
	emit_json "ANDROID_AVD_NAME=s:${avd}" "ANDROID_SYSTEM_IMAGE_DIR=s:${image}" \
		"ANDROID_SYSTEM_IMAGE_REVISION=s:$(ini_value "${properties}" Pkg.Revision)" \
		"ANDROID_API_LEVEL=s:$(ini_value "${properties}" AndroidVersion.ApiLevel | cut -d. -f1)" \
		"ANDROID_DEVICE_PROFILE=s:${profile}" "ANDROID_LOCALE=j:null" "ANDROID_EMULATOR_MEMORY_MB=j:null" \
		"ANDROID_EMULATOR_CORES=j:null" "ANDROID_EMULATOR_HEADLESS=j:null" \
		"adb=s:$(first_line_of adb version)" "emulator=s:$(first_line_of "$(resolve_sdk)/emulator/emulator" -version)"
}

cmd_ensure() {
	local booted_by_harness=false data_gb model booted_at
	verify_avd
	if ! find_serial; then
		boot_emulator
		booted_by_harness=true
	fi
	readiness_gate
	apply_settings
	verify_shape
	tunnel
	model="$(adb_out getprop ro.product.model)"
	data_gb="$(du -sk "${AVD_CONFIG%/*}" | awk '{ printf "%.2f", $1 / 1048576 }')"
	booted_at="$(python3 -c 'import datetime, sys; print((datetime.datetime.now(datetime.timezone.utc) - datetime.timedelta(seconds=float(sys.argv[1]))).strftime("%Y-%m-%dT%H:%M:%SZ"))' \
		"$(adb_out cat /proc/uptime | awk '{ print $1 }')")"
	emit_json "id=s:${SERIAL}" "model=s:${model}" "bootedAt=s:${booted_at}" "bootedByHarness=j:${booted_by_harness}" \
		"dataDirGb=j:${data_gb}" "settings=j:${SETTINGS_JSON}"
}

cmd_health() {
	local load waited_from="${SECONDS}" deadline=$((SECONDS + HEALTH_LOAD_WAIT_SECONDS))
	find_avd
	find_serial || fail "No online emulator runs ${ANDROID_AVD_NAME}"
	[[ "$(adb_out getprop sys.boot_completed)" == "1" ]] || fail "sys.boot_completed is not 1"
	[[ "$(adb_out settings get global hide_error_dialogs)" == "1" ]] || fail "hide_error_dialogs is not 1"
	! has_anr_window || fail "An Application Not Responding window is showing"
	tunnel
	load="$(device_load)"
	until load_is_low "${load}"; do
		(( SECONDS < deadline )) || fail "The device load ${load:-unknown} did not drop below ${DEVICE_LOAD_LIMIT} within ${HEALTH_LOAD_WAIT_SECONDS}s"
		sleep "${GATE_POLL_SECONDS}"
		load="$(device_load)"
	done
	emit_json "ok=j:true" "deviceLoad1=j:${load}" "loadWaitSeconds=j:$((SECONDS - waited_from))"
}

cmd_recover() {
	find_avd
	find_serial || fail "No online emulator runs ${ANDROID_AVD_NAME} to recover"
	adb_s reboot
	wait_until_gone 90
	readiness_gate
	apply_settings
	verify_shape
	tunnel
	emit_json "ok=j:true" "id=s:${SERIAL}" "settings=j:${SETTINGS_JSON}"
}

cmd_stop() {
	find_avd
	if find_serial; then
		if ! adb_s emu kill > /dev/null 2>&1; then
			log "emu kill reported an error; checking whether ${SERIAL} went away anyway"
		fi
		wait_until_gone 60
	fi
	emit_json "ok=j:true"
}

cmd_serial() {
	find_avd
	find_serial || fail "No online emulator runs ${ANDROID_AVD_NAME}"
	printf '%s\n' "${SERIAL}"
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
