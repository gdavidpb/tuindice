package com.gdavidpb.tuindice.record.ui.screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
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

	private fun contentState(
		termId: String = SyntheticTermId,
		termKind: TermKind,
		attempts: List<AcademicAttempt> = emptyList()
	): Record.State.Content {
		return Record.State.Content(
			viewMode = RecordViewMode.Projection,
			record = AcademicRecord(
				id = "record",
				terms = listOf(
					AcademicTerm(
						id = termId,
						periodYear = 2026,
						periodCode = AcademicTermPeriod.SEP_DEC,
						kind = termKind,
						attempts = attempts
					)
				)
			),
			selectedTermId = termId
		)
	}
}

private const val SyntheticTermId = "synthetic-term"
