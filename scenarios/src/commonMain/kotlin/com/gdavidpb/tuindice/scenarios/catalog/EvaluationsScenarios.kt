package com.gdavidpb.tuindice.scenarios.catalog

import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.scenario
import com.gdavidpb.tuindice.scenariokit.dsl.scrollUntilVisible
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.dsl.waitVisible
import com.gdavidpb.tuindice.scenariokit.model.Scroll
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts
import com.gdavidpb.tuindice.scenarios.fixture.E2eFixtures
import com.gdavidpb.tuindice.scenarios.fixture.Start
import com.gdavidpb.tuindice.scenarios.shared.Within
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.ui.MaincoreUiTags

val evaluationsSwipeDelete = scenario(
	"evaluations-swipe-delete",
	"evaluations",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers("evaluations.Evaluations.RemoveEvaluation")
	account(E2eAccounts.Canonical.id)

	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	tap(MaincoreUiTags.TuIndiceBottomBarEvaluationsItem)
	waitVisible(EvaluationsUiTags.EvaluationsContentContainer, Within.Long)
	scrollUntilVisible(
		EvaluationsUiTags.evaluationItemCard(E2eFixtures.GradedEvaluation.value),
		Scroll.ContentDown,
		Within.Wait
	)
	tap(EvaluationsUiTags.evaluationItemCard(E2eFixtures.GradedEvaluation.value))
	waitVisible(EvaluationsUiTags.EvaluationSwipeDeleteAction, Within.Action)
	waitVisible(EvaluationsUiTags.EvaluationSwipeToDismissContainer, Within.Assert)
	tap(EvaluationsUiTags.EvaluationSwipeDeleteAction)
	waitVisible(EvaluationsUiTags.DeleteEvaluationMessage, Within.Action)
	tap(BaseUiTags.ConfirmationDialogPositiveButton)
	waitVisible(BaseUiTags.SnackbarContainer, Within.Wait)
	waitVisible(EvaluationsUiTags.EvaluationsContentContainer, Within.Assert)
}
