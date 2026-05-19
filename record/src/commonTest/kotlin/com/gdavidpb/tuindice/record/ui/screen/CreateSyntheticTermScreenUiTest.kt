package com.gdavidpb.tuindice.record.ui.screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermPeriodOption
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
	fun when_opened_then_suggestedTabIsShownAndSearchFieldIsHidden() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CreateSyntheticTermScreen(
				state = CreateSyntheticTerm.State(
					suggestedSubjects = listOf(
						SyntheticTermSubject(
							subjectCode = "MA1111",
							name = "Matemáticas I",
							credits = 4
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

		onNodeWithText("Agregar materias").assertIsDisplayed()
		onNodeWithText("Sugeridas por tu pensum").assertIsDisplayed()
		onNodeWithText("Matemáticas I").assertIsDisplayed()
		onAllNodesWithTag(RecordUiTags.CreateSyntheticTermSearchField).assertCountEquals(0)

		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchTab).performClick()

		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchField).assertIsDisplayed()
	}

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

		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchTab).performClick()

		onNodeWithText("Matemáticas I").assertIsDisplayed()
		onAllNodesWithText("Matemáticas II").assertCountEquals(0)
		onNodeWithText("Mostrar 1 ya cursadas").assertIsDisplayed()

		onNodeWithTag(RecordUiTags.CreateSyntheticTermTakenSubjectsToggle).performClick()

		onNodeWithText("Matemáticas II").assertIsDisplayed()
		onNodeWithText("Ocultar 1 ya cursadas").assertIsDisplayed()
	}

	@Test
	fun when_selectedSubjectIsStillInSearchResults_then_itIsOnlyShownInSelectedSubjects() = runTuIndiceUiTest {
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

		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchTab).performClick()

		onAllNodesWithText("Matemáticas I").assertCountEquals(1)
	}

	@Test
	fun when_switchingAwayFromSearch_then_queryResultsAreKeptForSearchTab() = runTuIndiceUiTest {
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

		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchTab).performClick()
		onNodeWithText("Matemáticas I").assertIsDisplayed()

		onNodeWithTag(RecordUiTags.CreateSyntheticTermSuggestedTab).performClick()
		onAllNodesWithText("Matemáticas I").assertCountEquals(0)

		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchTab).performClick()
		onNodeWithText("Matemáticas I").assertIsDisplayed()
	}

	@Test
	fun when_searchQueryHasNoMatches_then_zeroResultsIsShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CreateSyntheticTermScreen(
				state = CreateSyntheticTerm.State(
					query = "zz",
					searchResults = emptyList()
				),
				onQueryChange = {},
				onClearQueryClick = {},
				onPeriodSelected = {},
				onSubjectAdd = {},
				onSubjectRemove = {},
				onCreateClick = {}
			)
		}

		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchTab).performClick()

		onNodeWithText("0 resultados").assertIsDisplayed()
	}

	@Test
	fun when_stateIsSubmitting_then_submitButtonShowsProgressAndIsDisabled() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CreateSyntheticTermScreen(
				state = CreateSyntheticTerm.State(
					selectedPeriod = SyntheticTermPeriodOption(
						periodYear = 2027,
						periodCode = AcademicTermPeriod.JUL_AUG
					),
					selectedSubjects = listOf(
						SyntheticTermSubject(
							subjectCode = "MA1111",
							name = "Matemáticas I",
							credits = 4
						)
					),
					isSubmitting = true
				),
				onQueryChange = {},
				onClearQueryClick = {},
				onPeriodSelected = {},
				onSubjectAdd = {},
				onSubjectRemove = {},
				onCreateClick = {}
			)
		}

		onNodeWithTag(RecordUiTags.CreateSyntheticTermSubmitProgress).assertIsDisplayed()
		onNodeWithTag(RecordUiTags.CreateSyntheticTermSubmitButton).assertIsNotEnabled()
	}
}
