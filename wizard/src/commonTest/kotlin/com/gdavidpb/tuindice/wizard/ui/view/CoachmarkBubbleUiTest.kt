package com.gdavidpb.tuindice.wizard.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import com.gdavidpb.tuindice.wizard.presentation.model.Coachmark
import com.gdavidpb.tuindice.wizard.presentation.model.CoachmarkId
import com.gdavidpb.tuindice.wizard.presentation.model.contextualCoachmarks
import com.gdavidpb.tuindice.wizard.ui.CoachmarkUiTags
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class CoachmarkBubbleUiTest {
	@Test
	fun when_coachmarkRendered_then_displaysItsTitleAndMessage() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CoachmarkBubble(
				coachmark = coachmark(CoachmarkId.Summary),
				hasPreviousCoachmark = false,
				hasNextCoachmark = false,
				onPreviousActionClick = {},
				onPrimaryActionClick = {}
			)
		}

		assertNodeVisible(CoachmarkUiTags.Bubble)
		onNodeWithTag(CoachmarkUiTags.currentCoachmark(CoachmarkId.Summary))
			.assertTextEquals("Resumen")
		onNodeWithText("Revisa aquí tu índice, créditos, materias y estado de sincronización.")
			.assertIsDisplayed()
		onAllNodesWithTag(CoachmarkUiTags.currentCoachmark(CoachmarkId.Record))
			.assertCountEquals(0)
	}

	@Test
	fun when_lastCoachmarkWithoutPrevious_then_showsOnlyConfirmAction() = runTuIndiceUiTest {
		var previousClicks = 0
		var primaryClicks = 0

		setTuIndiceTestContent {
			CoachmarkBubble(
				coachmark = coachmark(CoachmarkId.Summary),
				hasPreviousCoachmark = false,
				hasNextCoachmark = false,
				onPreviousActionClick = { previousClicks++ },
				onPrimaryActionClick = { primaryClicks++ }
			)
		}

		onAllNodesWithTag(CoachmarkUiTags.BackButton).assertCountEquals(0)
		onNodeWithTag(CoachmarkUiTags.ConfirmButton)
			.assertTextEquals("Entendido")
			.performClick()

		assertEquals(1, primaryClicks)
		assertEquals(0, previousClicks)
	}

	@Test
	fun when_hasNextCoachmark_then_primaryActionTextIsNext() = runTuIndiceUiTest {
		var primaryClicks = 0

		setTuIndiceTestContent {
			CoachmarkBubble(
				coachmark = coachmark(CoachmarkId.Record),
				hasPreviousCoachmark = false,
				hasNextCoachmark = true,
				onPreviousActionClick = {},
				onPrimaryActionClick = { primaryClicks++ }
			)
		}

		onNodeWithTag(CoachmarkUiTags.currentCoachmark(CoachmarkId.Record))
			.assertTextEquals("Informe académico")
		onNodeWithTag(CoachmarkUiTags.ConfirmButton)
			.assertTextEquals("Siguiente")
			.performClick()

		assertEquals(1, primaryClicks)
	}

	@Test
	fun when_hasPreviousCoachmark_then_backActionInvokesPreviousCallback() = runTuIndiceUiTest {
		var previousClicks = 0
		var primaryClicks = 0

		setTuIndiceTestContent {
			CoachmarkBubble(
				coachmark = coachmark(CoachmarkId.RecordControls),
				hasPreviousCoachmark = true,
				hasNextCoachmark = false,
				onPreviousActionClick = { previousClicks++ },
				onPrimaryActionClick = { primaryClicks++ }
			)
		}

		onNodeWithTag(CoachmarkUiTags.BackButton)
			.assertTextEquals("Atrás")
			.performClick()

		assertEquals(1, previousClicks)
		assertEquals(0, primaryClicks)
	}

	private fun coachmark(id: CoachmarkId): Coachmark {
		return contextualCoachmarks().first { coachmark -> coachmark.id == id }
	}
}
