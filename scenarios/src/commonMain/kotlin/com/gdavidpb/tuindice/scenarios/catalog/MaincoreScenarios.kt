package com.gdavidpb.tuindice.scenarios.catalog

import com.gdavidpb.tuindice.about.ui.AboutUiTags
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.debug.DebugLaunchArguments
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.back
import com.gdavidpb.tuindice.scenariokit.dsl.mockState
import com.gdavidpb.tuindice.scenariokit.dsl.onPlatform
import com.gdavidpb.tuindice.scenariokit.dsl.scenario
import com.gdavidpb.tuindice.scenariokit.dsl.submitTextEntry
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.dsl.text
import com.gdavidpb.tuindice.scenariokit.dsl.waitGone
import com.gdavidpb.tuindice.scenariokit.dsl.waitVisible
import com.gdavidpb.tuindice.scenariokit.model.Platform
import com.gdavidpb.tuindice.scenariokit.model.Scenario
import com.gdavidpb.tuindice.scenarios.fixture.Copy
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts
import com.gdavidpb.tuindice.scenarios.fixture.Start
import com.gdavidpb.tuindice.scenarios.shared.Within
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.ui.MaincoreUiTags

private val maincoreAppAvailabilityNotice = scenario(
	"maincore-app-availability-notice",
	"maincore",
	Start.Clean(
		notice = DebugLaunchArguments.AvailabilityNotice(
			enabled = true,
			title = Copy.NoticeTitle,
			message = Copy.NoticeMessage
		)
	).toLaunchSpec()
) {
	covers("maincore.Main.StartUp")

	waitVisible(MaincoreUiTags.AppAvailabilityNoticeScreen, Within.Sync)
	waitVisible(MaincoreUiTags.AppAvailabilityNoticeTitle, Within.Assert)
	waitVisible(MaincoreUiTags.AppAvailabilityNoticeMessage, Within.Assert)
	waitVisible(BaseUiTags.ErrorStateAnimation, Within.Assert)
	waitGone(MaincoreUiTags.TuIndiceNavHost, Within.Assert)
	waitGone(MaincoreUiTags.TuIndiceBottomBar, Within.Assert)
	waitGone(BaseUiTags.ErrorViewRetryButton, Within.Assert)
	waitVisible(MaincoreUiTags.AppAvailabilityNoticeRetryButton, Within.Assert)
	// Retry re-runs startup; the forced notice keeps the screen, proving the tap lands.
	tap(MaincoreUiTags.AppAvailabilityNoticeRetryButton)
	waitVisible(MaincoreUiTags.AppAvailabilityNoticeScreen, Within.Action)
}

private val maincoreBottomBarState = scenario(
	"maincore-bottom-bar-state",
	"maincore",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers("maincore.Main.SetLastMainSection")
	tags("smoke")
	account(E2eAccounts.Canonical.id)

	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	tap(MaincoreUiTags.TuIndiceBottomBarSummaryItem)
	waitVisible(SummaryUiTags.ContentContainer, Within.Wait)
	waitVisible(MaincoreUiTags.TuIndiceBottomBarSummaryItemSelected, Within.Assert)
	tap(MaincoreUiTags.TuIndiceBottomBarRecordItem)
	waitVisible(RecordUiTags.ContentContainer, Within.Long)
	waitVisible(MaincoreUiTags.TuIndiceBottomBarRecordItemSelected, Within.Assert)
	tap(MaincoreUiTags.TuIndiceBottomBarPensumItem)
	waitVisible(PensumUiTags.PensumScreen, Within.Wait)
	waitVisible(MaincoreUiTags.TuIndiceBottomBarPensumItemSelected, Within.Assert)
	tap(MaincoreUiTags.TuIndiceBottomBarEvaluationsItem)
	waitVisible(EvaluationsUiTags.EvaluationsContentContainer, Within.Wait)
	waitVisible(MaincoreUiTags.TuIndiceBottomBarEvaluationsItemSelected, Within.Assert)
	tap(MaincoreUiTags.TuIndiceBottomBarAboutItem)
	waitVisible(AboutUiTags.ContentContainer, Within.Wait)
	waitVisible(MaincoreUiTags.TuIndiceBottomBarAboutItemSelected, Within.Assert)
	tap(MaincoreUiTags.TuIndiceBottomBarSummaryItem)
	waitVisible(SummaryUiTags.ContentContainer, Within.Wait)
	waitVisible(MaincoreUiTags.TuIndiceBottomBarSummaryItemSelected, Within.Assert)
}

