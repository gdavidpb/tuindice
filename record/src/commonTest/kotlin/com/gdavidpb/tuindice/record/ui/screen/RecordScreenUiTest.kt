package com.gdavidpb.tuindice.record.ui.screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class RecordScreenUiTest {
	@Test
	fun when_attemptNameUsesMixedCase_then_recordDisplaysUppercaseName() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RecordScreen(
				state = contentState(
					termKind = TermKind.SYNTHETIC,
					attempts = listOf(
						AcademicAttempt(
							id = "attempt-1",
							subjectCode = "MA1112",
							subjectName = "Matemáticas II",
							credits = 4
						)
					)
				),
				selectedTermId = SyntheticTermId,
				onSelectedTermChange = {},
				onRetryClick = {},
				onAttemptSelectionChange = { _, _, _, _ -> },
				onCreateSyntheticTermClick = {},
				onUpdateSyntheticTermClick = {},
				onDeleteSyntheticTermClick = {}
			)
		}

		onNodeWithText("MATEMÁTICAS II").assertIsDisplayed()
		onAllNodesWithText("Matemáticas II").assertCountEquals(0)
	}

	@Test
	fun when_selectedTermIsSynthetic_then_overlayActionsAreShownAndClickable() = runTuIndiceUiTest {
		var editedTermId: String? = null
		var deletedTermId: String? = null
		var createClicks = 0

		setTuIndiceTestContent {
			RecordScreen(
				state = contentState(termKind = TermKind.SYNTHETIC),
				selectedTermId = SyntheticTermId,
				onSelectedTermChange = {},
				onRetryClick = {},
				onAttemptSelectionChange = { _, _, _, _ -> },
				onCreateSyntheticTermClick = { createClicks++ },
				onUpdateSyntheticTermClick = { editedTermId = it },
				onDeleteSyntheticTermClick = { deletedTermId = it }
			)
		}

		assertNodeVisible(RecordUiTags.EditSyntheticTermButton)
		assertNodeVisible(RecordUiTags.DeleteSyntheticTermButton)
		assertNodeVisible(RecordUiTags.CreateSyntheticTermFab)

		onNodeWithTag(RecordUiTags.EditSyntheticTermButton).performClick()
		onNodeWithTag(RecordUiTags.DeleteSyntheticTermButton).performClick()
		onNodeWithTag(RecordUiTags.CreateSyntheticTermFab).performClick()

		assertEquals(SyntheticTermId, editedTermId)
		assertEquals(SyntheticTermId, deletedTermId)
		assertEquals(1, createClicks)
	}

	@Test
	fun when_selectedTermIsNotSynthetic_then_overlayActionsAreHidden() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RecordScreen(
				state = contentState(
					termId = "current-term",
					termKind = TermKind.CURRENT
				),
				selectedTermId = "current-term",
				onSelectedTermChange = {},
				onRetryClick = {},
				onAttemptSelectionChange = { _, _, _, _ -> },
				onCreateSyntheticTermClick = {},
				onUpdateSyntheticTermClick = {},
				onDeleteSyntheticTermClick = {}
			)
		}

		assertNodeHidden(RecordUiTags.EditSyntheticTermButton)
		assertNodeHidden(RecordUiTags.DeleteSyntheticTermButton)
		assertNodeVisible(RecordUiTags.CreateSyntheticTermFab)
	}

	@Test
	fun when_selectedTermIsCurrentProjection_then_enrollmentProofFabIsNotRendered() = runTuIndiceUiTest {
		val state = contentState(
			termId = "current-term",
			termKind = TermKind.CURRENT
		)

		setTuIndiceTestContent {
			RecordScreen(
				state = state,
				selectedTermId = "current-term",
				onSelectedTermChange = {},
				onRetryClick = {},
				onAttemptSelectionChange = { _, _, _, _ -> },
				onCreateSyntheticTermClick = {},
				onUpdateSyntheticTermClick = {},
				onDeleteSyntheticTermClick = {}
			)
		}

		assertEquals(TopBarConfig.RecordWithEnrollmentProof, state.topBarConfig)
	}

	@Test
	fun when_selectedTermIsHistoricalProjection_then_enrollmentProofTopBarActionIsNotAvailable() {
		val state = contentState(
			termId = "historical-term",
			termKind = TermKind.HISTORICAL
		)

		assertEquals(TopBarConfig.Record, state.topBarConfig)
	}

	@Test
	fun when_termSelectionIsShown_then_termsAreGroupedAndSelectionIsForwarded() = runTuIndiceUiTest {
		var selectedTermId: String? = null

		setTuIndiceTestContent {
			RecordScreen(
				state = contentState(
					terms = listOf(
						academicTerm(
							id = "current-2026",
							periodYear = 2026,
							periodCode = AcademicTermPeriod.SEP_DEC,
							kind = TermKind.CURRENT
						),
						academicTerm(
							id = "synthetic-2026",
							periodYear = 2026,
							periodCode = AcademicTermPeriod.APR_JUL,
							kind = TermKind.SYNTHETIC
						),
						academicTerm(
							id = "historical-2024",
							periodYear = 2024,
							periodCode = AcademicTermPeriod.JAN_MAR,
							kind = TermKind.HISTORICAL
						)
					),
					selectedTermId = "current-2026"
				),
				selectedTermId = "current-2026",
				onSelectedTermChange = { selectedTermId = it },
				onRetryClick = {},
				onAttemptSelectionChange = { _, _, _, _ -> },
				onCreateSyntheticTermClick = {},
				onUpdateSyntheticTermClick = {},
				onDeleteSyntheticTermClick = {},
				showTermSelection = true
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			onAllNodesWithTag(RecordUiTags.TermSelectionSheet)
				.fetchSemanticsNodes()
				.isNotEmpty()
		}

		onNodeWithTag(RecordUiTags.TermSelectionSheet).assertIsDisplayed()
		onNodeWithTag(RecordUiTags.TermSelectionList).assertIsDisplayed()
		onNodeWithText("Proyección: trimestre actual y simulaciones").assertIsDisplayed()
		onNodeWithTag(RecordUiTags.termSelectionYear(2026)).assertIsDisplayed()
		onNodeWithTag(RecordUiTags.termSelectionYear(2024)).assertIsDisplayed()
		onNodeWithTag(
			testTag = RecordUiTags.termSelectionSelectedIcon("current-2026"),
			useUnmergedTree = true
		).assertIsDisplayed()
		onNodeWithTag(
			testTag = RecordUiTags.termSelectionKind("current-2026"),
			useUnmergedTree = true
		).assertIsDisplayed()
		onNodeWithTag(
			testTag = RecordUiTags.termSelectionKind("synthetic-2026"),
			useUnmergedTree = true
		).assertIsDisplayed()
		onNodeWithTag(
			testTag = RecordUiTags.termSelectionKind("historical-2024"),
			useUnmergedTree = true
		).assertIsDisplayed()

		onNodeWithTag(RecordUiTags.termSelectionOption("historical-2024")).performClick()

		assertEquals("historical-2024", selectedTermId)
	}

	@Test
	fun when_historicalTermSelectionIsShown_then_modeDescriptionExplainsFilteredTerms() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RecordScreen(
				state = contentState(
					viewMode = RecordViewMode.Historical,
					terms = listOf(
						academicTerm(
							id = "historical-2024",
							periodYear = 2024,
							periodCode = AcademicTermPeriod.JAN_MAR,
							kind = TermKind.HISTORICAL
						)
					),
					selectedTermId = "historical-2024"
				),
				selectedTermId = "historical-2024",
				onSelectedTermChange = {},
				onRetryClick = {},
				onAttemptSelectionChange = { _, _, _, _ -> },
				onCreateSyntheticTermClick = {},
				onUpdateSyntheticTermClick = {},
				onDeleteSyntheticTermClick = {},
				showTermSelection = true
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			onAllNodesWithTag(RecordUiTags.TermSelectionSheet)
				.fetchSemanticsNodes()
				.isNotEmpty()
		}

		onNodeWithText("Histórico: solo trimestres cerrados").assertIsDisplayed()
	}

	private fun contentState(
		termId: String = SyntheticTermId,
		termKind: TermKind,
		attempts: List<AcademicAttempt> = emptyList()
	): Record.State.Content {
		return contentState(
			terms = listOf(
				academicTerm(
					id = termId,
					periodYear = 2026,
					periodCode = AcademicTermPeriod.SEP_DEC,
					kind = termKind,
					attempts = attempts
				)
			),
			selectedTermId = termId
		)
	}

	private fun contentState(
		viewMode: RecordViewMode = RecordViewMode.Projection,
		terms: List<AcademicTerm>,
		selectedTermId: String
	): Record.State.Content {
		return Record.State.Content(
			viewMode = viewMode,
			record = AcademicRecord(
				id = "record",
				terms = terms
			),
			selectedTermId = selectedTermId
		)
	}

	private fun academicTerm(
		id: String,
		periodYear: Int,
		periodCode: AcademicTermPeriod,
		kind: TermKind,
		attempts: List<AcademicAttempt> = emptyList()
	): AcademicTerm {
		return AcademicTerm(
			id = id,
			periodYear = periodYear,
			periodCode = periodCode,
			kind = kind,
			attempts = attempts
		)
	}
}

private const val SyntheticTermId = "synthetic-term"
