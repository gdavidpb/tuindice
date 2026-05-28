package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SummaryFailedViewUiTest {
	@Test
	fun when_retryButtonTapped_then_invokesRetryCallback() = runTuIndiceUiTest {
		var retryClicks = 0

		setTuIndiceTestContent {
			SummaryFailedView(
				title = "No pudimos cargar el resumen",
				message = "Intenta nuevamente en unos segundos",
				retryText = "Reintentar",
				onRetryClick = { retryClicks++ }
			)
		}

		assertNodeVisible(BaseUiTags.ErrorViewContainer)
		assertNodeVisible(BaseUiTags.ErrorViewTitle)
		assertNodeVisible(BaseUiTags.ErrorViewMessage)
		assertNodeVisible(BaseUiTags.ErrorViewRetryButton)

		onNodeWithTag(BaseUiTags.ErrorViewRetryButton).performClick()

		assertEquals(1, retryClicks)
	}

	@Test
	fun when_headerContentProvided_then_rendersHeaderSlot() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryFailedView(
				title = "No pudimos cargar el resumen",
				message = "Intenta nuevamente en unos segundos",
				retryText = "Reintentar",
				onRetryClick = {},
				headerContent = {
					Text("Header slot")
				}
			)
		}

		onNodeWithText("Header slot").assertIsDisplayed()
	}
}
