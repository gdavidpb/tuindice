package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.subjects.presentation.model.SubjectSearchResultItem
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SubjectSearchResultsUiTest {
	@Test
	fun when_resultsAreEmptyAndNotRefreshing_then_displaysNoResultsMessage() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectSearchResults(
				query = "zz",
				results = emptyList(),
				isRefreshing = false,
				onSubjectClick = {},
				onResultsInteraction = {}
			)
		}

		onNodeWithText("No encontramos materias").assertIsDisplayed()
		onNodeWithText("No hay resultados para \"zz\". Prueba con otro código o nombre.")
			.assertIsDisplayed()
	}

	@Test
	fun when_resultsAreEmptyWhileRefreshing_then_displaysCountInsteadOfEmptyMessage() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectSearchResults(
				query = "zz",
				results = emptyList(),
				isRefreshing = true,
				onSubjectClick = {},
				onResultsInteraction = {}
			)
		}

		onNodeWithText("0 resultados para \"zz\"").assertIsDisplayed()
		onAllNodesWithText("No encontramos materias").assertCountEquals(0)
	}

	@Test
	fun when_singleResult_then_displaysSingularCountAndItsCard() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectSearchResults(
				query = "ci",
				results = listOf(resultItem(subjectCode = "CI2511")),
				isRefreshing = false,
				onSubjectClick = {},
				onResultsInteraction = {}
			)
		}

		onNodeWithText("1 resultado para \"ci\"").assertIsDisplayed()
		assertNodeVisible(SubjectsUiTags.SearchResults)
		assertNodeVisible(SubjectsUiTags.searchResult("CI2511"))
	}

	@Test
	fun when_multipleResults_then_displaysPluralCountAndEveryCard() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectSearchResults(
				query = "ci",
				results = listOf(
					resultItem(subjectCode = "CI2511"),
					resultItem(subjectCode = "CI2611")
				),
				isRefreshing = false,
				onSubjectClick = {},
				onResultsInteraction = {}
			)
		}

		onNodeWithText("2 resultados para \"ci\"").assertIsDisplayed()
		assertNodeVisible(SubjectsUiTags.searchResult("CI2511"))
		assertNodeVisible(SubjectsUiTags.searchResult("CI2611"))
		assertNodeHidden(SubjectsUiTags.searchResult("CI3641"))
	}

	@Test
	fun when_resultTapped_then_reportsInteractionAndItsSubjectCode() = runTuIndiceUiTest {
		val clickedSubjectCodes = mutableListOf<String>()
		var interactions = 0

		setTuIndiceTestContent {
			SubjectSearchResults(
				query = "ci",
				results = listOf(
					resultItem(subjectCode = "CI2511"),
					resultItem(subjectCode = "CI2611")
				),
				isRefreshing = false,
				onSubjectClick = { subjectCode -> clickedSubjectCodes += subjectCode },
				onResultsInteraction = { interactions++ }
			)
		}

		onNodeWithTag(SubjectsUiTags.searchResult("CI2611")).performClick()

		assertEquals(listOf("CI2611"), clickedSubjectCodes)
		assertTrue(interactions >= 1)
	}

	private fun resultItem(subjectCode: String): SubjectSearchResultItem {
		return SubjectSearchResultItem(
			subjectCode = subjectCode,
			name = "Materia $subjectCode",
			creditsText = "4 UC"
		)
	}
}
