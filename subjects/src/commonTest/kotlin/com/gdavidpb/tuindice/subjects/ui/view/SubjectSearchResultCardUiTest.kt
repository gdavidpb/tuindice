package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionContains
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumNodeStatus
import com.gdavidpb.tuindice.subjects.presentation.model.SubjectSearchResultItem
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
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
					creditsText = "4 UC",
					pensumStatus = AcademicPensumNodeStatus.APPROVED
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

	@Test
	fun when_pensumStatusExists_then_displaysPensumLanguageStatus() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectSearchResultCard(
				item = SubjectSearchResultItem(
					subjectCode = "CI2511",
					name = "Lógica Simbólica",
					creditsText = "4 UC",
					pensumStatus = AcademicPensumNodeStatus.AVAILABLE
				),
				onClick = {}
			)
		}

		onNodeWithTag(SubjectsUiTags.searchResultStatus("CI2511", "available"))
			.assertIsDisplayed()
		onNodeWithText("Disponible").assertIsDisplayed()
	}

	@Test
	fun when_pensumStatusIsMissing_then_doesNotDisplayStatus() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectSearchResultCard(
				item = SubjectSearchResultItem(
					subjectCode = "CI2511",
					name = "Lógica Simbólica",
					creditsText = "4 UC"
				),
				onClick = {}
			)
		}

		assertNodeHidden(SubjectsUiTags.searchResultStatus("CI2511", "approved"))
		onAllNodesWithText("Aprobada").assertCountEquals(0)
	}
}
