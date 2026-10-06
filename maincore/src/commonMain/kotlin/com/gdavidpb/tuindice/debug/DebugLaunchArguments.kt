package com.gdavidpb.tuindice.debug

import com.gdavidpb.tuindice.base.domain.model.MainSection

/**
 * The one definition of the `TUINDICE_E2E_*` launch arguments of the debug builds.
 *
 * The Android debug code, the Swift host (through the framework) and the E2E scenarios read
 * the keys from here; nothing else spells them. Release builds never read them: Android ships
 * the reader in the debug source set only and the iOS host reads launch arguments under `#if DEBUG`.
 *
 * Arguments are read on cold start only (`DebugMainActivity` is `singleInstance` and does not
 * handle `onNewIntent`). `MAIN_SECTION` only matters together with a seed and is ignored without one.
 */
data class DebugLaunchArguments(
	val apiBaseUrl: String?,
	val webBaseUrl: String?,
	val networkAvailable: Boolean?,
	val animationsDisabled: Boolean,
	val availabilityNotice: AvailabilityNotice?,
	val sessionSeed: DebugSessionSeed?
) {
	data class AvailabilityNotice(
		val enabled: Boolean,
		val title: String,
		val message: String
	)

	companion object {
		const val PREFIX = "TUINDICE_E2E_"

		/** iOS only; Android takes the API URL from `BuildConfig`. */
		const val API_BASE_URL = "TUINDICE_E2E_API_BASE_URL"

		/** iOS only. */
		const val WEB_BASE_URL = "TUINDICE_E2E_WEB_BASE_URL"
		const val NETWORK_AVAILABLE = "TUINDICE_E2E_NETWORK_AVAILABLE"
		const val DISABLE_ANIMATIONS = "TUINDICE_E2E_DISABLE_ANIMATIONS"
		const val AVAILABILITY_NOTICE_ENABLED = "TUINDICE_E2E_AVAILABILITY_NOTICE_ENABLED"
		const val AVAILABILITY_NOTICE_TITLE = "TUINDICE_E2E_AVAILABILITY_NOTICE_TITLE"
		const val AVAILABILITY_NOTICE_MESSAGE = "TUINDICE_E2E_AVAILABILITY_NOTICE_MESSAGE"
		const val MAIN_SECTION = "TUINDICE_E2E_MAIN_SECTION"

		/** The five identity values go together or not at all. */
		const val SEED_SESSION_ID = "TUINDICE_E2E_SEED_SESSION_ID"
		const val SEED_ACCESS_TOKEN = "TUINDICE_E2E_SEED_ACCESS_TOKEN"
		const val SEED_REFRESH_TOKEN = "TUINDICE_E2E_SEED_REFRESH_TOKEN"
		const val SEED_USB_ID = "TUINDICE_E2E_SEED_USB_ID"
		const val SEED_PASSWORD = "TUINDICE_E2E_SEED_PASSWORD"

		/** `seen` (default) or `pending`. */
		const val SEED_COACHMARKS = "TUINDICE_E2E_SEED_COACHMARKS"

		/** Legacy Maestro seed, `authenticatedCoachmarksSeen` or `authenticatedCoachmarksPending`. */
		const val SEED_STATE = "TUINDICE_E2E_SEED_STATE"

		const val COACHMARKS_SEEN = "seen"
		const val COACHMARKS_PENDING = "pending"
		const val LEGACY_STATE_COACHMARKS_SEEN = "authenticatedCoachmarksSeen"
		const val LEGACY_STATE_COACHMARKS_PENDING = "authenticatedCoachmarksPending"

		private val identityKeys = listOf(
			SEED_SESSION_ID,
			SEED_ACCESS_TOKEN,
			SEED_REFRESH_TOKEN,
			SEED_USB_ID,
			SEED_PASSWORD
		)

		val keys: List<String> = listOf(
			API_BASE_URL,
			WEB_BASE_URL,
			NETWORK_AVAILABLE,
			DISABLE_ANIMATIONS,
			AVAILABILITY_NOTICE_ENABLED,
			AVAILABILITY_NOTICE_TITLE,
			AVAILABILITY_NOTICE_MESSAGE,
			MAIN_SECTION,
			SEED_COACHMARKS,
			SEED_STATE
		) + identityKeys

		/**
		 * Parses raw launch values. Blank values count as absent, booleans are strict, a key with
		 * the prefix that is not declared fails, and a partial or ambiguous seed fails.
		 */
		fun parse(values: Map<String, String>): DebugLaunchArguments {
			val unknown = values.keys.filter { it.startsWith(PREFIX) && it !in keys }

			require(unknown.isEmpty()) { "Unknown launch arguments: ${unknown.sorted()}" }

			val present = values.filterKeys { it in keys }
				.mapValues { (_, value) -> value.trim() }
				.filterValues { it.isNotEmpty() }

			val notice = present[AVAILABILITY_NOTICE_ENABLED]?.let { enabled ->
				AvailabilityNotice(
					enabled = parseBoolean(AVAILABILITY_NOTICE_ENABLED, enabled),
					title = present[AVAILABILITY_NOTICE_TITLE].orEmpty(),
					message = present[AVAILABILITY_NOTICE_MESSAGE].orEmpty()
				)
			}

			return DebugLaunchArguments(
				apiBaseUrl = present[API_BASE_URL],
				webBaseUrl = present[WEB_BASE_URL],
				networkAvailable = present[NETWORK_AVAILABLE]?.let { parseBoolean(NETWORK_AVAILABLE, it) },
				animationsDisabled = present[DISABLE_ANIMATIONS]?.let { parseBoolean(DISABLE_ANIMATIONS, it) } ?: false,
				availabilityNotice = notice,
				sessionSeed = parseSeed(present)
			)
		}

		private fun parseSeed(present: Map<String, String>): DebugSessionSeed? {
			val explicitKeys = identityKeys + SEED_COACHMARKS
			val hasExplicitSeed = explicitKeys.any { it in present }
			val legacyState = present[SEED_STATE]

			require(legacyState == null || !hasExplicitSeed) {
				"$SEED_STATE cannot be combined with explicit seed values."
			}

			val mainSection = present[MAIN_SECTION]?.let(::parseMainSection) ?: MainSection.SUMMARY

			return when {
				legacyState != null -> legacySeed(legacyState, mainSection)
				hasExplicitSeed -> explicitSeed(present, mainSection)
				else -> null
			}
		}

		private fun legacySeed(state: String, mainSection: MainSection): DebugSessionSeed {
			return DebugSessionSeed.Canonical.copy(
				coachmarksSeen = when (state) {
					LEGACY_STATE_COACHMARKS_SEEN -> true
					LEGACY_STATE_COACHMARKS_PENDING -> false
					else -> throw IllegalArgumentException("Unsupported $SEED_STATE: $state")
				},
				mainSection = mainSection
			)
		}

		private fun explicitSeed(present: Map<String, String>, mainSection: MainSection): DebugSessionSeed {
			val missing = identityKeys.filter { it !in present }

			require(missing.isEmpty()) { "A seeded session also needs: $missing" }

			return DebugSessionSeed(
				sessionId = present.getValue(SEED_SESSION_ID),
				accessToken = present.getValue(SEED_ACCESS_TOKEN),
				refreshToken = present.getValue(SEED_REFRESH_TOKEN),
				usbId = present.getValue(SEED_USB_ID),
				password = present.getValue(SEED_PASSWORD),
				coachmarksSeen = when (val coachmarks = present[SEED_COACHMARKS] ?: COACHMARKS_SEEN) {
					COACHMARKS_SEEN -> true
					COACHMARKS_PENDING -> false
					else -> throw IllegalArgumentException("Unsupported $SEED_COACHMARKS: $coachmarks")
				},
				mainSection = mainSection
			)
		}

		private fun parseBoolean(key: String, value: String): Boolean {
			return when (value.lowercase()) {
				"true", "1", "yes" -> true
				"false", "0", "no" -> false
				else -> throw IllegalArgumentException("Unsupported boolean for $key: $value")
			}
		}

		private fun parseMainSection(value: String): MainSection {
			return MainSection.entries.firstOrNull { it.name == value }
				?: throw IllegalArgumentException("Unsupported $MAIN_SECTION: $value")
		}
	}
}
