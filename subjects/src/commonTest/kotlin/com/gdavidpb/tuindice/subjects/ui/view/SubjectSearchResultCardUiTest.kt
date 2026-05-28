package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionContains
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.subjects.presentation.model.SubjectSearchResultItem
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SubjectSearchResultCardUiTest {
	@Test
	fun when_rendered_then_usesStatsIconActionInsteadOfNavigationChevron() = runTuIndiceUiTest {
		var clickedSubjectCode: String? = null

		setTuIndiceTestContent {
			SubjectSearchResultCard(
				item = SubjectSearchResultItem(
					subjectCode = "CI2511",
					name = "Lógica Simbólica",
					creditsText = "4 UC"
				),
				onClick = { clickedSubjectCode = "CI2511" }
			)
		}

		onNodeWithTag(SubjectsUiTags.searchResultStatsButton("CI2511"))
			.assertIsDisplayed()
			.assertContentDescriptionContains("Abrir estadísticas de CI2511")

		onNodeWithTag(SubjectsUiTags.searchResultStatsButton("CI2511")).performClick()

		assertEquals("CI2511", clickedSubjectCode)
	}
}
