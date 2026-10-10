package com.gdavidpb.tuindice.ui.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeEnabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import com.gdavidpb.tuindice.ui.MaincoreUiTags
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The screen on its own. What the Lottie illustration draws is not asserted: only that the
 * error illustration is the one placed above the notice.
 */
@OptIn(ExperimentalTestApi::class)
class AppAvailabilityNoticeScreenUiTest {
	@Test
	fun when_noticeIsShown_then_placesEachTextOnItsOwnSlotUnderTheErrorIllustration() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AppAvailabilityNoticeScreen(
				title = "Servicio pausado",
				message = "Estamos en mantenimiento.",
				retryText = "Reintentar",
				onRetryClick = {}
			)
		}

		assertNodeVisible(MaincoreUiTags.AppAvailabilityNoticeScreen)
		assertNodeVisible(BaseUiTags.ErrorStateAnimation)
		assertNodeVisible(MaincoreUiTags.AppAvailabilityNoticeTitle)
		assertNodeVisible(MaincoreUiTags.AppAvailabilityNoticeMessage)
		onNodeWithTag(MaincoreUiTags.AppAvailabilityNoticeTitle).assertTextEquals("Servicio pausado")
		onNodeWithTag(MaincoreUiTags.AppAvailabilityNoticeMessage)
			.assertTextEquals("Estamos en mantenimiento.")
		onNodeWithTag(MaincoreUiTags.AppAvailabilityNoticeRetryButton).assertTextEquals("Reintentar")
	}

	@Test
	fun when_retryIsTapped_then_invokesRetryOncePerTap() = runTuIndiceUiTest {
		var retryClicks = 0

		setTuIndiceTestContent {
			AppAvailabilityNoticeScreen(
				title = "Servicio pausado",
				message = "Estamos en mantenimiento.",
				retryText = "Reintentar",
				onRetryClick = { retryClicks++ }
			)
		}

		assertNodeVisible(MaincoreUiTags.AppAvailabilityNoticeRetryButton)
		assertNodeEnabled(MaincoreUiTags.AppAvailabilityNoticeRetryButton)
		onNodeWithTag(MaincoreUiTags.AppAvailabilityNoticeRetryButton).assertHasClickAction()
		assertEquals(0, retryClicks)

		onNodeWithTag(MaincoreUiTags.AppAvailabilityNoticeRetryButton).performClick()
		assertEquals(1, retryClicks)

		onNodeWithTag(MaincoreUiTags.AppAvailabilityNoticeRetryButton).performClick()
		assertEquals(2, retryClicks)
	}

	@Test
	fun when_noticeChanges_then_replacesItsTextsAndRetriesTheNewNotice() = runTuIndiceUiTest {
		val retried = mutableListOf<String>()
		var notice by mutableStateOf(Notice("Servicio pausado", "Estamos en mantenimiento.", "Reintentar"))

		setTuIndiceTestContent {
			val current = notice

			AppAvailabilityNoticeScreen(
				title = current.title,
				message = current.message,
				retryText = current.retryText,
				onRetryClick = { retried += current.title }
			)
		}

		assertNodeVisible(MaincoreUiTags.AppAvailabilityNoticeTitle)

		runOnIdle {
			notice = Notice("No disponible en tu región", "Vuelve a intentarlo más tarde.", "Comprobar de nuevo")
		}

		onNodeWithTag(MaincoreUiTags.AppAvailabilityNoticeTitle).assertTextEquals("No disponible en tu región")
		onNodeWithTag(MaincoreUiTags.AppAvailabilityNoticeMessage)
			.assertTextEquals("Vuelve a intentarlo más tarde.")
		onNodeWithTag(MaincoreUiTags.AppAvailabilityNoticeRetryButton).assertTextEquals("Comprobar de nuevo")
		onNodeWithText("Servicio pausado").assertDoesNotExist()

		onNodeWithTag(MaincoreUiTags.AppAvailabilityNoticeRetryButton).performClick()
		assertEquals(listOf("No disponible en tu región"), retried)
	}

	private data class Notice(
		val title: String,
		val message: String,
		val retryText: String
	)
}
