package com.gdavidpb.tuindice.base.domain.model.assistant

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AssistantAvailabilityTest {
	@Test
	fun effectiveMode_isTheConfiguredModeWhenTheServerDoesNotDisableIt() {
		val availability = AssistantAvailability(mode = AssistantMode.GUIDED, serverDisabled = false)

		assertEquals(expected = AssistantMode.GUIDED, actual = availability.effectiveMode)
		assertTrue(actual = availability.isVisible)
	}

	@Test
	fun effectiveMode_isOffWhenTheServerDisablesTheAssistant() {
		val availability = AssistantAvailability(mode = AssistantMode.ON, serverDisabled = true)

		assertEquals(expected = AssistantMode.OFF, actual = availability.effectiveMode)
		assertFalse(actual = availability.isVisible)
	}

	@Test
	fun isVisible_isFalseWhenTheModeIsOff() {
		val availability = AssistantAvailability(mode = AssistantMode.OFF, serverDisabled = false)

		assertFalse(actual = availability.isVisible)
	}
}
