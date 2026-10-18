package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.domain.exception.AssistantRequestRefusedException
import com.gdavidpb.tuindice.assistant.presentation.model.ChatNoticeItem
import com.gdavidpb.tuindice.assistant.presentation.testing.AssistantChatFixture
import com.gdavidpb.tuindice.assistant.presentation.testing.agentMessage
import com.gdavidpb.tuindice.assistant.presentation.testing.assistantConversation
import com.gdavidpb.tuindice.assistant.presentation.testing.changeTextAction
import com.gdavidpb.tuindice.assistant.presentation.testing.chatTest
import com.gdavidpb.tuindice.assistant.presentation.testing.content
import com.gdavidpb.tuindice.assistant.presentation.testing.openAction
import com.gdavidpb.tuindice.assistant.presentation.testing.pickSuggestionAction
import com.gdavidpb.tuindice.assistant.presentation.testing.sendMessageAction
import com.gdavidpb.tuindice.assistant.presentation.viewmodel.AssistantChatViewModel
import com.gdavidpb.tuindice.assistant.testing.assistantStatus
import com.gdavidpb.tuindice.base.presentation.mapper.DateTextStyle
import com.gdavidpb.tuindice.base.presentation.mapper.formatDate
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.abuse.AbuseStanding
import com.gdavidpb.tuindice.chatcontract.abuse.StandingLevel
import com.gdavidpb.tuindice.chatcontract.error.AssistantErrorCodes
import com.gdavidpb.tuindice.chatcontract.event.RunFinished
import com.gdavidpb.tuindice.chatcontract.message.Text
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.jetbrains.compose.resources.StringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_chat_restricted_banned
import tuindice.assistant.generated.resources.assistant_chat_restricted_cooldown_today
import tuindice.assistant.generated.resources.assistant_chat_restricted_cooldown_until
import tuindice.assistant.generated.resources.assistant_chat_restricted_suspended
import tuindice.assistant.generated.resources.assistant_chat_restricted_suspended_until
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

// The restriction as the student reads it: what it is and until when, whether it can be appealed, and how it is let go
// once its end passes. The clock is noon of a day in the time zone of the device, so «today» is the test's.
class AssistantChatRestrictionBehaviorTest {
	@Test
	fun aPauseThatEndsToday_saysTheHourTheAssistantReturns() = chatTest { fixture, viewModel ->
		openWith(fixture, viewModel, AbuseStanding(level = StandingLevel.COOLDOWN, untilMillis = THIS_AFTERNOON))

		val message = viewModel.restrictedMessage()
		assertEquals(Res.string.assistant_chat_restricted_cooldown_today, message.resource)
		assertEquals(1, message.args.size)
		assertFalse(viewModel.content().composer.isEnabled)
	}

	@Test
	fun aPauseThatEndsAnotherDay_saysTheDayAndTheHour() = chatTest { fixture, viewModel ->
		openWith(fixture, viewModel, AbuseStanding(level = StandingLevel.COOLDOWN, untilMillis = TWO_DAYS_LATER))

		val message = viewModel.restrictedMessage()
		assertEquals(Res.string.assistant_chat_restricted_cooldown_until, message.resource)
		assertEquals(TWO_DAYS_LATER.formatDate(DateTextStyle.DAY_MONTH_YEAR), message.args.first())
		assertEquals(2, message.args.size)
	}

	@Test
	fun aSuspensionWithAnEnd_saysTheDayItEnds() = chatTest { fixture, viewModel ->
		openWith(fixture, viewModel, AbuseStanding(level = StandingLevel.SUSPENDED, untilMillis = TWO_DAYS_LATER))

		val message = viewModel.restrictedMessage()
		assertEquals(Res.string.assistant_chat_restricted_suspended_until, message.resource)
		assertEquals(listOf<Any>(TWO_DAYS_LATER.formatDate(DateTextStyle.DAY_MONTH_YEAR)), message.args)
	}

	@Test
	fun aSuspensionWithoutAnEnd_saysItWithoutAnEnd() = chatTest { fixture, viewModel ->
		openWith(fixture, viewModel, AbuseStanding(level = StandingLevel.SUSPENDED))

		assertEquals(noArgs(Res.string.assistant_chat_restricted_suspended), viewModel.restrictedMessage())
	}

	@Test
	fun aBan_saysTheAccessIsClosed() = chatTest { fixture, viewModel ->
		openWith(fixture, viewModel, AbuseStanding(level = StandingLevel.BANNED))

		assertEquals(noArgs(Res.string.assistant_chat_restricted_banned), viewModel.restrictedMessage())
		assertFalse(viewModel.content().composer.isEnabled)
	}

	@Test
	fun anAppealableStanding_offersTheAppealFromTheOpening() = chatTest { fixture, viewModel ->
		openWith(
			fixture,
			viewModel,
			AbuseStanding(level = StandingLevel.SUSPENDED, untilMillis = TWO_DAYS_LATER, appealable = true)
		)

		assertTrue(assertIs<ChatNoticeItem.Restricted>(viewModel.content().notice).canAppeal)
	}

