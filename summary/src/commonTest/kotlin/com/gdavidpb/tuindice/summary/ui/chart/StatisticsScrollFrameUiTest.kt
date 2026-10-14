package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class StatisticsScrollFrameUiTest {
	@Test
	fun when_contentOverflows_then_itOpensAtTheLatestEndAndFadesTheLeadingEdge() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			StatisticsScrollFrame(modifier = Modifier.fillMaxWidth()) {
				Box(modifier = Modifier.width(600.dp).height(40.dp)) {
					Box(
						modifier = Modifier
							.align(Alignment.CenterStart)
							.size(40.dp)
							.testTag(StartTag)
					)
					Box(
						modifier = Modifier
							.align(Alignment.CenterEnd)
							.size(40.dp)
							.testTag(EndTag)
					)
				}
			}
		}
		waitForIdle()

		assertNodeVisible(EndTag)
		onNodeWithTag(StartTag).assertIsNotDisplayed()
		assertNodeVisible(SummaryUiTags.StatisticsLeadingEdge)
		assertNodeHidden(SummaryUiTags.StatisticsTrailingEdge)
	}

	@Test
	fun when_contentFits_then_noEdgeIsFaded() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			StatisticsScrollFrame(modifier = Modifier.fillMaxWidth()) {
				Box(modifier = Modifier.width(100.dp).height(40.dp))
			}
		}
		waitForIdle()

		assertNodeHidden(SummaryUiTags.StatisticsLeadingEdge)
		assertNodeHidden(SummaryUiTags.StatisticsTrailingEdge)
	}

	private companion object {
		const val StartTag = "statistics_scroll_start"
		const val EndTag = "statistics_scroll_end"
	}
}
