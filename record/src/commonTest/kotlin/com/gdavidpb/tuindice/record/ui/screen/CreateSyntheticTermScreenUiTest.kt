package com.gdavidpb.tuindice.record.ui.screen

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
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
import com.gdavidpb.tuindice.testkit.ui.performTextInputPerCharacter
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
				onQueryChange = { query ->
					screenState.value = screenState.value.copy(query = query)
				},
				onClearQueryClick = {
					screenState.value = screenState.value.copy(query = "")
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
		onNodeWithText("Código, nombre o palabra clave").assertIsDisplayed()
		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchGuidance).assertIsDisplayed()
		onNodeWithText("Búsquedas sugeridas").assertIsDisplayed()
		onNodeWithText("MA1111").assertIsDisplayed()
		onNodeWithText("Física").assertIsDisplayed()
		onNodeWithText("Algoritmos").assertIsDisplayed()
	}

	@Test
	fun when_searchQueryIsTooShort_then_minQueryGuidanceIsShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CreateSyntheticTermScreen(
				state = CreateSyntheticTerm.State(
					query = "m",
					selectedAddSubjectTab = CreateTermAddSubjectTab.Search
				),
				onQueryChange = {},
				onClearQueryClick = {},
				onPeriodSelected = {},
				onAddSubjectTabSelected = {},
				onSubjectAdd = {},
				onSubjectRemove = {},
				onCreateClick = {}
			)
		}

		onNodeWithText("Agrega un carácter más").assertIsDisplayed()
		onNodeWithText("La búsqueda empieza con al menos 2 caracteres.").assertIsDisplayed()
		onNodeWithText("Búsquedas sugeridas").assertIsDisplayed()
	}

	@Test
	fun when_searchExampleIsClicked_then_queryIsUpdated() = runTuIndiceUiTest {
		var latestQuery = ""

		setTuIndiceTestContent {
			val screenState = remember {
				mutableStateOf(
					CreateSyntheticTerm.State(
						selectedAddSubjectTab = CreateTermAddSubjectTab.Search
					)
				)
			}

			CreateSyntheticTermScreen(
				state = screenState.value,
				onQueryChange = { query ->
					latestQuery = query
					screenState.value = screenState.value.copy(query = query)
				},
				onClearQueryClick = {},
				onPeriodSelected = {},
				onAddSubjectTabSelected = {},
				onSubjectAdd = {},
				onSubjectRemove = {},
				onCreateClick = {}
			)
		}

		onNodeWithTag(RecordUiTags.createSyntheticTermSearchExample(0)).performClick()

		assertEquals("MA1111", latestQuery)
	}

	// The view model answers each keystroke later: the answer to a key lands just before the next one.
	@Test
	fun when_theQueryEchoLagsBehindTheTyping_then_keepsEveryTypedCharacter() = runTuIndiceUiTest {
		val reportedQueries = mutableListOf<String>()
		val screenState = mutableStateOf(
			CreateSyntheticTerm.State(selectedAddSubjectTab = CreateTermAddSubjectTab.Search)
		)

		setTuIndiceTestContent {
			CreateSyntheticTermScreen(
				state = screenState.value,
				onQueryChange = { query -> reportedQueries += query },
				onClearQueryClick = {},
				onPeriodSelected = {},
				onAddSubjectTabSelected = {},
				onSubjectAdd = {},
				onSubjectRemove = {},
				onCreateClick = {}
			)
		}

		performTextInputPerCharacter(RecordUiTags.CreateSyntheticTermSearchField, "slot") { index ->
			if (index >= 1) {
				runOnIdle {
					screenState.value = screenState.value.copy(query = reportedQueries[index - 1])
				}
				waitForIdle()
			}
		}

		assertEquals("slot", reportedQueries.last())
		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchField).assert(hasText("slot"))
	}

	// The field holds the typed text, so an example is a text the caller never pushed: the field
	// itself must take it, even when the same text was typed earlier.
	@Test
	fun when_anExampleIsTappedAfterTheSameTextWasTyped_then_theFieldShowsTheExample() = runTuIndiceUiTest {
		val screenState = mutableStateOf(
			CreateSyntheticTerm.State(selectedAddSubjectTab = CreateTermAddSubjectTab.Search)
		)

		setTuIndiceTestContent {
			CreateSyntheticTermScreen(
				state = screenState.value,
				onQueryChange = { query ->
					screenState.value = screenState.value.copy(query = query)
				},
				onClearQueryClick = {},
				onPeriodSelected = {},
				onAddSubjectTabSelected = {},
				onSubjectAdd = {},
				onSubjectRemove = {},
				onCreateClick = {}
			)
		}

		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchField).performTextInput("MA1111")
		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchField).performTextReplacement("M")
		waitForIdle()
		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchField).assert(hasText("M"))

		onNodeWithTag(RecordUiTags.createSyntheticTermSearchExample(0)).performClick()

		assertEquals("MA1111", screenState.value.query)
		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchField).assert(hasText("MA1111"))
	}

	@Test
	fun when_theQueryChangesToATextTheUserNeverTyped_then_theFieldShowsIt() = runTuIndiceUiTest {
		val screenState = mutableStateOf(
			CreateSyntheticTerm.State(
				query = "fisica",
				selectedAddSubjectTab = CreateTermAddSubjectTab.Search
			)
		)

		setTuIndiceTestContent {
			CreateSyntheticTermScreen(
				state = screenState.value,
				onQueryChange = {},
				onClearQueryClick = {},
				onPeriodSelected = {},
				onAddSubjectTabSelected = {},
				onSubjectAdd = {},
				onSubjectRemove = {},
				onCreateClick = {}
			)
		}

		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchField).assert(hasText("fisica"))

		runOnIdle { screenState.value = screenState.value.copy(query = "quimica") }
		waitForIdle()

		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchField).assert(hasText("quimica"))
	}

	@Test
	fun when_searchHasApprovedSubjects_then_theyAreHiddenBehindToggle() = runTuIndiceUiTest {
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
							availability = SyntheticTermSubjectAvailability.APPROVED
						)
					).toItems()
				),
				onQueryChange = {},
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
	fun when_searchHasOnlyApprovedSubjects_then_toggleIsShownInsteadOfEmptyState() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CreateSyntheticTermScreen(
				state = CreateSyntheticTerm.State(
					query = "ma1111",
					selectedAddSubjectTab = CreateTermAddSubjectTab.Search,
					searchResults = listOf(
						SyntheticTermSubject(
							subjectCode = "MA1111",
							name = "Matemáticas I",
							credits = 4,
							availability = SyntheticTermSubjectAvailability.APPROVED
						)
					).toItems(),
					suggestedSubjects = listOf(
						SyntheticTermSubject(
							subjectCode = "MA1121",
							name = "Matemáticas II",
							credits = 4,
							availability = SyntheticTermSubjectAvailability.AVAILABLE
						)
					).toItems()
				),
				onQueryChange = {},
				onClearQueryClick = {},
				onPeriodSelected = {},
				onAddSubjectTabSelected = {},
				onSubjectAdd = {},
				onSubjectRemove = {},
				onCreateClick = {}
			)
		}

		onNodeWithText("0 resultados").assertIsDisplayed()
		onAllNodesWithText("Sugeridas por tu pensum").assertCountEquals(0)
		onAllNodesWithText("MATEMÁTICAS I").assertCountEquals(0)
		onNodeWithText("Mostrar 1 ya cursadas").assertIsDisplayed()

		onNodeWithTag(RecordUiTags.CreateSyntheticTermTakenSubjectsToggle).performClick()

		onNodeWithText("MATEMÁTICAS I").assertIsDisplayed()
		assertVisibleStatus("MA1111", SyntheticTermSubjectAvailability.APPROVED)
		onNodeWithText("Aprobada").assertIsDisplayed()
	}

	@Test
	fun when_searchResultsHaveApprovedAndOthers_thenApprovedSubjectsAppearAtTheEndAndToggleIsBetweenGroups() = runTuIndiceUiTest {
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
							availability = SyntheticTermSubjectAvailability.APPROVED
						),
						SyntheticTermSubject(
							subjectCode = "BB0001",
							name = "Materia bloqueada",
							credits = 4,
							availability = SyntheticTermSubjectAvailability.BLOCKED
						)
					).toItems()
				),
				onQueryChange = {},
				onClearQueryClick = {},
				onPeriodSelected = {},
				onAddSubjectTabSelected = {},
				onSubjectAdd = {},
				onSubjectRemove = {},
				onCreateClick = {}
			)
		}

		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchTab).performClick()

		// Hidden state: only non-approved subjects should be visible.
		assertVisibleSearchResult(index = 0, subjectCode = "AA0001")
		assertVisibleSearchResult(index = 1, subjectCode = "BB0001")
		onAllNodesWithText("Materia cursada").assertCountEquals(0)
		onNodeWithTag(RecordUiTags.CreateSyntheticTermTakenSubjectsToggle).performClick()

		// After expand, approved subjects should stay at the end.
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
						selectedSubject
					).toItems(),
					selectedSubjects = listOf(selectedSubject).toItems()
				),
				onQueryChange = {},
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
							subjectCode = "CC1001",
							name = "Estado cursada",
							credits = 4,
							availability = SyntheticTermSubjectAvailability.APPROVED
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
							availability = SyntheticTermSubjectAvailability.BLOCKED
						),
						SyntheticTermSubject(
							subjectCode = "FF1001",
							name = "Estado fuera del pensum",
							credits = 4,
							availability = SyntheticTermSubjectAvailability.NOT_IN_PENSUM
						)
					).toItems()
				),
				onQueryChange = {},
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
		onAllNodesWithTag(statusTag("CC1001", SyntheticTermSubjectAvailability.APPROVED)).assertCountEquals(0)
		onNodeWithTag(
			RecordUiTags.createSyntheticTermSubjectAction(
				subjectCode = "AA1001",
				action = CreateTermSubjectCardAction.Add.name.lowercase()
			)
		).assertIsDisplayed()
		assertVisibleStatus("DD1001", SyntheticTermSubjectAvailability.ALREADY_PLANNED)
		assertVisibleStatsButton("DD1001")
		onAllNodesWithTag(
			RecordUiTags.createSyntheticTermSubjectAction(
				subjectCode = "DD1001",
				action = CreateTermSubjectCardAction.Add.name.lowercase()
			)
		).assertCountEquals(0)
		assertVisibleStatus("EE1001", SyntheticTermSubjectAvailability.BLOCKED)
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

		assertVisibleStatus("CC1001", SyntheticTermSubjectAvailability.APPROVED)
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
							availability = SyntheticTermSubjectAvailability.BLOCKED
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
				onQueryChange = {},
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
							availability = SyntheticTermSubjectAvailability.BLOCKED
						)
					).toItems()
				),
				onQueryChange = {},
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
				onQueryChange = { query ->
					screenState.value = screenState.value.copy(query = query)
				},
				onClearQueryClick = {
					screenState.value = screenState.value.copy(query = "")
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
	fun when_searchQueryHasNoMatches_then_noResultsMessageIsShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CreateSyntheticTermScreen(
				state = CreateSyntheticTerm.State(
					query = "zz",
					selectedAddSubjectTab = CreateTermAddSubjectTab.Search,
					searchResults = emptyList()
				),
				onQueryChange = {},
				onClearQueryClick = {},
				onPeriodSelected = {},
				onAddSubjectTabSelected = {},
				onSubjectAdd = {},
				onSubjectRemove = {},
				onCreateClick = {}
			)
		}

		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchTab).performClick()

		onNodeWithText("No encontramos materias").assertIsDisplayed()
		onNodeWithText("No hay resultados para \"zz\". Prueba con otro código o nombre.").assertIsDisplayed()
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
				onQueryChange = {},
				onClearQueryClick = {},
				onPeriodSelected = {},
				onAddSubjectTabSelected = {},
				onSubjectAdd = {},
				onSubjectRemove = {},
				onCreateClick = {}
			)
		}

		onNodeWithTag(RecordUiTags.CreateSyntheticTermSearchTab).performClick()

		onNodeWithText("No encontramos materias").assertIsDisplayed()
		onNodeWithText("No hay resultados para \"zz\". Prueba con otro código o nombre.").assertIsDisplayed()
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
				onQueryChange = {},
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
