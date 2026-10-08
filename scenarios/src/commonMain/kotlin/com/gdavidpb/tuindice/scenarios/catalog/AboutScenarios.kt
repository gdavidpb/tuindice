package com.gdavidpb.tuindice.scenarios.catalog

import com.gdavidpb.tuindice.about.ui.AboutUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.StepBuilder
import com.gdavidpb.tuindice.scenariokit.dsl.foreground
import com.gdavidpb.tuindice.scenariokit.dsl.ifVisible
import com.gdavidpb.tuindice.scenariokit.dsl.onPlatform
import com.gdavidpb.tuindice.scenariokit.dsl.scenario
import com.gdavidpb.tuindice.scenariokit.dsl.scrollUntilVisible
import com.gdavidpb.tuindice.scenariokit.dsl.system
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.dsl.waitVisible
import com.gdavidpb.tuindice.scenariokit.model.Platform
import com.gdavidpb.tuindice.scenariokit.model.Scenario
import com.gdavidpb.tuindice.scenariokit.model.Scroll
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts
import com.gdavidpb.tuindice.scenarios.fixture.Start
import com.gdavidpb.tuindice.scenarios.shared.Within
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.ui.MaincoreUiTags

/** The label of the iOS popover's dimmed area, which closes a share sheet. */
private const val POPOVER_DISMISS_REGION = "PopoverDismissRegion"

/** From the seeded summary: opens the About tab. */
private fun StepBuilder.openAbout() {
	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	tap(MaincoreUiTags.TuIndiceBottomBarAboutItem)
	waitVisible(AboutUiTags.ContentContainer, Within.Wait)
}

/** Opens an in-app browser page from the About list and comes back with the top bar's back button. */
private fun StepBuilder.openBrowserPageAndReturn(link: String) {
	tap(link)
	waitVisible(MaincoreUiTags.BrowserContainer, Within.Wait)
	tap(MaincoreUiTags.TuIndiceTopBarBackButton)
	waitVisible(AboutUiTags.ContentContainer, Within.Action)
}

/** Back from another app: foreground the app and select the About tab again. */
private fun StepBuilder.returnToAbout() {
	tap(MaincoreUiTags.TuIndiceBottomBarAboutItem)
	waitVisible(AboutUiTags.ContentContainer, Within.Action)
}

/** Scrolls to a link that hands off to another app, taps it, and comes back. */
private fun StepBuilder.openExternalLinkAndReturn(link: String) {
	scrollUntilVisible(link, Scroll.ContentDown, Within.Action)
	tap(link)
	foreground()
	returnToAbout()
}

/** Scrolls to a trigger that may open a page of the in-app browser, taps it, and comes back. */
private fun StepBuilder.openPlatformEdgeTriggerAndReturn(trigger: String) {
	scrollUntilVisible(trigger, Scroll.ContentDown, Within.Action)
	tap(trigger)
	foreground()
	ifVisible(MaincoreUiTags.BrowserContainer) {
		tap(MaincoreUiTags.TuIndiceTopBarBackButton)
	}
	returnToAbout()
}

private val aboutSmoke = scenario(
	"about-smoke",
	"about",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	tags("smoke")
	covers("about.About.OpenPrivacyPolicy")
	account(E2eAccounts.Canonical.id)

	openAbout()
	waitVisible(AboutUiTags.OpenPrivacy, Within.Assert)
	openBrowserPageAndReturn(AboutUiTags.OpenPrivacy)
}

private val aboutInternalBrowserLinks = scenario(
	"about-internal-browser-links",
	"about",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers(
		"about.About.OpenTermsAndConditions",
		"about.About.OpenPrivacyPolicy",
		"about.About.OpenSupport"
	)
	account(E2eAccounts.Canonical.id)

	openAbout()
	openBrowserPageAndReturn(AboutUiTags.OpenTerms)
	openBrowserPageAndReturn(AboutUiTags.OpenPrivacy)
	openBrowserPageAndReturn(AboutUiTags.OpenSupport)
}

private val aboutExternalUrlLinks = scenario(
	"about-external-url-links",
	"about",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers("about.About.OpenUrl")
	account(E2eAccounts.Canonical.id)

	openAbout()
	tap(AboutUiTags.OpenCreativeCommons)
	foreground()
	returnToAbout()
	openExternalLinkAndReturn(AboutUiTags.OpenX)
	openExternalLinkAndReturn(AboutUiTags.OpenGithub)
	openExternalLinkAndReturn(AboutUiTags.OpenKotlin)
	openExternalLinkAndReturn(AboutUiTags.OpenDst)
}

/**
 * Every trigger that hands off to the system (share sheet, store, mail, bug report) opens its hand-off;
 * the scenario only proves the trigger fires and the app is usable when it comes back.
 */
private val aboutPlatformEdgeTriggers = scenario(
	"about-platform-edge-triggers",
	"about",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers(
		"about.About.ShareApp",
		"about.About.RateOnStore",
		"about.About.ContactDeveloper",
		"about.About.ReportBug"
	)
	account(E2eAccounts.Canonical.id)

	openAbout()
	scrollUntilVisible(AboutUiTags.ShareApp, Scroll.ContentDown, Within.Action)
	tap(AboutUiTags.ShareApp)
	// Android's chooser has no cancel or close button (it goes when the app is brought back); iOS's sheet is
	// closed by the dimmed area around it, whatever the language of the device.
	onPlatform(Platform.Ios) {
		tap(system(POPOVER_DISMISS_REGION))
	}
	foreground()
	returnToAbout()
	openPlatformEdgeTriggerAndReturn(AboutUiTags.RateOnStore)
	openPlatformEdgeTriggerAndReturn(AboutUiTags.ContactDeveloper)
	openPlatformEdgeTriggerAndReturn(AboutUiTags.ReportBug)
}

private val aboutUsageDataConsent = scenario(
	"about-usage-data-consent",
	"about",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers("about.About.SetUsageDataCollectionEnabled")
	account(E2eAccounts.Canonical.id)

	openAbout()
	scrollUntilVisible(AboutUiTags.RateOnStore, Scroll.ContentDown, Within.Action)
	waitVisible(AboutUiTags.RateOnStore, Within.Assert)
	scrollUntilVisible(AboutUiTags.UsageDataConsentToggle, Scroll.ContentDown, Within.Action)
	waitVisible(AboutUiTags.UsageDataConsentToggle, Within.Assert)
	tap(AboutUiTags.UsageDataConsentToggle)
	waitVisible(AboutUiTags.ContentContainer, Within.Assert)
	tap(AboutUiTags.UsageDataConsentToggle)
	waitVisible(AboutUiTags.ContentContainer, Within.Assert)
}

/** The scenarios of the `about` module; list every new one here. */
val aboutScenarios: List<Scenario> = listOf(
	aboutSmoke,
	aboutInternalBrowserLinks,
	aboutExternalUrlLinks,
	aboutPlatformEdgeTriggers,
	aboutUsageDataConsent
)
