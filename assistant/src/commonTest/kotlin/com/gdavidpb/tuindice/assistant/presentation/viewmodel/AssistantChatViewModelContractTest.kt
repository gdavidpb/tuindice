package com.gdavidpb.tuindice.assistant.presentation.viewmodel

import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.testing.changeTextAction
import com.gdavidpb.tuindice.assistant.presentation.testing.chatTest
import com.gdavidpb.tuindice.assistant.presentation.testing.openAction
import com.gdavidpb.tuindice.assistant.presentation.testing.sendMessageAction
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityKind
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityRef
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AssistantChatViewModelContractTest {
	@Test
	fun initialState_isIdle_untilTheChatIsOpened() = chatTest { _, viewModel ->
		assertEquals(AssistantChat.State.Idle, viewModel.state.value)
	}

	@Test
	fun open_withConsent_sendsTheTurnsWithTheScreenContext() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)

		val context = AssistantScreenContext(
			screen = AssistantScreen.EVALUATION_DETAIL,
			entities = listOf(AssistantEntityRef(kind = AssistantEntityKind.SUBJECT, id = "CI4325")),
			visibleFields = mapOf("title" to "PARCIAL 1", "points" to "15 / 20", "grade" to "15.00")
		)
		viewModel.openAction(context)

		assertTrue(viewModel.state.value is AssistantChat.State.Content)

		viewModel.changeTextAction("hola")
		viewModel.sendMessageAction()

		assertEquals(context.visibleFields, fixture.turns.requests.single().screenContext?.visibleFields)
	}

	@Test
	fun open_withoutConsent_asksForIt() = chatTest { _, viewModel ->
		viewModel.openAction(null)

		assertEquals(AssistantChat.State.ConsentRequired, viewModel.state.value)
	}
}
