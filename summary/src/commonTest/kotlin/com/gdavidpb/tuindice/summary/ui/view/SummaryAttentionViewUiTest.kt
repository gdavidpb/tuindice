package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.testing.HUB_FIXTURE_ATTENTION_WEEK_START
import com.gdavidpb.tuindice.summary.testing.WithLargeLetter
import com.gdavidpb.tuindice.summary.testing.assertTextIsWhole
import com.gdavidpb.tuindice.summary.testing.summaryHubItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SummaryAttentionViewUiTest {
	private val items = summaryHubItem().attention

	@Test
	fun when_thereArePoints_then_eachIsALineThatNamesTheThing() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryAttentionView(items = items, onItemClick = {})
		}

		assertNodeVisible(SummaryUiTags.AttentionContainer)
		onNodeWithTag(SummaryUiTags.attentionItem("ungraded")).assertIsDisplayed().assertHasClickAction()
		onNodeWithText("2 evaluaciones sin nota").assertIsDisplayed()
		onNodeWithTag(SummaryUiTags.attentionItem("enrollment-CI5311")).assertIsDisplayed().assertHasClickAction()
		onNodeWithText("Choque de horario en CI5311").assertIsDisplayed()
	}

	@Test
	fun when_aLineIsTapped_then_itAsksForItsTarget() = runTuIndiceUiTest {
		val targets = mutableListOf<SummaryHubTarget>()

		setTuIndiceTestContent {
			SummaryAttentionView(items = items, onItemClick = { target -> targets += target })
		}

		onNodeWithTag(SummaryUiTags.attentionItem("ungraded")).performClick()
		onNodeWithTag(SummaryUiTags.attentionItem("enrollment-CI5311")).performClick()

		assertEquals(
			listOf(
				SummaryHubTarget.EvaluationsWeek(
						weekStart = HUB_FIXTURE_ATTENTION_WEEK_START,
						evaluationId = "evaluation-1"
					),
				SummaryHubTarget.Record
			),
			targets
		)
	}

	@Test
	fun when_thereIsOnePoint_then_noOtherLineIsShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryAttentionView(items = items.take(1), onItemClick = {})
		}

		assertNodeVisible(SummaryUiTags.attentionItem("ungraded"))
		onNodeWithText("Choque de horario en CI5311").assertDoesNotExist()
	}

	@Test
	fun when_theLetterIsLargeOnANarrowScreen_then_noLineIsCut() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WithLargeLetter {
				SummaryAttentionView(items = items, onItemClick = {})
			}
		}

		assertTextIsWhole("2 evaluaciones sin nota")
		assertTextIsWhole("Choque de horario en CI5311")
	}
}
