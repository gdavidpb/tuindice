package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class CreateTermSearchGuidanceViewUiTest {
	@Test
	fun when_nothingIsTyped_then_onlyTheSuggestedSearchesAreOffered() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CreateTermSearchGuidance(query = "", onExampleClick = {})
		}

		assertNodeVisible(RecordUiTags.CreateSyntheticTermSearchGuidance)
		onNodeWithText("Búsquedas sugeridas").assertIsDisplayed()
		onNodeWithTag(RecordUiTags.createSyntheticTermSearchExample(0)).assertTextEquals("MA1111")
		onNodeWithTag(RecordUiTags.createSyntheticTermSearchExample(1)).assertTextEquals("Física")
		onNodeWithTag(RecordUiTags.createSyntheticTermSearchExample(2)).assertTextEquals("Algoritmos")
		// Nothing was typed, so there is no query to call too short.
		onAllNodesWithText("Agrega un carácter más").assertCountEquals(0)
	}

	@Test
	fun when_aSingleCharacterIsTyped_then_theGuidanceAsksForOneMore_aboveTheSuggestions() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CreateTermSearchGuidance(query = "m", onExampleClick = {})
		}

		onNodeWithText("Agrega un carácter más").assertIsDisplayed()
		onNodeWithText("La búsqueda empieza con al menos 2 caracteres.").assertIsDisplayed()
		onNodeWithText("Búsquedas sugeridas").assertIsDisplayed()
	}

	@Test
	fun when_onlySpacesAreTyped_then_theyDoNotCountAsAQuery() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CreateTermSearchGuidance(query = "   ", onExampleClick = {})
		}

		onAllNodesWithText("Agrega un carácter más").assertCountEquals(0)
		onAllNodesWithText("La búsqueda empieza con al menos 2 caracteres.").assertCountEquals(0)
		onNodeWithText("Búsquedas sugeridas").assertIsDisplayed()
	}

	@Test
	fun when_aSuggestedSearchIsTapped_then_itsTextIsReportedAsTheQuery() = runTuIndiceUiTest {
		val reported = mutableListOf<String>()

		setTuIndiceTestContent {
			CreateTermSearchGuidance(query = "", onExampleClick = { example -> reported += example })
		}

		onNodeWithTag(RecordUiTags.createSyntheticTermSearchExample(1)).performClick()
		onNodeWithTag(RecordUiTags.createSyntheticTermSearchExample(0)).performClick()

		assertEquals(listOf("Física", "MA1111"), reported)
	}
}
