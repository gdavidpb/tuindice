package com.gdavidpb.tuindice.scenarios.catalog

import com.gdavidpb.tuindice.about.ui.AboutUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.StepBuilder
import com.gdavidpb.tuindice.scenariokit.dsl.SwipeDirection
import com.gdavidpb.tuindice.scenariokit.dsl.assertChecked
import com.gdavidpb.tuindice.scenariokit.dsl.foreground
import com.gdavidpb.tuindice.scenariokit.dsl.onPlatform
import com.gdavidpb.tuindice.scenariokit.dsl.scenario
import com.gdavidpb.tuindice.scenariokit.dsl.scrollUntilVisible
import com.gdavidpb.tuindice.scenariokit.dsl.swipeScreen
import com.gdavidpb.tuindice.scenariokit.dsl.system
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.dsl.tapAtScreen
import com.gdavidpb.tuindice.scenariokit.dsl.text
import com.gdavidpb.tuindice.scenariokit.dsl.waitBackgrounded
import com.gdavidpb.tuindice.scenariokit.dsl.waitGone
import com.gdavidpb.tuindice.scenariokit.dsl.waitVisible
import com.gdavidpb.tuindice.scenariokit.model.Platform
import com.gdavidpb.tuindice.scenariokit.model.Scenario
import com.gdavidpb.tuindice.scenariokit.model.Scroll
import com.gdavidpb.tuindice.scenarios.fixture.Copy
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts
import com.gdavidpb.tuindice.scenarios.fixture.Start
import com.gdavidpb.tuindice.scenarios.shared.ShareSheet
import com.gdavidpb.tuindice.scenarios.shared.Within
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.ui.MaincoreUiTags

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
	// The terms page the mock serves is taller than a screen (`mocks/__files/e2e/terms.html`): its last line is not on
	// screen when the page loads and comes into view only when the web view scrolls, which is what keeps the web view
	// scrolling covered on both platforms.
	tap(AboutUiTags.OpenTerms)
	waitVisible(MaincoreUiTags.BrowserContainer, Within.Wait)
	waitVisible(text(Copy.TermsPageTitle), Within.Wait)
	waitGone(text(Copy.TermsPageEnd), Within.Assert)
	swipeScreen(SwipeDirection.Up)
	waitVisible(text(Copy.TermsPageEnd), Within.Action)
	tap(MaincoreUiTags.TuIndiceTopBarBackButton)
	waitVisible(AboutUiTags.ContentContainer, Within.Action)
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
		"about.About.ContactDeveloper"
	)
	account(E2eAccounts.Canonical.id)

	openAbout()
	scrollUntilVisible(AboutUiTags.ShareApp, Scroll.ContentDown, Within.Action)
	tap(AboutUiTags.ShareApp)
	// The sheet must have appeared before it is closed (YE-1): without this a share that opens nothing would pass,
	// because the touch that closes it and the waits after it are already true when there is no sheet.
	onPlatform(Platform.Android) {
		waitVisible(system(ShareSheet.ANDROID_ELEMENT), Within.Action)
	}
	foreground()
	// Android's chooser has no cancel or close button and goes when the app is brought back. iOS's share sheet has
	// none either and is closed by tapping the dimmed area around it, but that tap also reaches whatever the app
	// shows under the finger (measured: a tap at the height of the Creative Commons row closed the sheet and opened
	// that link in Safari). So the scenario sees the sheet first, then taps where the app has nothing to activate,
	// the empty part of the top bar, and asserts that the sheet went and the About screen is still the one in front.
	onPlatform(Platform.Ios) {
		waitVisible(system(ShareSheet.IOS_ELEMENT), Within.Action)
		tapAtScreen(ShareSheet.DISMISS_X, ShareSheet.DISMISS_Y)
		waitGone(system(ShareSheet.IOS_ELEMENT), Within.Action)
		waitVisible(AboutUiTags.ContentContainer, Within.Assert)
		waitGone(MaincoreUiTags.BrowserContainer, Within.Assert)
	}
	returnToAbout()
	openPlatformEdgeTriggerAndReturn(AboutUiTags.RateOnStore, leavesTheAppOnAndroid = true)
	openPlatformEdgeTriggerAndReturn(AboutUiTags.ContactDeveloper, leavesTheAppOnAndroid = true)
	// Not asserted, and so not in `covers` but a platform edge (ActionDispositions): Gmail's compose screen, with no
	// account in the emulator, closes itself within ~100 ms of opening (measured 0 of 10 runs on Android in which the
	// app was seen leaving), and on iOS the app stays in front 3 of 3 times (docs/e2e-mediciones.md, section 3).
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
