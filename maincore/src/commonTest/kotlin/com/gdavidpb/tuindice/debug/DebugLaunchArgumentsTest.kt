package com.gdavidpb.tuindice.debug

import com.gdavidpb.tuindice.base.domain.model.MainSection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class DebugLaunchArgumentsTest {
	private val identity = mapOf(
		DebugLaunchArguments.SEED_SESSION_ID to "session-1",
		DebugLaunchArguments.SEED_ACCESS_TOKEN to "access-1",
		DebugLaunchArguments.SEED_REFRESH_TOKEN to "refresh-1",
		DebugLaunchArguments.SEED_USB_ID to "22-22222",
		DebugLaunchArguments.SEED_PASSWORD to "secret"
	)

	@Test
	fun emptyValues_parseToNoOverrides() {
		val blanks = DebugLaunchArguments.keys.associateWith { "  " }

		assertEquals(
			DebugLaunchArguments(
				apiBaseUrl = null,
				webBaseUrl = null,
				networkAvailable = null,
				animationsDisabled = false,
				fixedNow = null,
				availabilityNotice = null,
				sessionSeed = null
			),
			DebugLaunchArguments.parse(blanks)
		)
		assertEquals(DebugLaunchArguments.parse(emptyMap()), DebugLaunchArguments.parse(blanks))
	}

	@Test
	fun booleans_acceptTheThreeSpellings() {
		listOf("true", "1", "yes", "TRUE").forEach { value ->
			assertEquals(true, parseNetwork(value))
		}
		listOf("false", "0", "no", "No").forEach { value ->
			assertEquals(false, parseNetwork(value))
		}

		assertFailsWith<IllegalArgumentException> { parseNetwork("maybe") }
		assertFailsWith<IllegalArgumentException> {
			DebugLaunchArguments.parse(mapOf(DebugLaunchArguments.DISABLE_ANIMATIONS to "2"))
		}
	}

	@Test
	fun animationsAndNotice_areParsed() {
		val arguments = DebugLaunchArguments.parse(
			mapOf(
				DebugLaunchArguments.DISABLE_ANIMATIONS to "true",
				DebugLaunchArguments.AVAILABILITY_NOTICE_ENABLED to "true",
				DebugLaunchArguments.AVAILABILITY_NOTICE_TITLE to "Title",
				DebugLaunchArguments.AVAILABILITY_NOTICE_MESSAGE to "Message"
			)
		)

		assertTrue(arguments.animationsDisabled)
		assertEquals(
			DebugLaunchArguments.AvailabilityNotice(enabled = true, title = "Title", message = "Message"),
			arguments.availabilityNotice
		)
	}

	@Test
	fun now_isParsedAsAnInstantWithItsOffset() {
		assertEquals(
			Instant.parse("2026-10-15T12:00:00Z"),
			parseNow("2026-10-15T12:00:00Z")
		)
		assertEquals(
			Instant.parse("2026-10-15T16:00:00Z"),
			parseNow(" 2026-10-15T12:00:00-04:00 ")
		)
		assertNull(parseNow("  "))
		assertNull(DebugLaunchArguments.parse(emptyMap()).fixedNow)
	}

	@Test
	fun now_failsWhenItIsNotAnInstant() {
		val notInstants = listOf(
			"2026-10-15",
			"12:00",
			"yesterday",
			"2026-13-40T00:00:00Z",
			"2026-10-15T12:00:00",
			"1760529600000"
		)

		notInstants.forEach { value ->
			val failure = assertFailsWith<IllegalArgumentException>(value) { parseNow(value) }

			assertTrue(DebugLaunchArguments.NOW in failure.message.orEmpty(), failure.message)
		}
	}

	@Test
	fun unknownPrefixedKey_fails() {
		assertFailsWith<IllegalArgumentException> {
			DebugLaunchArguments.parse(mapOf("TUINDICE_E2E_FAKE" to "1"))
		}
		assertFailsWith<IllegalArgumentException> {
			DebugLaunchArguments.parse(mapOf("TUINDICE_E2E_FAKE" to ""))
		}
	}

	@Test
	fun keysWithoutThePrefix_areIgnored() {
		assertNull(DebugLaunchArguments.parse(mapOf("OTHER" to "1")).networkAvailable)
	}

	@Test
	fun partialSessionSeed_fails() {
		assertFailsWith<IllegalArgumentException> {
			DebugLaunchArguments.parse(identity - DebugLaunchArguments.SEED_PASSWORD)
		}
		assertFailsWith<IllegalArgumentException> {
			DebugLaunchArguments.parse(mapOf(DebugLaunchArguments.SEED_COACHMARKS to "pending"))
		}
	}

	@Test
	fun completeSessionSeed_isParsedWithDefaults() {
		val seed = DebugLaunchArguments.parse(identity).sessionSeed

		assertEquals(
			DebugSessionSeed(
				sessionId = "session-1",
				accessToken = "access-1",
				refreshToken = "refresh-1",
				usbId = "22-22222",
				password = "secret",
				coachmarksSeen = true,
				mainSection = MainSection.SUMMARY
			),
			seed
		)
	}

	@Test
	fun sessionSeed_honorsCoachmarksAndSection() {
		val seed = DebugLaunchArguments.parse(
			identity + mapOf(
				DebugLaunchArguments.SEED_COACHMARKS to "pending",
				DebugLaunchArguments.MAIN_SECTION to "RECORD"
			)
		).sessionSeed

		assertFalse(seed!!.coachmarksSeen)
		assertEquals(MainSection.RECORD, seed.mainSection)
		assertFailsWith<IllegalArgumentException> {
			DebugLaunchArguments.parse(identity + (DebugLaunchArguments.SEED_COACHMARKS to "later"))
		}
		assertFailsWith<IllegalArgumentException> {
			DebugLaunchArguments.parse(identity + (DebugLaunchArguments.MAIN_SECTION to "NOWHERE"))
		}
	}

	@Test
	fun legacySeedState_mapsToTheCanonicalSeed() {
		val seen = DebugLaunchArguments.parse(
			mapOf(DebugLaunchArguments.SEED_STATE to "authenticatedCoachmarksSeen")
		).sessionSeed
		val pending = DebugLaunchArguments.parse(
			mapOf(
				DebugLaunchArguments.SEED_STATE to "authenticatedCoachmarksPending",
				DebugLaunchArguments.MAIN_SECTION to "PENSUM"
			)
		).sessionSeed

		assertEquals(DebugSessionSeed.Canonical, seen)
		assertEquals(
			DebugSessionSeed.Canonical.copy(coachmarksSeen = false, mainSection = MainSection.PENSUM),
			pending
		)
		assertFailsWith<IllegalArgumentException> {
			DebugLaunchArguments.parse(mapOf(DebugLaunchArguments.SEED_STATE to "other"))
		}
	}

	@Test
	fun legacySeedStateWithExplicitSeed_fails() {
		assertFailsWith<IllegalArgumentException> {
			DebugLaunchArguments.parse(identity + (DebugLaunchArguments.SEED_STATE to "authenticatedCoachmarksSeen"))
		}
		assertFailsWith<IllegalArgumentException> {
			DebugLaunchArguments.parse(
				mapOf(
					DebugLaunchArguments.SEED_STATE to "authenticatedCoachmarksSeen",
					DebugLaunchArguments.SEED_COACHMARKS to "seen"
				)
			)
		}
	}

	@Test
	fun everyDeclaredConstant_isListedInKeys() {
		val declared = listOf(
			DebugLaunchArguments.API_BASE_URL,
			DebugLaunchArguments.WEB_BASE_URL,
			DebugLaunchArguments.NETWORK_AVAILABLE,
			DebugLaunchArguments.DISABLE_ANIMATIONS,
			DebugLaunchArguments.NOW,
			DebugLaunchArguments.AVAILABILITY_NOTICE_ENABLED,
			DebugLaunchArguments.AVAILABILITY_NOTICE_TITLE,
			DebugLaunchArguments.AVAILABILITY_NOTICE_MESSAGE,
			DebugLaunchArguments.MAIN_SECTION,
			DebugLaunchArguments.SEED_SESSION_ID,
			DebugLaunchArguments.SEED_ACCESS_TOKEN,
			DebugLaunchArguments.SEED_REFRESH_TOKEN,
			DebugLaunchArguments.SEED_USB_ID,
			DebugLaunchArguments.SEED_PASSWORD,
			DebugLaunchArguments.SEED_COACHMARKS,
			DebugLaunchArguments.SEED_STATE
		)

		assertEquals(declared.toSet(), DebugLaunchArguments.keys.toSet())
		assertEquals(declared.size, DebugLaunchArguments.keys.size)
		assertTrue(declared.all { it.startsWith(DebugLaunchArguments.PREFIX) })
	}

	private fun parseNetwork(value: String): Boolean? {
		return DebugLaunchArguments.parse(
			mapOf(DebugLaunchArguments.NETWORK_AVAILABLE to value)
		).networkAvailable
	}

	private fun parseNow(value: String): Instant? {
		return DebugLaunchArguments.parse(mapOf(DebugLaunchArguments.NOW to value)).fixedNow
	}
}