	@Test
	fun aStandingThatCannotBeAppealed_offersNoAppeal() = chatTest { fixture, viewModel ->
		openWith(fixture, viewModel, AbuseStanding(level = StandingLevel.BANNED, appealable = false))

		assertFalse(assertIs<ChatNoticeItem.Restricted>(viewModel.content().notice).canAppeal)
	}

	@Test
	fun reopening_afterTheEndOfAPause_leavesTheChatOpen() = chatTest { fixture, viewModel ->
		openWith(fixture, viewModel, AbuseStanding(level = StandingLevel.COOLDOWN, untilMillis = NOON - 1))

		assertNull(viewModel.content().notice)
		assertTrue(viewModel.content().composer.isEnabled)
	}

	@Test
	fun aRefusal_whoseStandingNoLongerRestricts_releasesTheChat() = chatTest { fixture, viewModel ->
		openWith(fixture, viewModel, AbuseStanding(level = StandingLevel.NONE))
		fixture.turns.sendError = AssistantRequestRefusedException(AssistantErrorCodes.SUSPENDED, 30, "suspended")
		viewModel.changeTextAction("hola")

		viewModel.sendMessageAction()

		assertNull(viewModel.content().notice)
		assertTrue(viewModel.content().composer.isEnabled)
	}

	@Test
	fun aRestrictionWhoseEndPassed_isNoLongerShown_onTheNextChange() = chatTest { fixture, viewModel ->
		openWith(fixture, viewModel, AbuseStanding(level = StandingLevel.COOLDOWN, untilMillis = NOON + MINUTE))
		assertIs<ChatNoticeItem.Restricted>(viewModel.content().notice)

		fixture.clock.nowMillis = NOON + 2 * MINUTE
		fixture.conversation.conversation.value = assistantConversation(items = listOf(agentMessage("a1", Text("Hola"))))

		assertNull(viewModel.content().notice)
		assertTrue(viewModel.content().composer.isEnabled)
	}

	@Test
	fun aSendAfterTheEnd_readsTheStandingAgain_andTheChatStaysOpen() = chatTest { fixture, viewModel ->
		openWith(fixture, viewModel, AbuseStanding(level = StandingLevel.COOLDOWN, untilMillis = NOON + MINUTE))
		assertIs<ChatNoticeItem.Restricted>(viewModel.content().notice)
		fixture.clock.nowMillis = NOON + 2 * MINUTE
		fixture.standing.answer = assistantStatus()
		fixture.turns.events = listOf(RunFinished(seq = 1, turnId = "t1"))
		val readsBefore = fixture.standing.statusReads

		viewModel.pickSuggestionAction(viewModel.content().suggestions.first().id)

		assertEquals(readsBefore + 1, fixture.standing.statusReads)
		assertEquals(1, fixture.turns.requests.size)
		assertNull(viewModel.content().notice)
		assertTrue(viewModel.content().composer.isEnabled)
	}

	@Test
	fun aSendAfterTheEnd_whoseStandingGivesANewEnd_keepsTheChatPaused() = chatTest { fixture, viewModel ->
		openWith(fixture, viewModel, AbuseStanding(level = StandingLevel.COOLDOWN, untilMillis = NOON + MINUTE))
		fixture.clock.nowMillis = NOON + 2 * MINUTE
		fixture.standing.answer = assistantStatus().copy(
			standing = AbuseStanding(level = StandingLevel.SUSPENDED, untilMillis = TWO_DAYS_LATER)
		)
		fixture.turns.sendError = AssistantRequestRefusedException(AssistantErrorCodes.SUSPENDED, 30, "suspended")

		viewModel.pickSuggestionAction(viewModel.content().suggestions.first().id)

		assertEquals(Res.string.assistant_chat_restricted_suspended_until, viewModel.restrictedMessage().resource)
		assertFalse(viewModel.content().composer.isEnabled)
	}

	private suspend fun openWith(
		fixture: AssistantChatFixture,
		viewModel: AssistantChatViewModel,
		standing: AbuseStanding
	) {
		fixture.consent.setAssistantConsentGranted(true)
		fixture.clock.nowMillis = NOON
		fixture.standing.answer = assistantStatus().copy(standing = standing)
		viewModel.openAction(null)
	}

	private fun AssistantChatViewModel.restrictedMessage(): UiText.Resource {
		return assertIs<UiText.Resource>(assertIs<ChatNoticeItem.Restricted>(content().notice).message)
	}

	private fun noArgs(resource: StringResource): UiText.Resource = UiText.Resource(resource)

	private companion object {
		const val MINUTE = 60_000L

		val NOON = localMillis(day = 18, hour = 12, minute = 0)
		val THIS_AFTERNOON = localMillis(day = 18, hour = 15, minute = 5)
		val TWO_DAYS_LATER = localMillis(day = 20, hour = 9, minute = 30)

		fun localMillis(day: Int, hour: Int, minute: Int): Long {
			return LocalDateTime(year = 2026, month = 10, day = day, hour = hour, minute = minute)
				.toInstant(TimeZone.currentSystemDefault())
				.toEpochMilliseconds()
		}
	}
}
