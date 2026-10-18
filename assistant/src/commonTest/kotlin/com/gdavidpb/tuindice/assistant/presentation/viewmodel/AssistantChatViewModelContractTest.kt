package com.gdavidpb.tuindice.assistant.presentation.viewmodel

import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.testing.chatTest
import com.gdavidpb.tuindice.assistant.presentation.testing.content
import com.gdavidpb.tuindice.assistant.presentation.testing.openAction
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityKind
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityRef
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AssistantChatViewModelContractTest {
	@Test
	fun initialState_isIdle_untilTheSheetIsOpened() = chatTest { _, viewModel ->
		assertEquals(AssistantChat.State.Idle, viewModel.state.value)
	}

	@Test
	fun open_withConsent_showsTheContent_withTheChipOfTheScreen() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)

		viewModel.openAction(
			AssistantScreenContext(
				screen = AssistantScreen.EVALUATION_DETAIL,
				entities = listOf(AssistantEntityRef(kind = AssistantEntityKind.SUBJECT, id = "CI4325")),
				visibleFields = mapOf("title" to "PARCIAL 1", "points" to "15 / 20", "grade" to "15.00")
			)
		)

		assertTrue(viewModel.state.value is AssistantChat.State.Content)
		assertEquals("screen:EVALUATION_DETAIL · CI4325 PARCIAL 1", viewModel.content().context?.label)
	}

	@Test
	fun open_withoutConsent_asksForIt() = chatTest { _, viewModel ->
		viewModel.openAction(null)

		assertEquals(AssistantChat.State.ConsentRequired, viewModel.state.value)
	}
}
