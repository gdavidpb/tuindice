package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.domain.model.AssistantUserMessage
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.testing.AssistantChatFixture
import com.gdavidpb.tuindice.assistant.presentation.testing.NOW_MILLIS
import com.gdavidpb.tuindice.assistant.presentation.testing.RecordingNotificationOfferRepository
import com.gdavidpb.tuindice.assistant.presentation.testing.acceptConsentAction
import com.gdavidpb.tuindice.assistant.presentation.testing.chatTest
import com.gdavidpb.tuindice.assistant.presentation.testing.content
import com.gdavidpb.tuindice.assistant.presentation.testing.openAction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// The card of the notification permission, through the chat's rows: it shows in the empty chat once the consent is
// accepted, and each of its answers hides it for good on the device.
class AssistantChatNotificationOfferTest {
	@Test
	fun acceptingTheConsent_onAnEmptyChat_showsTheOffer_andMarksItOffered() = chatTest { fixture, viewModel ->
		viewModel.openAction(null)
		viewModel.acceptConsentAction()

		assertTrue(viewModel.content().notificationOffer)
		assertEquals(1, fixture.notificationOffers.marks)
	}

	@Test
	fun theOffer_isNotShown_whenThePlatformAsksForNothing() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.notificationPermission.platformAsks = false

		viewModel.openAction(null)

		assertFalse(viewModel.content().notificationOffer)
		assertEquals(0, fixture.notificationOffers.marks)
	}

	@Test
	fun activate_hidesTheOffer_andAsksThePlatformForThePermission() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		viewModel.openAction(null)

		viewModel.dispatch(AssistantChat.Action.ActivateNotifications)

		// The route asks the platform when it takes the effect; the machine only sends it.
		assertFalse(viewModel.content().notificationOffer)
		assertEquals(listOf<AssistantChat.Effect>(AssistantChat.Effect.RequestNotificationPermission), fixture.effects)
	}

	@Test
	fun dismiss_hidesTheOffer_andAsksForNothing() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		viewModel.openAction(null)

		viewModel.dispatch(AssistantChat.Action.DismissNotificationOffer)

		assertFalse(viewModel.content().notificationOffer)
		assertTrue(fixture.effects.isEmpty())
	}

	@Test
	fun theOffer_isNotShownAgain_onAnotherSheetOfTheSameDevice() {
		val offers = RecordingNotificationOfferRepository()

		chatTest(AssistantChatFixture(notificationOffers = offers)) { fixture, viewModel ->
			fixture.consent.setAssistantConsentGranted(true)
			viewModel.openAction(null)
			viewModel.dispatch(AssistantChat.Action.DismissNotificationOffer)
		}

		chatTest(AssistantChatFixture(notificationOffers = offers)) { fixture, viewModel ->
			fixture.consent.setAssistantConsentGranted(true)

			viewModel.openAction(null)

			assertFalse(viewModel.content().notificationOffer)
			assertEquals(1, offers.marks)
		}
	}

	@Test
	fun theOffer_hides_whenTheStudentWritesInTheChat() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		viewModel.openAction(null)
		assertTrue(viewModel.content().notificationOffer)

		fixture.conversation.addUserMessage(
			AssistantUserMessage(id = "u1", text = "hola", createdAtMillis = NOW_MILLIS)
		)

		assertFalse(viewModel.content().notificationOffer)
	}
}
