package com.gdavidpb.tuindice.record.ui.screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.intl.Locale
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.TuIndiceTestSizeClass
import com.gdavidpb.tuindice.testkit.ui.assertNodeEnabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * RTL and accessibility-semantics matrix for the Record screen.
 *
 * Mirrors the minimal state construction of [RecordScreenUiTest]
 * (its helpers are file-private, so equivalent builders live here).
 */
@OptIn(ExperimentalTestApi::class)
class RecordRtlA11yUiTest {
	@Test
	fun when_layoutIsRtl_then_recordCriticalNodesRemainVisibleAndEnabled() = runTuIndiceUiTest {
		setTuIndiceTestContent(locale = Locale("ar")) {
			RecordScreen(
				state = syntheticContentState(),
				selectedTermId = RtlSyntheticTermId,
				onSelectedTermChange = {},
				onRetryClick = {},
				onAttemptSelectionChange = { _, _, _, _ -> },
				onCreateSyntheticTermClick = {},
				onUpdateSyntheticTermClick = {},
				onDeleteSyntheticTermClick = {}
			)
		}

		// Content scaffold plus the synthetic-term action stack stay visible mirrored.
		assertNodeVisible(RecordUiTags.ContentContainer)
		assertNodeVisible(RecordUiTags.EditSyntheticTermButton)
		assertNodeVisible(RecordUiTags.DeleteSyntheticTermButton)
		assertNodeVisible(RecordUiTags.CreateSyntheticTermFab)
		assertNodeEnabled(RecordUiTags.CreateSyntheticTermFab)

		// The attempt row (list content) keeps rendering its uppercase subject name.
		onNodeWithText("MATEMÁTICAS II").assertExists()
		onNodeWithTag(RecordUiTags.attemptCard("attempt-1")).assertExists()
	}

	@Test
	fun when_layoutIsRtl_then_overlayActionsStillForwardCallbacks() = runTuIndiceUiTest {
		var editedTermId: String? = null
		var deletedTermId: String? = null
		var createClicks = 0

		setTuIndiceTestContent(locale = Locale("ar")) {
			RecordScreen(
				state = syntheticContentState(),
				selectedTermId = RtlSyntheticTermId,
				onSelectedTermChange = {},
				onRetryClick = {},
				onAttemptSelectionChange = { _, _, _, _ -> },
				onCreateSyntheticTermClick = { createClicks++ },
				onUpdateSyntheticTermClick = { editedTermId = it },
				onDeleteSyntheticTermClick = { deletedTermId = it }
			)
		}

		// Mirrored layout must keep the corner action stack tappable.
		onNodeWithTag(RecordUiTags.EditSyntheticTermButton).performClick()
		onNodeWithTag(RecordUiTags.DeleteSyntheticTermButton).performClick()
		onNodeWithTag(RecordUiTags.CreateSyntheticTermFab).performClick()

		assertEquals(RtlSyntheticTermId, editedTermId)
		assertEquals(RtlSyntheticTermId, deletedTermId)
		assertEquals(1, createClicks)
	}

	@Test
	fun when_a11ySemanticsInspected_then_actionButtonsExposeDescriptionsAndClickActions() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			RecordScreen(
				state = syntheticContentState(),
				selectedTermId = RtlSyntheticTermId,
				onSelectedTermChange = {},
				onRetryClick = {},
				onAttemptSelectionChange = { _, _, _, _ -> },
				onCreateSyntheticTermClick = {},
				onUpdateSyntheticTermClick = {},
				onDeleteSyntheticTermClick = {}
			)
		}

		// Icon-only FABs are labeled via contentDescription
		// (label_edit/delete_synthetic_term, a11y_create_synthetic_term).
		onNodeWithTag(RecordUiTags.EditSyntheticTermButton)
			.assert(hasClickAction())
			.assert(hasContentDescription("Modificar trimestre"))
		onNodeWithTag(RecordUiTags.DeleteSyntheticTermButton)
			.assert(hasClickAction())
			.assert(hasContentDescription("Eliminar trimestre"))
		onNodeWithTag(RecordUiTags.CreateSyntheticTermFab)
			.assert(hasClickAction())
			.assert(hasContentDescription("Crear término de proyección"))

		// The attempt content exposes its subject name as plain text.
		onNodeWithText("MATEMÁTICAS II").assertExists()
	}

	@Test
	fun when_compactRtlWithIncreasedDensity_then_contentRemainsComposedAndActionsEnabled() = runTuIndiceUiTest {
		// Compact size class plus 1.25x density (the kit's scale knob).
		setTuIndiceTestContent(
			sizeClass = TuIndiceTestSizeClass.Compact,
			density = 1.25f,
			locale = Locale("ar")
		) {
			RecordScreen(
				state = syntheticContentState(),
				selectedTermId = RtlSyntheticTermId,
				onSelectedTermChange = {},
				onRetryClick = {},
				onAttemptSelectionChange = { _, _, _, _ -> },
				onCreateSyntheticTermClick = {},
				onUpdateSyntheticTermClick = {},
				onDeleteSyntheticTermClick = {}
			)
		}

		// Top-region scaffold stays visible at the larger scale; the bottom-anchored
		// action stack may touch the scaled viewport edge, so existence + enabled
		// state is asserted instead of strict visibility.
		assertNodeVisible(RecordUiTags.ContentContainer)
		onNodeWithText("MATEMÁTICAS II").assertExists()
		onNodeWithTag(RecordUiTags.CreateSyntheticTermFab).assertExists().assertIsEnabled()
		onNodeWithTag(RecordUiTags.EditSyntheticTermButton).assertExists().assertIsEnabled()
	}

	private fun syntheticContentState(): Record.State.Content {
		// Projection mode keeps the synthetic-term action stack visible (RecordScreen).
		return Record.State.Content(
			viewMode = RecordViewMode.Projection,
			record = AcademicRecord(
				id = "record",
				terms = listOf(
					AcademicTerm(
						id = RtlSyntheticTermId,
						periodYear = 2026,
						periodCode = AcademicTermPeriod.SEP_DEC,
						kind = TermKind.SYNTHETIC,
						attempts = listOf(
							AcademicAttempt(
								id = "attempt-1",
								subjectCode = "MA1112",
								subjectName = "Matemáticas II",
								credits = 4
							)
						)
					)
				)
			),
			selectedTermId = RtlSyntheticTermId
		)
	}
}

private const val RtlSyntheticTermId = "synthetic-term"
