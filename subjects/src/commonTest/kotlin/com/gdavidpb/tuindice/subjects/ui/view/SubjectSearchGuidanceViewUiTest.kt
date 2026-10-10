package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SubjectSearchGuidanceViewUiTest {
	@Test
	fun when_queryIsBlank_then_displaysExamplesWithoutMinimumLengthHint() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectSearchGuidanceView(
				query = "   ",
				onExampleClick = {}
			)
		}

		assertNodeVisible(SubjectsUiTags.SearchGuidance)
		onNodeWithText("Búsquedas sugeridas").assertIsDisplayed()
		onAllNodesWithText("Agrega un carácter más").assertCountEquals(0)
		onAllNodesWithText("La búsqueda empieza con al menos 2 caracteres.").assertCountEquals(0)
	}

	@Test
	fun when_queryHasDraftCharacter_then_displaysMinimumLengthHint() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectSearchGuidanceView(
				query = " m ",
				onExampleClick = {}
			)
		}

		onNodeWithText("Agrega un carácter más").assertIsDisplayed()
		onNodeWithText("La búsqueda empieza con al menos 2 caracteres.").assertIsDisplayed()
		onNodeWithText("Búsquedas sugeridas").assertIsDisplayed()
	}

	@Test
	fun when_rendered_then_displaysTheThreeSuggestedSearches() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectSearchGuidanceView(
				query = "",
				onExampleClick = {}
			)
		}

		onNodeWithTag(SubjectsUiTags.searchExample(0)).assertTextEquals("MA1111")
		onNodeWithTag(SubjectsUiTags.searchExample(1)).assertTextEquals("Física")
		onNodeWithTag(SubjectsUiTags.searchExample(2)).assertTextEquals("Algoritmos")
		assertNodeHidden(SubjectsUiTags.searchExample(3))
	}

	@Test
	fun when_exampleTapped_then_invokesCallbackWithItsText() = runTuIndiceUiTest {
		val selectedExamples = mutableListOf<String>()

		setTuIndiceTestContent {
			SubjectSearchGuidanceView(
				query = "",
				onExampleClick = { example -> selectedExamples += example }
			)
		}

		onNodeWithTag(SubjectsUiTags.searchExample(1)).performClick()
		onNodeWithTag(SubjectsUiTags.searchExample(2)).performClick()
		onNodeWithTag(SubjectsUiTags.searchExample(0)).performClick()

		assertEquals(listOf("Física", "Algoritmos", "MA1111"), selectedExamples)
	}
}
