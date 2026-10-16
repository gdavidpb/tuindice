package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsEmptyWordsUiTest {
	@Test
	fun when_aTitleAndAMessageAreGiven_then_BothAreWritten() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WordsHost(title = "Sin notas aún", actionText = null, onActionClick = {})
		}
		waitForIdle()

		onNodeWithText("Sin notas aún").assertIsDisplayed()
		onNodeWithText("Cuando tengas notas, aparecerán aquí.").assertIsDisplayed()
	}

	@Test
	fun when_anActionIsGiven_then_ItIsAButtonThatRunsIt() = runTuIndiceUiTest {
		var clicked = false

		setTuIndiceTestContent {
			WordsHost(title = null, actionText = "Ver registro", onActionClick = { clicked = true })
		}
		waitForIdle()

		onNodeWithTag(SummaryUiTags.StatisticsEmptyAction).performClick()

		assertTrue(clicked)
	}

	@Composable
	private fun WordsHost(title: String?, actionText: String?, onActionClick: () -> Unit) {
		Column {
			SummaryStatisticsEmptyWords(
				message = "Cuando tengas notas, aparecerán aquí.",
				title = title,
				actionText = actionText,
				onActionClick = onActionClick
			)
		}
	}
}
