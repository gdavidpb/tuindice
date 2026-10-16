package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryStatisticsEmptyHeading
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsEmptyViewUiTest {
	@Test
	fun when_titleAndActionAreGiven_then_theActionIsTapped() = runTuIndiceUiTest {
		var tapped = false

		setTuIndiceTestContent {
			SummaryStatisticsEmptyView(
				heading = SummaryStatisticsEmptyHeading(
					title = "Tus evaluaciones no están aquí",
					icon = Icons.AutoMirrored.Outlined.Assignment
				),
				message = "Ábrelas en Evaluaciones para verlas aquí.",
				actionText = "Ir a Evaluaciones",
				onActionClick = { tapped = true }
			)
		}

		onNodeWithText("Tus evaluaciones no están aquí").assertIsDisplayed()
		onNodeWithTag(SummaryUiTags.StatisticsEmptyIcon, useUnmergedTree = true).assertIsDisplayed()
		onNodeWithText("Ábrelas en Evaluaciones para verlas aquí.").assertIsDisplayed()
		onNodeWithText("Ir a Evaluaciones").performClick()

		assertTrue(tapped)
	}

	@Test
	fun when_onlyAMessageIsGiven_then_itIsTheOnlyTextAndOffersNoAction() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsEmptyView(message = "Aún no hay evaluaciones en este trimestre.")
		}

		onNodeWithText("Aún no hay evaluaciones en este trimestre.").assertIsDisplayed()
		onAllNodesWithTag(SummaryUiTags.StatisticsEmptyAction).assertCountEquals(0)
		onAllNodesWithTag(SummaryUiTags.StatisticsEmptyIcon, useUnmergedTree = true).assertCountEquals(0)
	}
}
