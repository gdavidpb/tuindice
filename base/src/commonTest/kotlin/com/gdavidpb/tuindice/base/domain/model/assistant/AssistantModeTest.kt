package com.gdavidpb.tuindice.base.domain.model.assistant

import kotlin.test.Test
import kotlin.test.assertEquals

class AssistantModeTest {
	@Test
	fun fromRemoteValue_readsTheValidValues() {
		assertEquals(expected = AssistantMode.ON, actual = AssistantMode.fromRemoteValue("on"))
		assertEquals(expected = AssistantMode.GUIDED, actual = AssistantMode.fromRemoteValue("guided"))
		assertEquals(expected = AssistantMode.OFF, actual = AssistantMode.fromRemoteValue("off"))
	}

	@Test
	fun fromRemoteValue_ignoresCase() {
		assertEquals(expected = AssistantMode.ON, actual = AssistantMode.fromRemoteValue("ON"))
		assertEquals(expected = AssistantMode.GUIDED, actual = AssistantMode.fromRemoteValue("Guided"))
		assertEquals(expected = AssistantMode.OFF, actual = AssistantMode.fromRemoteValue("oFF"))
	}

	@Test
	fun fromRemoteValue_anyOtherValueIsOff() {
		assertEquals(expected = AssistantMode.OFF, actual = AssistantMode.fromRemoteValue("banana"))
		assertEquals(expected = AssistantMode.OFF, actual = AssistantMode.fromRemoteValue(""))
		assertEquals(expected = AssistantMode.OFF, actual = AssistantMode.fromRemoteValue(" on "))
	}

	@Test
	fun fromRemoteValue_nullIsOff() {
		assertEquals(expected = AssistantMode.OFF, actual = AssistantMode.fromRemoteValue(null))
	}
}
