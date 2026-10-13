package com.gdavidpb.tuindice.scenarios.shared

import com.gdavidpb.tuindice.scenariokit.dsl.StepBuilder
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.dsl.waitVisible
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.ui.MaincoreUiTags

/**
 * From the seeded launch of a hub account to the hub with its downloads in: the sync is over and the evaluations
 * (which the sync revalidates, and the hub never asks for) are read, which is when the points of attention exist.
 */
fun StepBuilder.waitSummaryHub() {
	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	waitVisible(SummaryUiTags.AttentionContainer, Within.Long)
}

/** Back on the Summary tab through the bottom bar, with the tab selected and its content in front. */
fun StepBuilder.returnToSummaryTab() {
	tap(MaincoreUiTags.TuIndiceBottomBarSummaryItem)
	waitVisible(MaincoreUiTags.TuIndiceBottomBarSummaryItemSelected, Within.Action)
	waitVisible(SummaryUiTags.ContentContainer, Within.Action)
}
