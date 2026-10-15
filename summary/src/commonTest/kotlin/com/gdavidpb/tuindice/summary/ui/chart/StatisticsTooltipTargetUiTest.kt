package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import com.gdavidpb.tuindice.base.ui.model.RichTooltipText
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class, ExperimentalMaterial3Api::class)
class StatisticsTooltipTargetUiTest {
	@Test
	fun when_tappedWhileClosed_then_asksToOpen() = runTuIndiceUiTest {
		val requests = mutableListOf<Boolean>()

		setTuIndiceTestContent {
			StatisticsTooltipTarget(
				target = target(),
				isOpen = false,
				onOpenChange = { requests += it }
			) {}
		}

		onNodeWithTag(TargetTag).performTouchInput { click() }
		waitForIdle()

		assertEquals(listOf(true), requests)
	}

	@Test
	fun when_openAndTapped_then_itsTooltipShowsAndTheTapAsksToClose() = runTuIndiceUiTest {
		val requests = mutableListOf<Boolean>()

		setTuIndiceTestContent {
			StatisticsTooltipTarget(
				target = target(),
				isOpen = true,
				onOpenChange = { requests += it }
			) {}
		}
		assertNodeVisible(BodyTag, useUnmergedTree = true)

		onNodeWithTag(TargetTag).performTouchInput { click() }
		waitForIdle()

		// The toolkit also reports the tap as an outside tap on the open tooltip, so the target may ask twice. Both
		// asks close it, and closing twice is no different from closing once.
		assertTrue(requests.isNotEmpty() && requests.none { request -> request }, "Requests were $requests")
	}

	private fun target(): StatisticsTarget {
		return StatisticsTarget(
			tag = TargetTag,
			description = "Trimestre 1",
			actionLabel = "Ver detalle",
			tooltip = RichTooltipText(lines = listOf("Índice 4.2"), testTag = BodyTag)
		)
	}

	private companion object {
		const val TargetTag = "statistics_target_test"
		const val BodyTag = "statistics_target_test_body"
	}
}
