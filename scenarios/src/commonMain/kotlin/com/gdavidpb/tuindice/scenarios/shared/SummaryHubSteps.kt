package com.gdavidpb.tuindice.scenarios.shared

import com.gdavidpb.tuindice.scenariokit.dsl.StepBuilder
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.dsl.waitVisible
import com.gdavidpb.tuindice.scenarios.fixture.E2eInputs
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.ui.MaincoreUiTags

/**
 * From the seeded launch of a hub account to the hub with its downloads in: the sync is over and the evaluations (which
 * the sync revalidates, and the hub never asks for) have arrived. The ungraded row of the points of attention exists
 * only once they have; the container of the points shows before, with the schedule clash alone, and the hub then grows
 * above a row that is about to be tapped.
 */
fun StepBuilder.waitSummaryHub() {
	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	waitVisible(SummaryUiTags.attentionItem(E2eInputs.SummaryAttentionUngradedKey), Within.Long)
}

/** Back on the Summary tab through the bottom bar, with the tab selected and its content in front. */
fun StepBuilder.returnToSummaryTab() {
	tap(MaincoreUiTags.TuIndiceBottomBarSummaryItem)
	waitVisible(MaincoreUiTags.TuIndiceBottomBarSummaryItemSelected, Within.Action)
	waitVisible(SummaryUiTags.ContentContainer, Within.Action)
}
