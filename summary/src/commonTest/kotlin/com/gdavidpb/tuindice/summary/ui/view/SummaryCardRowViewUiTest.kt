package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryCardIcon
import com.gdavidpb.tuindice.summary.presentation.model.SummaryCardItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SummaryCardRowViewUiTest {
	@Test
	fun when_theCardHasAction_then_itsButtonDispatchesItsTarget() = runTuIndiceUiTest {
		val targets = mutableListOf<SummaryHubTarget>()

		setTuIndiceTestContent {
			SummaryCardRowView(
				item = card(),
				actions = SummaryHubActions(onTargetClick = { target -> targets += target }),
				hasArrow = false
			)
		}
		onNodeWithTag(SummaryUiTags.CardAction).performClick()

		assertEquals(listOf<SummaryHubTarget>(SummaryHubTarget.Record), targets)
	}

	@Test
	fun when_theCardIsShown_then_itsTitleAndMessageAreShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryCardRowView(
				item = card(),
				actions = SummaryHubActions(onTargetClick = {}),
				hasArrow = true
			)
		}

		onNodeWithText("Todo listo").assertIsDisplayed()
		onNodeWithText("Nada pendiente").assertIsDisplayed()
	}

	private fun card() = SummaryCardItem(
		icon = SummaryCardIcon.Done,
		titleText = UiText.Raw("Todo listo"),
		messageText = UiText.Raw("Nada pendiente"),
		action = SummaryCardItem.Action(text = UiText.Raw("Ver registro"), target = SummaryHubTarget.Record)
	)
}
