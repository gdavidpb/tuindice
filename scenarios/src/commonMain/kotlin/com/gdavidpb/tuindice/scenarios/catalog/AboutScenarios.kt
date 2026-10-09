package com.gdavidpb.tuindice.scenarios.catalog

import com.gdavidpb.tuindice.about.ui.AboutUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.StepBuilder
import com.gdavidpb.tuindice.scenariokit.dsl.assertChecked
import com.gdavidpb.tuindice.scenariokit.dsl.foreground
import com.gdavidpb.tuindice.scenariokit.dsl.onPlatform
import com.gdavidpb.tuindice.scenariokit.dsl.scenario
import com.gdavidpb.tuindice.scenariokit.dsl.scrollUntilVisible
import com.gdavidpb.tuindice.scenariokit.dsl.system
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.dsl.tapAtScreen
import com.gdavidpb.tuindice.scenariokit.dsl.waitBackgrounded
import com.gdavidpb.tuindice.scenariokit.dsl.waitGone
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

/** Where the top bar of the About screen has nothing to tap: right of its title (fractions of the screen). */
private const val SHEET_DISMISS_X = 0.6
private const val SHEET_DISMISS_Y = 0.1

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
	waitBackgrounded()
	foreground()
	returnToAbout()
}

/**
 * Scrolls to a trigger that hands off to the system (store, mail, bug report), taps it, and comes back. On Android the
 * triggers measured to take the app out of the foreground every time ([leavesTheAppOnAndroid]) wait for that first.
 * No trigger leaves the app on the iOS simulator, so nothing is asserted there
 * (e2e/platform/ios/about-platform-edge-triggers.md).
 */
private fun StepBuilder.openPlatformEdgeTriggerAndReturn(trigger: String, leavesTheAppOnAndroid: Boolean) {
	scrollUntilVisible(trigger, Scroll.ContentDown, Within.Action)
	tap(trigger)
	if (leavesTheAppOnAndroid) {
		onPlatform(Platform.Android) {
			waitBackgrounded()
		}
	}
	foreground()
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
	waitBackgrounded()
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
	foreground()
	// Android's chooser has no cancel or close button and goes when the app is brought back. iOS's share sheet has
	// none either and is closed by tapping the dimmed area around it, but that tap also reaches whatever the app
	// shows under the finger (measured: a tap at the height of the Creative Commons row closed the sheet and opened
	// that link in Safari). So the scenario taps where the app has nothing to activate, the empty part of the top
	// bar, and asserts that the sheet went and the About screen is still the one in front.
	onPlatform(Platform.Ios) {
		tapAtScreen(SHEET_DISMISS_X, SHEET_DISMISS_Y)
		waitGone(system(POPOVER_DISMISS_REGION), Within.Action)
		waitVisible(AboutUiTags.ContentContainer, Within.Assert)
		waitGone(MaincoreUiTags.BrowserContainer, Within.Assert)
	}
	returnToAbout()
	openPlatformEdgeTriggerAndReturn(AboutUiTags.RateOnStore, leavesTheAppOnAndroid = true)
	openPlatformEdgeTriggerAndReturn(AboutUiTags.ContactDeveloper, leavesTheAppOnAndroid = true)
	// Not asserted: Gmail's compose screen, with no account in the emulator, closes itself within ~100 ms of opening
	// (measured 0 of 10 runs in which the app was seen leaving), so the app is already back when the wait looks.
	openPlatformEdgeTriggerAndReturn(AboutUiTags.ReportBug, leavesTheAppOnAndroid = false)
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
	assertChecked(AboutUiTags.UsageDataConsentToggle, false)
	tap(AboutUiTags.UsageDataConsentToggle)
	assertChecked(AboutUiTags.UsageDataConsentToggle, true)
	waitVisible(AboutUiTags.ContentContainer, Within.Assert)
	tap(AboutUiTags.UsageDataConsentToggle)
	assertChecked(AboutUiTags.UsageDataConsentToggle, false)
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
