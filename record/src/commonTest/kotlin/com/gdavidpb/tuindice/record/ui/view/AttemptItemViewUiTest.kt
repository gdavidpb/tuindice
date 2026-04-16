package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptBadge
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.base.domain.model.GradingMode
import com.gdavidpb.tuindice.record.presentation.model.AttemptItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class AttemptItemViewUiTest {
	@Test
	fun when_qualitativeAttemptIsEditable_then_selectorMovesToHeaderAndDispatchesSelection() = runTuIndiceUiTest {
		var selectedOutcome: AttemptOutcome? = null

		setTuIndiceTestContent {
			AttemptItemView(
				item = qualitativeAttemptItem(outcome = AttemptOutcome.APPROVED),
				onGradeChange = { _, newOutcome, _ ->
					selectedOutcome = newOutcome
				}
			)
		}

		assertNodeVisible(RecordUiTags.attemptStatusSelector("attempt-1"))
		onNodeWithTag(RecordUiTags.attemptStatusSelector("attempt-1"))
			.assertTextContains("Aprobada")

		onNodeWithTag(RecordUiTags.attemptStatusSelector("attempt-1")).performClick()
		onNodeWithTag(
			RecordUiTags.attemptStatusOption(
				attemptId = "attempt-1",
				status = AttemptOutcome.FAILED.name.lowercase()
			)
		).performClick()

		assertEquals(AttemptOutcome.FAILED, selectedOutcome)
	}

	@Test
	fun when_qualitativeAttemptIsEditableAndWithoutEffect_then_metadataIsVisibleBelowSelector() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AttemptItemView(
				item = qualitativeAttemptItem(
					outcome = AttemptOutcome.APPROVED,
					badge = AttemptBadge.WITHOUT_EFFECT
				),
				onGradeChange = { _, _, _ -> }
			)
		}

		assertNodeVisible(RecordUiTags.attemptStatusSelector("attempt-1"))
		onNodeWithTag(RecordUiTags.attemptStatusSelector("attempt-1"))
			.assertTextContains("Aprobada")
		onNodeWithText("Sin efecto").assertIsDisplayed()
	}

	@Test
	fun when_qualitativeAttemptIsReadOnly_then_selectorIsHiddenAndChipRemainsVisible() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AttemptItemView(
				item = qualitativeAttemptItem(
					outcome = AttemptOutcome.APPROVED,
					isReadOnly = true
				),
				onGradeChange = { _, _, _ -> }
			)
		}

		assertNodeHidden(RecordUiTags.attemptStatusSelector("attempt-1"))
		assertNodeVisible(RecordUiTags.attemptStatusChip("attempt-1"))
		onNodeWithText("Aprobada").assertIsDisplayed()
	}

	private fun qualitativeAttemptItem(
		outcome: AttemptOutcome?,
		isReadOnly: Boolean = false,
		badge: AttemptBadge = AttemptBadge.NONE
	) = AttemptItem(
		attemptId = "attempt-1",
		grade = 0,
		gradingMode = GradingMode.QUALITATIVE_PASS_FAIL,
		outcome = outcome,
		badge = badge,
		codeText = "EP5406",
		nameText = "Proyecto de grado",
		gradeText = "",
		creditsText = "9 UC",
		codeColor = Color(0xFF1E3A5F),
		codeContainerColor = Color(0xFFDCE8F5),
		isReadOnly = isReadOnly
	)
}
