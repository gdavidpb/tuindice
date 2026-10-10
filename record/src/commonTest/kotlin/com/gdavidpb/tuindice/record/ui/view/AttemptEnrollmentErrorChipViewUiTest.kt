package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.testing.fitsWhole
import com.gdavidpb.tuindice.record.testing.textLayout
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalTestApi::class)
class AttemptEnrollmentErrorChipViewUiTest {
	@Test
	fun when_theUniversityFlaggedTheEnrollment_then_theChipShowsItsText_andOnlyTheTextIsRead() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AttemptEnrollmentErrorChipView(attemptId = "attempt-1", text = "Choque de horario · +1")
		}

		assertNodeVisible(RecordUiTags.attemptEnrollmentError("attempt-1"))
		onNodeWithText("Choque de horario · +1").assertIsDisplayed()
		// The warning icon is decorative: nothing but the text reaches a screen reader.
		onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.ContentDescription)).assertCountEquals(0)
	}

	@Test
	fun when_twoSubjectsAreFlagged_then_eachChipAnswersToItsOwnSubject() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Column {
				AttemptEnrollmentErrorChipView(attemptId = "attempt-1", text = "Choque de horario")
				AttemptEnrollmentErrorChipView(attemptId = "attempt-2", text = "Excede el límite de créditos")
			}
		}

		onNodeWithTag(RecordUiTags.attemptEnrollmentError("attempt-1"))
			.assert(hasAnyDescendant(hasText("Choque de horario")))
		onNodeWithTag(RecordUiTags.attemptEnrollmentError("attempt-2"))
			.assert(hasAnyDescendant(hasText("Excede el límite de créditos")))
	}

	@Test
	fun when_theTextDoesNotFit_then_itIsCutOnOneLine_insteadOfGrowingTheCard() = runTuIndiceUiTest {
		val text = "Excede el límite de créditos permitido para el trimestre según el reglamento vigente"

		setTuIndiceTestContent {
			AttemptEnrollmentErrorChipView(
				modifier = Modifier.width(160.dp),
				attemptId = "attempt-1",
				text = text
			)
		}

		val layout = onNodeWithText(text).textLayout()

		assertEquals(1, layout.lineCount)
		assertFalse(layout.fitsWhole, "the text that does not fit is cut on its one line")
		// The chip stays as wide as it was allowed to be.
		onNodeWithTag(RecordUiTags.attemptEnrollmentError("attempt-1")).assertWidthIsEqualTo(160.dp)
	}
}
