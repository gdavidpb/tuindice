package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.record.presentation.model.AttemptItem
import com.gdavidpb.tuindice.record.testing.fitsWhole
import com.gdavidpb.tuindice.record.testing.textLayout
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AttemptSubjectRowViewUiTest {
	@Test
	fun when_theSubjectHasSectionAndClassroom_then_theySitBetweenTheCodeAndTheCredits() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AttemptSubjectRowView(item = attemptItem(detailText = "Sección 1 · MYS-116"))
		}

		onNodeWithTag(RecordUiTags.attemptSubjectChip("attempt-1"))
			.assertTextEquals("CI5311")
			.assertHasNoClickAction()
		onNodeWithTag(RecordUiTags.attemptDetail("attempt-1")).assertTextEquals("Sección 1 · MYS-116")
		onNodeWithText("3 UC").assertIsDisplayed()

		val code = onNodeWithTag(RecordUiTags.attemptSubjectChip("attempt-1")).getUnclippedBoundsInRoot()
		val detail = onNodeWithTag(RecordUiTags.attemptDetail("attempt-1")).getUnclippedBoundsInRoot()
		val credits = onNodeWithText("3 UC").getUnclippedBoundsInRoot()

		assertTrue(code.right <= detail.left, "the detail starts after the code chip")
		assertTrue(detail.right <= credits.left, "the credits come after the detail")
	}

	@Test
	fun when_theSubjectHasNoDetail_then_onlyTheCodeAndTheCreditsAreShown_atEachEnd() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AttemptSubjectRowView(item = attemptItem(detailText = null))
		}

		assertNodeVisible(RecordUiTags.attemptSubjectChip("attempt-1"))
		assertNodeHidden(RecordUiTags.attemptDetail("attempt-1"))
		onNodeWithText("3 UC").assertIsDisplayed()

		// The free space still sits between them: the credits end where the row does.
		val code = onNodeWithTag(RecordUiTags.attemptSubjectChip("attempt-1")).getUnclippedBoundsInRoot()
		val credits = onNodeWithText("3 UC").getUnclippedBoundsInRoot()

		assertTrue(credits.left - code.right > credits.right - credits.left, "the credits are pushed to the end")
	}

	@Test
	fun when_theDetailIsLongerThanTheFreeSpace_then_itGivesWay_andTheCreditsStayWhole() = runTuIndiceUiTest {
		val detail = "Sección 12 · Edificio de Mecánica y Materiales, laboratorio de fenómenos de transporte"

		setTuIndiceTestContent {
			AttemptSubjectRowView(item = attemptItem(detailText = detail))
		}

		val detailLayout = onNodeWithTag(RecordUiTags.attemptDetail("attempt-1")).textLayout()

		assertEquals(1, detailLayout.lineCount)
		assertFalse(detailLayout.fitsWhole, "the detail is cut on its one line")
		// It is the detail that gives way: the code and the credits are drawn whole at each end.
		onNodeWithText("3 UC").assertIsDisplayed()
		assertTrue(onNodeWithText("3 UC").textLayout().fitsWhole, "the credits are not cut")
		assertTrue(
			actual = onNodeWithTag(RecordUiTags.attemptSubjectChip("attempt-1")).textLayout().fitsWhole,
			message = "the code is not cut"
		)
	}

	private fun attemptItem(detailText: String?) = AttemptItem(
		attemptId = "attempt-1",
		subjectCode = "CI5311",
		grade = 0,
		codeText = "CI5311",
		nameText = "INGENIERIA DE SOFTWARE",
		gradeText = "",
		creditsText = "3 UC",
		codeColor = Color(0xFF1E3A5F),
		codeContainerColor = Color(0xFFDCE8F5),
		isReadOnly = false,
		detailText = detailText
	)
}
