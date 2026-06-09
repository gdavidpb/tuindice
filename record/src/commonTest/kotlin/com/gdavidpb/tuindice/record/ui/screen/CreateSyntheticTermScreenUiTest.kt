package com.gdavidpb.tuindice.record.ui.screen

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermPeriodOption
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubjectAvailability
import com.gdavidpb.tuindice.record.presentation.contract.CreateSyntheticTerm
import com.gdavidpb.tuindice.record.presentation.mapper.toCreateTermSubjectItem
import com.gdavidpb.tuindice.record.presentation.model.CreateTermAddSubjectTab
import com.gdavidpb.tuindice.record.presentation.model.CreateTermSubjectItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.model.CreateTermSubjectCardAction
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class CreateSyntheticTermScreenUiTest {
	@Test
	fun when_opened_then_suggestedTabIsShownAndSearchFieldIsHidden() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			val screenState = remember {
				mutableStateOf(
					CreateSyntheticTerm.State(
						suggestedSubjects = listOf(
							SyntheticTermSubject(
								subjectCode = "MA1111",
								name = "Matemáticas I",
								credits = 4
							)
						).toItems()
					)
				)
			}

			CreateSyntheticTermScreen(
				state = screenState.value,
				onQueryChange = { query, selectionStart, selectionEnd ->
					screenState.value = screenState.value.copy(
						query = query,
						querySelectionStart = selectionStart,
						querySelectionEnd = selectionEnd
					)
				},
				onClearQueryClick = {
					screenState.value = screenState.value.copy(
						query = "",
						querySelectionStart = 0,
						querySelectionEnd = 0
					)
				},
				onPeriodSelected = {},
				onAddSubjectTabSelected = { tab ->
					screenState.value = screenState.value.copy(selectedAddSubjectTab = tab)
				},
				onSubjectAdd = {},
				onSubjectRemove = {},
				onCreateClick = {}
			)
		}

		onNodeWithText("Agregar materias").assertIsDisplayed()
		onNodeWithText("Sugeridas por tu pensum").assertIsDisplayed()
		onNodeWithText("MATEMÁTICAS I").assertIsDisplayed()
		onAllNodesWithText("Matemáticas I").assertCountEquals(0)
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
					selectedAddSubjectTab = CreateTermAddSubjectTab.Search,
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
					).toItems()
				),
				onQueryChange = { _, _, _ -> },
				onClearQueryClick = {},
				onPeriodSelected = {},
				onAddSubjectTabSelected = {},
				onSubjectAdd = {},
				onSubjectRemove = {},
				onCreateClick = {}
			)
		}

		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchTab).performClick()

		onNodeWithText("MATEMÁTICAS I").assertIsDisplayed()
		onAllNodesWithText("Matemáticas I").assertCountEquals(0)
		onAllNodesWithText("MATEMÁTICAS II").assertCountEquals(0)
		onNodeWithText("Mostrar 1 ya cursadas").assertIsDisplayed()

		onNodeWithTag(RecordUiTags.CreateSyntheticTermTakenSubjectsToggle).performClick()

		onNodeWithText("MATEMÁTICAS II").assertIsDisplayed()
		onAllNodesWithText("Matemáticas II").assertCountEquals(0)
		onNodeWithText("Ocultar 1 ya cursadas").assertIsDisplayed()
	}

	@Test
	fun when_searchResultsHaveTakenAndOthers_thenTakenSubjectsAppearAtTheEndAndToggleIsBetweenGroups() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CreateSyntheticTermScreen(
				state = CreateSyntheticTerm.State(
					query = "ma",
					selectedAddSubjectTab = CreateTermAddSubjectTab.Search,
					searchResults = listOf(
						SyntheticTermSubject(
							subjectCode = "AA0001",
							name = "Materia libre",
							credits = 4,
							availability = SyntheticTermSubjectAvailability.AVAILABLE
						),
						SyntheticTermSubject(
							subjectCode = "CC0001",
							name = "Materia cursada",
							credits = 4,
							availability = SyntheticTermSubjectAvailability.ALREADY_TAKEN
						),
						SyntheticTermSubject(
							subjectCode = "BB0001",
							name = "Materia bloqueada",
							credits = 4,
							availability = SyntheticTermSubjectAvailability.UNAVAILABLE
						)
					).toItems()
				),
				onQueryChange = { _, _, _ -> },
				onClearQueryClick = {},
				onPeriodSelected = {},
				onAddSubjectTabSelected = {},
				onSubjectAdd = {},
				onSubjectRemove = {},
				onCreateClick = {}
			)
		}

		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchTab).performClick()

		// Hidden state: only non-taken subjects should be visible.
		assertVisibleSearchResult(index = 0, subjectCode = "AA0001")
		assertVisibleSearchResult(index = 1, subjectCode = "BB0001")
		onAllNodesWithText("Materia cursada").assertCountEquals(0)
		onNodeWithTag(RecordUiTags.CreateSyntheticTermTakenSubjectsToggle).performClick()

		// After expand, taken subjects should stay at the end.
		assertVisibleSearchResult(index = 0, subjectCode = "AA0001")
		assertVisibleSearchResult(index = 1, subjectCode = "BB0001")
		assertVisibleSearchResult(index = 2, subjectCode = "CC0001")
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
					selectedAddSubjectTab = CreateTermAddSubjectTab.Search,
					searchResults = listOf(
						selectedSubject.copy(
							availability = SyntheticTermSubjectAvailability.SELECTED
						)
					).toItems(),
					selectedSubjects = listOf(selectedSubject).toItems()
				),
				onQueryChange = { _, _, _ -> },
				onClearQueryClick = {},
				onPeriodSelected = {},
				onAddSubjectTabSelected = {},
				onSubjectAdd = {},
				onSubjectRemove = {},
				onCreateClick = {}
			)
		}

		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchTab).performClick()

		onAllNodesWithText("MATEMÁTICAS I").assertCountEquals(1)
		onAllNodesWithText("Matemáticas I").assertCountEquals(0)
	}

	@Test
	fun when_searchHasEveryAvailabilityState_then_statusAndActionsMatchEachState() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CreateSyntheticTermScreen(
				state = CreateSyntheticTerm.State(
					query = "estado",
					selectedAddSubjectTab = CreateTermAddSubjectTab.Search,
					searchResults = listOf(
						SyntheticTermSubject(
							subjectCode = "AA1001",
							name = "Estado disponible",
							credits = 4,
							availability = SyntheticTermSubjectAvailability.AVAILABLE
						),
						SyntheticTermSubject(
							subjectCode = "BB1001",
							name = "Estado seleccionada",
							credits = 4,
							availability = SyntheticTermSubjectAvailability.SELECTED
						),
						SyntheticTermSubject(
							subjectCode = "CC1001",
							name = "Estado cursada",
							credits = 4,
							availability = SyntheticTermSubjectAvailability.ALREADY_TAKEN
						),
						SyntheticTermSubject(
							subjectCode = "DD1001",
							name = "Estado planificada",
							credits = 4,
							availability = SyntheticTermSubjectAvailability.ALREADY_PLANNED
						),
						SyntheticTermSubject(
							subjectCode = "EE1001",
							name = "Estado no disponible",
							credits = 4,
							availability = SyntheticTermSubjectAvailability.UNAVAILABLE
						),
						SyntheticTermSubject(
							subjectCode = "FF1001",
							name = "Estado fuera del pensum",
							credits = 4,
							availability = SyntheticTermSubjectAvailability.NOT_IN_PENSUM
						)
					).toItems()
				),
				onQueryChange = { _, _, _ -> },
				onClearQueryClick = {},
				onPeriodSelected = {},
				onAddSubjectTabSelected = {},
				onSubjectAdd = {},
				onSubjectRemove = {},
				onCreateClick = {}
			)
		}

		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchTab).performClick()

		assertVisibleStatus("AA1001", SyntheticTermSubjectAvailability.AVAILABLE)
		assertVisibleStatsButton("AA1001")
		onAllNodesWithTag(statusTag("CC1001", SyntheticTermSubjectAvailability.ALREADY_TAKEN)).assertCountEquals(0)
		onNodeWithTag(
			RecordUiTags.createSyntheticTermSubjectAction(
				subjectCode = "AA1001",
				action = CreateTermSubjectCardAction.Add.name.lowercase()
			)
		).assertIsDisplayed()
		assertVisibleStatus("BB1001", SyntheticTermSubjectAvailability.SELECTED)
		assertVisibleStatsButton("BB1001")
		onAllNodesWithTag(
			RecordUiTags.createSyntheticTermSubjectAction(
				subjectCode = "BB1001",
				action = CreateTermSubjectCardAction.Add.name.lowercase()
			)
		).assertCountEquals(0)
		assertVisibleStatus("DD1001", SyntheticTermSubjectAvailability.ALREADY_PLANNED)
		assertVisibleStatsButton("DD1001")
		onAllNodesWithTag(
			RecordUiTags.createSyntheticTermSubjectAction(
				subjectCode = "DD1001",
				action = CreateTermSubjectCardAction.Add.name.lowercase()
			)
		).assertCountEquals(0)
		assertVisibleStatus("EE1001", SyntheticTermSubjectAvailability.UNAVAILABLE)
		assertVisibleStatsButton("EE1001")
		onNodeWithTag(
			RecordUiTags.createSyntheticTermSubjectAction(
				subjectCode = "EE1001",
				action = CreateTermSubjectCardAction.Add.name.lowercase()
			)
		).assertIsDisplayed()
		assertVisibleStatus("FF1001", SyntheticTermSubjectAvailability.NOT_IN_PENSUM)
		assertVisibleStatsButton("FF1001")
		onNodeWithTag(
			RecordUiTags.createSyntheticTermSubjectAction(
				subjectCode = "FF1001",
				action = CreateTermSubjectCardAction.Add.name.lowercase()
			)
		).assertIsDisplayed()

		onNodeWithTag(RecordUiTags.CreateSyntheticTermContentList)
			.performScrollToNode(hasTestTag(RecordUiTags.CreateSyntheticTermTakenSubjectsToggle))
		onNodeWithTag(RecordUiTags.CreateSyntheticTermTakenSubjectsToggle).performClick()

		assertVisibleStatus("CC1001", SyntheticTermSubjectAvailability.ALREADY_TAKEN)
		assertVisibleStatsButton("CC1001")
		onAllNodesWithTag(
			RecordUiTags.createSyntheticTermSubjectAction(
				subjectCode = "CC1001",
				action = CreateTermSubjectCardAction.Add.name.lowercase()
			)
		).assertCountEquals(0)
	}

	@Test
	fun when_searchResultsAreOrdered_then_indexTagsExposeDisplayedOrder() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CreateSyntheticTermScreen(
				state = CreateSyntheticTerm.State(
					query = "prioridad",
					selectedAddSubjectTab = CreateTermAddSubjectTab.Search,
					searchResults = listOf(
						SyntheticTermSubject(
							subjectCode = "EP1308",
							name = "Prioridad planificada del pensum",
							credits = 4,
							availability = SyntheticTermSubjectAvailability.ALREADY_PLANNED
						),
						SyntheticTermSubject(
							subjectCode = "EP2308",
							name = "Prioridad bloqueada del pensum",
							credits = 4,
							availability = SyntheticTermSubjectAvailability.UNAVAILABLE
						),
						SyntheticTermSubject(
							subjectCode = "AA1001",
							name = "Prioridad fuera del pensum alfa",
							credits = 4,
							availability = SyntheticTermSubjectAvailability.NOT_IN_PENSUM
						),
						SyntheticTermSubject(
							subjectCode = "AB1001",
							name = "Prioridad fuera del pensum beta",
							credits = 4,
							availability = SyntheticTermSubjectAvailability.NOT_IN_PENSUM
						)
					).toItems()
				),
				onQueryChange = { _, _, _ -> },
				onClearQueryClick = {},
				onPeriodSelected = {},
				onAddSubjectTabSelected = {},
				onSubjectAdd = {},
				onSubjectRemove = {},
				onCreateClick = {}
			)
		}

		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchTab).performClick()

		assertVisibleSearchResult(index = 0, subjectCode = "EP1308")
		assertVisibleSearchResult(index = 1, subjectCode = "EP2308")
		assertVisibleSearchResult(index = 2, subjectCode = "AA1001")
		assertVisibleSearchResult(index = 3, subjectCode = "AB1001")
		onAllNodesWithTag(
			RecordUiTags.createSyntheticTermSearchResult(index = 0, subjectCode = "AA1001")
		).assertCountEquals(0)
	}

	@Test
	fun when_searchResultStatsButtonIsClicked_then_subjectCodeIsEmitted() = runTuIndiceUiTest {
		var clickedSubjectCode: String? = null

		setTuIndiceTestContent {
			CreateSyntheticTermScreen(
				state = CreateSyntheticTerm.State(
					query = "aa",
					selectedAddSubjectTab = CreateTermAddSubjectTab.Search,
					searchResults = listOf(
						SyntheticTermSubject(
							subjectCode = "AA1001",
							name = "Estadística buscada",
							credits = 4,
							availability = SyntheticTermSubjectAvailability.UNAVAILABLE
						)
					).toItems()
				),
				onQueryChange = { _, _, _ -> },
				onClearQueryClick = {},
				onPeriodSelected = {},
				onAddSubjectTabSelected = {},
				onSubjectAdd = {},
				onSubjectRemove = {},
				onSubjectStatsClick = { subjectCode -> clickedSubjectCode = subjectCode },
				onCreateClick = {}
			)
		}

		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchTab).performClick()
		onNodeWithTag(RecordUiTags.createSyntheticTermSubjectStatsButton("AA1001")).performClick()

		assertEquals("AA1001", clickedSubjectCode)
	}

	@Test
	fun when_switchingAwayFromSearch_then_queryResultsAreKeptForSearchTab() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			val screenState = remember {
				mutableStateOf(
					CreateSyntheticTerm.State(
						query = "ma",
						searchResults = listOf(
							SyntheticTermSubject(
								subjectCode = "MA1111",
								name = "Matemáticas I",
								credits = 4,
								availability = SyntheticTermSubjectAvailability.AVAILABLE
							)
						).toItems()
					)
				)
			}

			CreateSyntheticTermScreen(
				state = screenState.value,
				onQueryChange = { query, selectionStart, selectionEnd ->
					screenState.value = screenState.value.copy(
						query = query,
						querySelectionStart = selectionStart,
						querySelectionEnd = selectionEnd
					)
				},
				onClearQueryClick = {
					screenState.value = screenState.value.copy(
						query = "",
						querySelectionStart = 0,
						querySelectionEnd = 0
					)
				},
				onPeriodSelected = {},
				onAddSubjectTabSelected = { tab ->
					screenState.value = screenState.value.copy(selectedAddSubjectTab = tab)
				},
				onSubjectAdd = {},
				onSubjectRemove = {},
				onCreateClick = {}
			)
		}

		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchTab).performClick()
		onNodeWithText("MATEMÁTICAS I").assertIsDisplayed()

		onNodeWithTag(RecordUiTags.CreateSyntheticTermSuggestedTab).performClick()
		onAllNodesWithText("MATEMÁTICAS I").assertCountEquals(0)

		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchTab).performClick()
		onNodeWithText("MATEMÁTICAS I").assertIsDisplayed()
	}

	@Test
	fun when_searchQueryHasNoMatches_then_zeroResultsIsShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CreateSyntheticTermScreen(
				state = CreateSyntheticTerm.State(
					query = "zz",
					selectedAddSubjectTab = CreateTermAddSubjectTab.Search,
					searchResults = emptyList()
				),
				onQueryChange = { _, _, _ -> },
				onClearQueryClick = {},
				onPeriodSelected = {},
				onAddSubjectTabSelected = {},
				onSubjectAdd = {},
				onSubjectRemove = {},
				onCreateClick = {}
			)
		}

		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchTab).performClick()

		onNodeWithText("0 resultados").assertIsDisplayed()
	}

	@Test
	fun when_searchHasNoMatches_then_suggestedSubjectsAreShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CreateSyntheticTermScreen(
				state = CreateSyntheticTerm.State(
					query = "zz",
					selectedAddSubjectTab = CreateTermAddSubjectTab.Search,
					searchResults = emptyList(),
					suggestedSubjects = listOf(
						SyntheticTermSubject(
							subjectCode = "MA1111",
							name = "Matemáticas I",
							credits = 4
						)
					).toItems()
				),
				onQueryChange = { _, _, _ -> },
				onClearQueryClick = {},
				onPeriodSelected = {},
				onAddSubjectTabSelected = {},
				onSubjectAdd = {},
				onSubjectRemove = {},
				onCreateClick = {}
			)
		}

		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchTab).performClick()

		onNodeWithText("0 resultados").assertIsDisplayed()
		onNodeWithText("Sugeridas por tu pensum").assertIsDisplayed()
		onNodeWithText("MATEMÁTICAS I").assertIsDisplayed()
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
					).toItems(),
					isSubmitting = true
				),
				onQueryChange = { _, _, _ -> },
				onClearQueryClick = {},
				onPeriodSelected = {},
				onAddSubjectTabSelected = {},
				onSubjectAdd = {},
				onSubjectRemove = {},
				onCreateClick = {}
			)
		}

		onNodeWithTag(RecordUiTags.CreateSyntheticTermSubmitProgress).assertIsDisplayed()
		onNodeWithTag(RecordUiTags.CreateSyntheticTermSubmitButton).assertIsNotEnabled()
	}

	private fun androidx.compose.ui.test.SemanticsNodeInteractionsProvider.assertVisibleStatus(
		subjectCode: String,
		availability: SyntheticTermSubjectAvailability
	) {
		val tag = statusTag(subjectCode, availability)
		onNodeWithTag(RecordUiTags.CreateSyntheticTermContentList)
			.performScrollToNode(hasTestTag(tag))
		onNodeWithTag(tag).assertIsDisplayed()
	}

	private fun androidx.compose.ui.test.SemanticsNodeInteractionsProvider.assertVisibleSearchResult(
		index: Int,
		subjectCode: String
	) {
		val tag = RecordUiTags.createSyntheticTermSearchResult(index = index, subjectCode = subjectCode)
		onNodeWithTag(RecordUiTags.CreateSyntheticTermContentList)
			.performScrollToNode(hasTestTag(tag))
		onNodeWithTag(tag).assertIsDisplayed()
	}

	private fun androidx.compose.ui.test.SemanticsNodeInteractionsProvider.assertVisibleStatsButton(subjectCode: String) {
		val tag = RecordUiTags.createSyntheticTermSubjectStatsButton(subjectCode)
		onNodeWithTag(RecordUiTags.CreateSyntheticTermContentList)
			.performScrollToNode(hasTestTag(tag))
		onNodeWithTag(tag).assertIsDisplayed()
	}

	private fun statusTag(
		subjectCode: String,
		availability: SyntheticTermSubjectAvailability
	): String {
		return RecordUiTags.createSyntheticTermSubjectStatus(
			subjectCode = subjectCode,
			status = availability.name.lowercase()
		)
	}
}

private fun List<SyntheticTermSubject>.toItems(): List<CreateTermSubjectItem> {
	return map { subject -> subject.toCreateTermSubjectItem() }
}
