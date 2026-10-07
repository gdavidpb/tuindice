package com.gdavidpb.tuindice.e2e

import com.gdavidpb.tuindice.debug.DebugLaunchArguments
import com.gdavidpb.tuindice.debug.DebugSessionSeed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class E2eSeedBridgeTest {
	private class RecordingEffects : E2eSeedBridge.Effects {
		val notices = mutableListOf<DebugLaunchArguments.AvailabilityNotice>()
		val networks = mutableListOf<Boolean>()
		val seeds = mutableListOf<DebugSessionSeed>()

		override fun setAvailabilityNotice(notice: DebugLaunchArguments.AvailabilityNotice) {
			notices += notice
		}

		override fun setNetworkAvailable(forced: Boolean) {
			networks += forced
		}

		override fun seedSession(seed: DebugSessionSeed) {
			seeds += seed
		}
	}

	private val seededExtras: Map<String, Any?> = mapOf(
		DebugLaunchArguments.SEED_SESSION_ID to "session",
		DebugLaunchArguments.SEED_ACCESS_TOKEN to "access",
		DebugLaunchArguments.SEED_REFRESH_TOKEN to "refresh",
		DebugLaunchArguments.SEED_USB_ID to "11-11111",
		DebugLaunchArguments.SEED_PASSWORD to "secret",
		DebugLaunchArguments.AVAILABILITY_NOTICE_ENABLED to "true",
		DebugLaunchArguments.AVAILABILITY_NOTICE_TITLE to "Title",
		DebugLaunchArguments.AVAILABILITY_NOTICE_MESSAGE to "Message",
		DebugLaunchArguments.NETWORK_AVAILABLE to "false",
		DebugLaunchArguments.DISABLE_ANIMATIONS to "true"
	)

	@Test
	fun onAColdStart_theSeedAndTheNoticeAreApplied() {
		val effects = RecordingEffects()

		E2eSeedBridge.applyLaunchArguments(seededExtras, isColdStart = true, effects = effects)

		assertEquals(1, effects.seeds.size)
		assertEquals(1, effects.notices.size)
		assertEquals(listOf(false), effects.networks)
	}

	@Test
	fun whenTheActivityIsRecreated_theSeedAndTheNoticeAreNotApplied() {
		val effects = RecordingEffects()

		E2eSeedBridge.applyLaunchArguments(seededExtras, isColdStart = false, effects = effects)

		assertTrue(effects.seeds.isEmpty())
		assertTrue(effects.notices.isEmpty())
	}

	@Test
	fun whenTheActivityIsRecreated_theArgumentsAreStillParsedAndTheNetworkOverrideIsApplied() {
		val effects = RecordingEffects()

		val arguments = E2eSeedBridge.applyLaunchArguments(seededExtras, isColdStart = false, effects = effects)

		assertTrue(arguments.animationsDisabled)
		assertNotNull(arguments.sessionSeed)
		assertEquals(listOf(false), effects.networks)
	}

	@Test
	fun withoutArguments_nothingIsApplied() {
		val effects = RecordingEffects()

		val arguments = E2eSeedBridge.applyLaunchArguments(emptyMap(), isColdStart = true, effects = effects)

		assertFalse(arguments.animationsDisabled)
		assertTrue(effects.seeds.isEmpty() && effects.notices.isEmpty() && effects.networks.isEmpty())
	}

	@Test
	fun extrasOutsideThePrefix_areIgnored() {
		val values = E2eSeedBridge.launchValues(mapOf("android.intent.extra.X" to 1, "TUINDICE_E2E_MAIN_SECTION" to "SUMMARY"))

		assertEquals(mapOf("TUINDICE_E2E_MAIN_SECTION" to "SUMMARY"), values)
	}

	@Test
	fun aNonStringExtra_failsNamingTheKeyAndTheType() {
		val error = assertThrows(IllegalArgumentException::class.java) {
			E2eSeedBridge.applyLaunchArguments(
				mapOf(DebugLaunchArguments.DISABLE_ANIMATIONS to true),
				isColdStart = true,
				effects = RecordingEffects()
			)
		}

		assertTrue(error.message.orEmpty().contains(DebugLaunchArguments.DISABLE_ANIMATIONS))
		assertTrue(error.message.orEmpty().contains("Boolean"))
	}
}
