package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.mapper.restrictionText
import com.gdavidpb.tuindice.assistant.presentation.model.ChatNoticeItem
import com.gdavidpb.tuindice.assistant.testing.FixedClock
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.abuse.StandingLevel
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalTestApi::class)
class AssistantChatNoticeViewUiTest {
	@Test
	fun when_turnFailedCanRetry_then_retryDispatchesRetryTurn() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantChatNoticeView(
				notice = ChatNoticeItem.TurnFailed(message = FAILED_MESSAGE, canRetry = true),
				onAction = { actions.add(it) }
			)
		}

		assertNodeVisible(AssistantUiTags.NoticeRetry)
		onNodeWithTag(AssistantUiTags.NoticeRetry).performClick()

		assertIs<AssistantChat.Action.RetryTurn>(actions.single())
	}

	@Test
	fun when_restrictionCanBeAppealed_then_appealSendsTheWrittenText() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantChatNoticeView(
				notice = ChatNoticeItem.Restricted(message = UiText.Raw(RESTRICTED_MESSAGE), canAppeal = true),
				onAction = { actions.add(it) }
			)
		}

		assertNodeVisible(AssistantUiTags.AppealAction)
		onNodeWithTag(AssistantUiTags.AppealAction).performClick()
		assertNodeVisible(AssistantUiTags.AppealText)
		onNodeWithTag(AssistantUiTags.AppealText).performTextInput(APPEAL)
		onNodeWithTag(AssistantUiTags.AppealSend).performClick()

		assertEquals(APPEAL, assertIs<AssistantChat.Action.Appeal>(actions.single()).text)
	}

	@Test
	fun when_aPauseEndsToday_then_itSaysTheHourTheAssistantReturns() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatNoticeView(
				notice = restricted(StandingLevel.COOLDOWN, untilMillis = localMillis(day = 18, hour = 15, minute = 5)),
				onAction = {}
			)
		}

		onNodeWithText("Pausamos el asistente por un rato. Vuelve a estar disponible a las 03:05 p. m.")
			.assertIsDisplayed()
	}

	@Test
	fun when_aPauseEndsAnotherDay_then_itSaysTheDayAndTheHour() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatNoticeView(
				notice = restricted(StandingLevel.COOLDOWN, untilMillis = localMillis(day = 20, hour = 9, minute = 30)),
				onAction = {}
			)
		}

		onNodeWithText("Pausamos el asistente por un rato. Vuelve a estar disponible el 20 de octubre 2026 a las 09:30 a. m.")
			.assertIsDisplayed()
	}

	@Test
	fun when_aSuspensionHasAnEnd_then_itSaysTheDayItEnds() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatNoticeView(
				notice = restricted(StandingLevel.SUSPENDED, untilMillis = localMillis(day = 20, hour = 9, minute = 30)),
				onAction = {}
			)
		}

		onNodeWithText("Tu acceso al asistente está suspendido hasta el 20 de octubre 2026.").assertIsDisplayed()
	}

	@Test
	fun when_aSuspensionHasNoEnd_then_itSaysItWithoutAnEnd() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatNoticeView(notice = restricted(StandingLevel.SUSPENDED, untilMillis = null), onAction = {})
		}

		onNodeWithText("Tu acceso al asistente está suspendido.").assertIsDisplayed()
	}

	@Test
	fun when_aBanCannotBeAppealed_then_itSaysTheAccessIsClosed_withoutTheAppeal() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatNoticeView(notice = restricted(StandingLevel.BANNED, untilMillis = null), onAction = {})
		}

		onNodeWithText("Tu acceso al asistente está cerrado.").assertIsDisplayed()
		assertNodeHidden(AssistantUiTags.AppealAction)
	}

	@Test
	fun when_aSuspensionCanBeAppealed_then_itShowsTheAppeal() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatNoticeView(
				notice = restricted(StandingLevel.SUSPENDED, untilMillis = null, canAppeal = true),
				onAction = {}
			)
		}

		assertNodeVisible(AssistantUiTags.AppealAction)
	}

	@Test
	fun when_quotaIsExhausted_then_itShowsItsMessageAsANotice() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatNoticeView(
				notice = ChatNoticeItem.QuotaExhausted(message = QUOTA_MESSAGE),
				onAction = {}
			)
		}

		assertNodeVisible(AssistantUiTags.Notice)
		onNodeWithText(QUOTA_MESSAGE).assertIsDisplayed()
	}

	// The notice of a restriction as the chat builds it, read at noon of the day the test sets in the device's zone.
	private fun restricted(level: StandingLevel, untilMillis: Long?, canAppeal: Boolean = false): ChatNoticeItem {
		val noon = FixedClock(nowMillis = localMillis(day = 18, hour = 12, minute = 0))

		return ChatNoticeItem.Restricted(
			message = restrictionText(level = level, untilMillis = untilMillis, clock = noon),
			canAppeal = canAppeal
		)
	}

	private fun localMillis(day: Int, hour: Int, minute: Int): Long {
		return LocalDateTime(year = 2026, month = 10, day = day, hour = hour, minute = minute)
			.toInstant(TimeZone.currentSystemDefault())
			.toEpochMilliseconds()
	}

	private companion object {
		const val FAILED_MESSAGE = "No pudimos responder. Intenta de nuevo."
		const val RESTRICTED_MESSAGE = "El asistente está en pausa."
		const val APPEAL = "Fue un error"
		const val QUOTA_MESSAGE = "Llegaste al límite de hoy."
	}
}
