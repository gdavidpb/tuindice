package com.gdavidpb.tuindice.scenarios.catalog

import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.StepBuilder
import com.gdavidpb.tuindice.scenariokit.dsl.SwipeDirection
import com.gdavidpb.tuindice.scenariokit.dsl.scenario
import com.gdavidpb.tuindice.scenariokit.dsl.scrollUntilVisible
import com.gdavidpb.tuindice.scenariokit.dsl.swipeFrom
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.dsl.waitGone
import com.gdavidpb.tuindice.scenariokit.dsl.waitVisible
import com.gdavidpb.tuindice.scenariokit.model.MockState
import com.gdavidpb.tuindice.scenariokit.model.Scenario
import com.gdavidpb.tuindice.scenariokit.model.Scroll
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts
import com.gdavidpb.tuindice.scenarios.fixture.Start
import com.gdavidpb.tuindice.scenarios.shared.Within
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.TermReportUiTags
import com.gdavidpb.tuindice.summary.ui.share.ShareTermUiTags
import com.gdavidpb.tuindice.ui.MaincoreUiTags

/**
 * The term report ("Tu trimestre"): the report the service wrote about one term, offered as a line of the summary
 * until the student opens it. Its reports are about [REPORT_TERM_KEY], a closed term of the Canonical record, because
 * the sheet that shares a term lists the closed ones only. The Retry scenario uses an account whose record is refused,
 * so the report cannot be read there and the screen offers to try again.
 */
private const val REPORT_TERM_KEY = "2026-JAN_MAR"

// The service answers its report only in these states of `assistant-insights` (`insights-report-get.json` and
// `insights-report-retry-get.json`), one state of its own for each scenario that starts with it; every other scenario
// gets the empty list of `insights-get.json`.
private val reportAtLaunch = listOf(MockState("assistant-insights", "ReportPresent"))
private val reportAtRetry = listOf(MockState("assistant-insights", "ReportPresentRetry"))

/** On the hub of Summary, taps the line of the report, which the hub shows once the service has answered. */
private fun StepBuilder.openTermReportFromHub() {
	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	scrollUntilVisible(TermReportUiTags.TermReportEntry, Scroll.ContentDown, Within.Long)
	tap(TermReportUiTags.TermReportEntry)
}

private val termreportOpen = scenario(
	"termreport-open",
	"termreport",
	Start.Seeded(E2eAccounts.Canonical, mockStates = reportAtLaunch).toLaunchSpec()
) {
	tags("smoke")
	covers(
		"summary.TermReport.Open",
		"summary.TermReport.Share",
		"summary.TermReport.Rate",
		"summary.TermReport.OpenNextAction"
	)
	account(E2eAccounts.Canonical.id)

	openTermReportFromHub()
	waitVisible(TermReportUiTags.TermReportContent, Within.Long)
	waitVisible(TermReportUiTags.TermReportHero, Within.Assert)
	waitVisible(TermReportUiTags.TermReportSubjects, Within.Assert)
	// The share action sends the term to the sheet that shares terms, which opens with the term of the report chosen.
	tap(BaseUiTags.topBarActionButton(TopBarAction.TermReportShareAction))
	waitVisible(SummaryUiTags.ShareTermSheet, Within.Wait)
	waitVisible(ShareTermUiTags.termSelected(REPORT_TERM_KEY), Within.Assert)
	swipeFrom(BaseUiTags.ConfirmationDialogTitle, SwipeDirection.Down)
	waitGone(SummaryUiTags.ShareTermSheet, Within.Action)
	waitVisible(TermReportUiTags.TermReportContent, Within.Assert)
	scrollUntilVisible(TermReportUiTags.termReportThumb(useful = true), Scroll.ContentDown, Within.Long)
	tap(TermReportUiTags.termReportThumb(useful = true))
	waitVisible(TermReportUiTags.termReportThumb(useful = true), Within.Assert)
	// Last, because the button leaves the report: the first next action opens the evaluations, as the chat does.
	scrollUntilVisible(TermReportUiTags.termReportNextAction(0), Scroll.ContentDown, Within.Long)
	tap(TermReportUiTags.termReportNextAction(0))
	waitVisible(MaincoreUiTags.TuIndiceBottomBarEvaluationsItemSelected, Within.Wait)
	waitVisible(EvaluationsUiTags.EvaluationsContentContainer, Within.Action)
}

private val termreportRetry = scenario(
	"termreport-retry",
	"termreport",
	Start.Seeded(E2eAccounts.RecordDenied, mockStates = reportAtRetry).toLaunchSpec()
) {
	covers("summary.TermReport.Retry")
	account(E2eAccounts.RecordDenied.id)

	openTermReportFromHub()
	// The record is refused to this account, so the report cannot be read and the screen offers to try again.
	waitVisible(TermReportUiTags.TermReportFailed, Within.Long)
	tap(TermReportUiTags.TermReportRetryButton)
	// The retry reads the report again: that read takes its delay, so the loading shows, and the refusal stands.
	waitVisible(SummaryUiTags.LoadingIndicator, Within.Action)
	waitVisible(TermReportUiTags.TermReportFailed, Within.Long)
	waitVisible(TermReportUiTags.TermReportRetryButton, Within.Assert)
	// Opening the report marked it as seen, so its line leaves the hub even though the report could not be read.
	tap(MaincoreUiTags.TuIndiceTopBarBackButton)
	waitVisible(SummaryUiTags.ContentContainer, Within.Action)
	waitGone(TermReportUiTags.TermReportEntry, Within.Assert)
}

/** The scenarios of the `termreport` module; list every new one here. */
val termreportScenarios: List<Scenario> = listOf(
	termreportOpen,
	termreportRetry
)
