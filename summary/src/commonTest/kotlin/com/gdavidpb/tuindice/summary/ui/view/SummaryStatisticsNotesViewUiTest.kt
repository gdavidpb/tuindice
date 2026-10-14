package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsNotesViewUiTest {
	@Test
	fun when_notesAreGiven_then_theyAreWrittenOneUnderAnotherInOrder() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsNotesView(
				notes = listOf(words("44 de 46 aprobadas al primer intento"), words("ninguna repetida"))
			)
		}

		onNodeWithText("44 de 46 aprobadas al primer intento").assertIsDisplayed()
		onNodeWithText("ninguna repetida").assertIsDisplayed()
		assertTrue(
			onNodeWithText("44 de 46 aprobadas al primer intento").fetchSemanticsNode().boundsInRoot.top <
				onNodeWithText("ninguna repetida").fetchSemanticsNode().boundsInRoot.top
		)
	}

	@Test
	fun when_oneNoteIsGiven_then_itIsTheOnlyLine() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsNotesView(notes = listOf(words("La más frecuente: 4.")))
		}

		onNodeWithTag(SummaryUiTags.StatisticsNotes).onChildren().assertCountEquals(1)
		onNodeWithText("La más frecuente: 4.").assertIsDisplayed()
	}
}
