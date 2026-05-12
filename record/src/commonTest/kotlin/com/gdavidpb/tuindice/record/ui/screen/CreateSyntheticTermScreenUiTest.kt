package com.gdavidpb.tuindice.record.ui.screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubjectAvailability
import com.gdavidpb.tuindice.record.presentation.contract.CreateSyntheticTerm
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class CreateSyntheticTermScreenUiTest {
	@Test
	fun when_searchHasTakenSubjects_then_theyAreHiddenBehindToggle() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CreateSyntheticTermScreen(
				state = CreateSyntheticTerm.State(
					query = "ma",
					searchResults = listOf(
						SyntheticTermSubject(
							subjectCode = "MA1111",
							name = "Matemáticas I",
							credits = 4,
							availability = SyntheticTermSubjectAvailability.AVAILABLE
						),
						SyntheticTermSubject(
							subjectCode = "MA1112",
							name = "Matemáticas II",
							credits = 4,
							availability = SyntheticTermSubjectAvailability.ALREADY_TAKEN
						)
					)
				),
				onQueryChange = {},
				onClearQueryClick = {},
				onPeriodSelected = {},
				onSubjectAdd = {},
				onSubjectRemove = {},
				onCreateClick = {}
			)
		}

		onNodeWithText("Matemáticas I").assertIsDisplayed()
		onAllNodesWithText("Matemáticas II").assertCountEquals(0)
		onNodeWithText("Mostrar 1 ya cursadas").assertIsDisplayed()

		onNodeWithTag(RecordUiTags.CreateSyntheticTermTakenSubjectsToggle).performClick()

		onNodeWithText("Matemáticas II").assertIsDisplayed()
		onNodeWithText("Ocultar 1 ya cursadas").assertIsDisplayed()
	}

	@Test
	fun when_selectedSubjectIsStillInSearchResults_then_screenKeepsUniqueLazyKeys() = runTuIndiceUiTest {
		val selectedSubject = SyntheticTermSubject(
			subjectCode = "MA1111",
			name = "Matemáticas I",
			credits = 4
		)

		setTuIndiceTestContent {
			CreateSyntheticTermScreen(
				state = CreateSyntheticTerm.State(
					query = "ma",
					searchResults = listOf(
						selectedSubject.copy(
							availability = SyntheticTermSubjectAvailability.SELECTED
						)
					),
					selectedSubjects = listOf(selectedSubject)
				),
				onQueryChange = {},
				onClearQueryClick = {},
				onPeriodSelected = {},
				onSubjectAdd = {},
				onSubjectRemove = {},
				onCreateClick = {}
			)
		}

		onAllNodesWithText("Matemáticas I").assertCountEquals(2)
	}
}