private val maincoreBackStack = scenario(
	"maincore-back-stack",
	"maincore",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers("maincore.Browser.NavigateTo")
	account(E2eAccounts.Canonical.id)

	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	tap(MaincoreUiTags.TuIndiceBottomBarAboutItem)
	waitVisible(AboutUiTags.ContentContainer, Within.Wait)
	tap(AboutUiTags.OpenTerms)
	waitVisible(MaincoreUiTags.BrowserContainer, Within.Wait)
	waitVisible(MaincoreUiTags.TuIndiceTopBarBackButton, Within.Assert)
	tap(MaincoreUiTags.TuIndiceTopBarBackButton)
	waitVisible(AboutUiTags.ContentContainer, Within.Action)
	tap(MaincoreUiTags.TuIndiceBottomBarPensumItem)
	waitVisible(PensumUiTags.PensumScreen, Within.Wait)
	tap(BaseUiTags.topBarActionButton(TopBarAction.SearchPensumAction))
	submitTextEntry()
	waitVisible(SubjectsUiTags.SearchScreen, Within.Action)
	waitVisible(MaincoreUiTags.TuIndiceTopBarBackButton, Within.Assert)
	tap(MaincoreUiTags.TuIndiceTopBarBackButton)
	waitVisible(PensumUiTags.PensumScreen, Within.Action)
	tap(MaincoreUiTags.TuIndiceBottomBarSummaryItem)
	waitVisible(SummaryUiTags.ContentContainer, Within.Wait)
}

// Navigation 3 tab back semantics: tab round trip on both platforms, and on Android the system back at a
// non-start tab root returns to the start tab. The iOS edge swipe is a UIKit gesture checked by manual QA.
private val maincoreTabStackPreservation = scenario(
	"maincore-tab-stack-preservation",
	"maincore",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers("maincore.Main.SetLastMainSection")
	account(E2eAccounts.Canonical.id)

	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	tap(MaincoreUiTags.TuIndiceBottomBarAboutItem)
	waitVisible(AboutUiTags.ContentContainer, Within.Wait)
	tap(MaincoreUiTags.TuIndiceBottomBarSummaryItem)
	waitVisible(SummaryUiTags.ContentContainer, Within.Wait)
	onPlatform(Platform.Android) {
		tap(MaincoreUiTags.TuIndiceBottomBarAboutItem)
		waitVisible(AboutUiTags.ContentContainer, Within.Wait)
		back()
		waitVisible(SummaryUiTags.ContentContainer, Within.Wait)
	}
}

private val maincoreBrowserExternalDialog = scenario(
	"maincore-browser-external-dialog",
	"maincore",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers(
		"about.About.OpenPrivacyPolicy",
		"maincore.Browser.NavigateTo",
		"maincore.Browser.OpenExternalResource"
	)
	account(E2eAccounts.Canonical.id)

	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	tap(MaincoreUiTags.TuIndiceBottomBarAboutItem)
	waitVisible(AboutUiTags.ContentContainer, Within.Wait)
	tap(AboutUiTags.OpenPrivacy)
	waitVisible(MaincoreUiTags.BrowserContainer, Within.Wait)
	// The container appears before the page inside it has rendered. Both platforms expose the page's text to the
	// hierarchy, and the page shows its link (`mocks/__files/e2e/privacy.html`): wait for it, then tap it.
	waitVisible(text(Copy.PrivacyPageExternalLink), Within.Wait)
	tap(text(Copy.PrivacyPageExternalLink))
	waitVisible(BaseUiTags.ExternalResourceMessage, Within.Action)
	waitVisible(BaseUiTags.ExternalResourceUrl, Within.Assert)
	tap(BaseUiTags.ConfirmationDialogNegativeButton)
	waitVisible(MaincoreUiTags.BrowserContainer, Within.Action)
	tap(MaincoreUiTags.TuIndiceTopBarBackButton)
	waitVisible(AboutUiTags.ContentContainer, Within.Action)
	tap(MaincoreUiTags.TuIndiceBottomBarSummaryItem)
	waitVisible(SummaryUiTags.ContentContainer, Within.Wait)
}

/**
 * A page that cannot be reached offers a retry: the mock resets the connection of the privacy page while it is in the
 * `Unreachable` state, the error view shows, and once the page answers again (`Recovered`) the retry loads it (YE-3).
 */
private val maincoreBrowserLoadFailedRetry = scenario(
	"maincore-browser-load-failed-retry",
	"maincore",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers("maincore.Browser.ClickRetry")
	account(E2eAccounts.Canonical.id)

	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	tap(MaincoreUiTags.TuIndiceBottomBarAboutItem)
	waitVisible(AboutUiTags.ContentContainer, Within.Wait)
	mockState("browser-privacy-page", "Unreachable")
	tap(AboutUiTags.OpenPrivacy)
	waitVisible(BaseUiTags.ErrorViewRetryButton, Within.Long)
	waitGone(MaincoreUiTags.BrowserContainer, Within.Assert)
	mockState("browser-privacy-page", "Recovered")
	tap(BaseUiTags.ErrorViewRetryButton)
	waitVisible(MaincoreUiTags.BrowserContainer, Within.Wait)
	waitVisible(text(Copy.PrivacyPageExternalLink), Within.Wait)
	waitGone(BaseUiTags.ErrorViewRetryButton, Within.Assert)
}

/** The scenarios of this module; list every new one here. */
val maincoreScenarios: List<Scenario> = listOf(
	maincoreAppAvailabilityNotice,
	maincoreBottomBarState,
	maincoreBackStack,
	maincoreTabStackPreservation,
	maincoreBrowserExternalDialog,
	maincoreBrowserLoadFailedRetry
)
