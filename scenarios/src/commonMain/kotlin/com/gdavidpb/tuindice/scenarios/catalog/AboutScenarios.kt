package com.gdavidpb.tuindice.scenarios.catalog

import com.gdavidpb.tuindice.about.ui.AboutUiTags
import com.gdavidpb.tuindice.about.ui.SettingsUiTags
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.StepBuilder
import com.gdavidpb.tuindice.scenariokit.dsl.SwipeDirection
import com.gdavidpb.tuindice.scenariokit.dsl.assertChecked
import com.gdavidpb.tuindice.scenariokit.dsl.expectRequest
import com.gdavidpb.tuindice.scenariokit.dsl.foreground
import com.gdavidpb.tuindice.scenariokit.dsl.mockState
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
import com.gdavidpb.tuindice.scenarios.shared.closeSettings
import com.gdavidpb.tuindice.scenarios.shared.openAboutFromSettings
import com.gdavidpb.tuindice.scenarios.shared.openSettings
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.ui.MaincoreUiTags

/** From the seeded summary: opens Settings through the avatar of the top bar. */
private fun StepBuilder.openSettingsFromSummary() {
	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	openSettings()
}

/** Opens an in-app browser page from the Settings list and comes back with the top bar's back button. */
private fun StepBuilder.openBrowserPageAndReturn(link: String) {
	tap(link)
	waitVisible(MaincoreUiTags.BrowserContainer, Within.Wait)
	tap(MaincoreUiTags.TuIndiceTopBarBackButton)
	waitVisible(SettingsUiTags.ContentContainer, Within.Action)
}

/** Back from another app: the app comes to the front on the screen it left, and answers. */
private fun StepBuilder.returnToSettings() {
	waitVisible(SettingsUiTags.ContentContainer, Within.Action)
}

/** Back from another app, on the About screen. */
private fun StepBuilder.returnToAbout() {
	waitVisible(AboutUiTags.ContentContainer, Within.Action)
}

/** Scrolls to a link of About that hands off to another app, taps it, and comes back. */
private fun StepBuilder.openExternalLinkAndReturn(link: String) {
	scrollUntilVisible(link, Scroll.ContentDown, Within.Action)
	tap(link)
	waitBackgrounded()
	foreground()
	returnToAbout()
}

/**
 * Scrolls to a trigger of Settings that hands off to the system (store, bug report), taps it, and comes back. On
 * Android the triggers measured to take the app out of the foreground every time ([leavesTheAppOnAndroid]) wait for
 * that first. No trigger leaves the app on the iOS simulator, so nothing is asserted there
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
	returnToSettings()
}

private val aboutSmoke = scenario(
	"about-smoke",
	"about",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	tags("smoke")
	covers("about.Settings.OpenPrivacyPolicy")
	account(E2eAccounts.Canonical.id)

	openSettingsFromSummary()
	waitVisible(SettingsUiTags.OpenPrivacy, Within.Assert)
	openBrowserPageAndReturn(SettingsUiTags.OpenPrivacy)
}

private val aboutInternalBrowserLinks = scenario(
	"about-internal-browser-links",
	"about",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers(
		"about.Settings.OpenTermsAndConditions",
		"about.Settings.OpenPrivacyPolicy",
		"about.Settings.OpenSupport"
	)
	account(E2eAccounts.Canonical.id)

	openSettingsFromSummary()
	// The terms page the mock serves is taller than a screen (`mocks/__files/e2e/terms.html`): its last line is not on
	// screen when the page loads and comes into view only when the web view scrolls, which is what keeps the web view
	// scrolling covered on both platforms.
	tap(SettingsUiTags.OpenTerms)
	waitVisible(MaincoreUiTags.BrowserContainer, Within.Wait)
	waitVisible(text(Copy.TermsPageTitle), Within.Wait)
	waitGone(text(Copy.TermsPageEnd), Within.Assert)
	swipeScreen(SwipeDirection.Up)
	waitVisible(text(Copy.TermsPageEnd), Within.Action)
	tap(MaincoreUiTags.TuIndiceTopBarBackButton)
	waitVisible(SettingsUiTags.ContentContainer, Within.Action)
	openBrowserPageAndReturn(SettingsUiTags.OpenPrivacy)
	openBrowserPageAndReturn(SettingsUiTags.OpenSupport)
}

private val aboutExternalUrlLinks = scenario(
	"about-external-url-links",
	"about",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers("about.Settings.OpenUrl", "about.About.OpenUrl")
	account(E2eAccounts.Canonical.id)

	openSettingsFromSummary()
	scrollUntilVisible(SettingsUiTags.OpenX, Scroll.ContentDown, Within.Action)
	tap(SettingsUiTags.OpenX)
	waitBackgrounded()
	foreground()
	returnToSettings()
	openAboutFromSettings()
	tap(AboutUiTags.OpenCreativeCommons)
	waitBackgrounded()
	foreground()
	returnToAbout()
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
		"about.Settings.ShareApp",
		"about.Settings.RateOnStore"
	)
	account(E2eAccounts.Canonical.id)

	openSettingsFromSummary()
	// Not asserted, and so not in `covers` but a platform edge (ActionDispositions): Gmail's compose screen, with no
	// account in the emulator, closes itself within ~100 ms of opening (measured 0 of 10 runs on Android in which the
	// app was seen leaving), and on iOS the app stays in front 3 of 3 times (docs/e2e-mediciones.md, section 3).
	openPlatformEdgeTriggerAndReturn(SettingsUiTags.ReportBug, leavesTheAppOnAndroid = false)
	scrollUntilVisible(SettingsUiTags.ShareApp, Scroll.ContentDown, Within.Action)
	tap(SettingsUiTags.ShareApp)
	// The sheet must have appeared before it is closed (YE-1): without this a share that opens nothing would pass,
	// because the touch that closes it and the waits after it are already true when there is no sheet.
	onPlatform(Platform.Android) {
		waitVisible(system(ShareSheet.ANDROID_ELEMENT), Within.Action)
		// The chooser is in front of the app: the exit is announced before the app is brought back.
		waitBackgrounded()
	}
	foreground()
	// Android's chooser has no cancel or close button and goes when the app is brought back. iOS's share sheet has
	// none either and is closed by tapping the dimmed area around it, over the middle of the top bar of Settings, where
	// no row lies: the tap must close the sheet and open nothing, so the scenario asserts that the sheet went, that
	// Settings is still the screen in front (no browser page opened, the app did not go to Safari), and the steps after
	// it tap the app and need it to answer.
	onPlatform(Platform.Ios) {
		waitVisible(system(ShareSheet.IOS_ELEMENT), Within.Action)
		tapAtScreen(ShareSheet.SETTINGS_DISMISS_X, ShareSheet.SETTINGS_DISMISS_Y)
		waitGone(system(ShareSheet.IOS_ELEMENT), Within.Action)
		waitVisible(SettingsUiTags.ContentContainer, Within.Assert)
		waitGone(MaincoreUiTags.BrowserContainer, Within.Assert)
		// The app answers the next touch (the sheet's closing left it taking touches again): back to the summary.
		closeSettings()
		waitVisible(SummaryUiTags.ContentContainer, Within.Action)
		openSettings()
	}
	returnToSettings()
	openPlatformEdgeTriggerAndReturn(SettingsUiTags.RateOnStore, leavesTheAppOnAndroid = true)
	// The developer's contact lives in About now. Like the bug report, it opens the mail app, which on Android closes
	// itself at once (see ReportBug above), so it is not asserted but a platform edge (ActionDispositions): the scenario
	// taps it and checks that About answers when the app is back.
	openAboutFromSettings()
	scrollUntilVisible(AboutUiTags.ContactDeveloper, Scroll.ContentDown, Within.Action)
	tap(AboutUiTags.ContactDeveloper)
	foreground()
	returnToAbout()
}

private val aboutUsageDataConsent = scenario(
	"about-usage-data-consent",
	"about",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers("about.Settings.SetUsageDataCollectionEnabled")
	account(E2eAccounts.Canonical.id)

	openSettingsFromSummary()
	scrollUntilVisible(SettingsUiTags.UsageDataConsentToggle, Scroll.ContentDown, Within.Action)
	waitVisible(SettingsUiTags.UsageDataConsentToggle, Within.Assert)
	assertChecked(SettingsUiTags.UsageDataConsentToggle, false)
	tap(SettingsUiTags.UsageDataConsentToggle)
	assertChecked(SettingsUiTags.UsageDataConsentToggle, true)
	waitVisible(SettingsUiTags.ContentContainer, Within.Assert)
	tap(SettingsUiTags.UsageDataConsentToggle)
	assertChecked(SettingsUiTags.UsageDataConsentToggle, false)
	waitVisible(SettingsUiTags.ContentContainer, Within.Assert)
	scrollUntilVisible(SettingsUiTags.RateOnStore, Scroll.ContentDown, Within.Action)
	waitVisible(SettingsUiTags.RateOnStore, Within.Assert)
}

/** From Settings: scrolls to the assistant's memory entry, opens it and waits for its screen. */
private fun StepBuilder.openAssistantMemory() {
	scrollUntilVisible(SettingsUiTags.AssistantMemoryItem, Scroll.ContentDown, Within.Action)
	tap(SettingsUiTags.AssistantMemoryItem)
	waitVisible(AssistantUiTags.MemoryScreen, Within.Wait)
}

private val aboutAssistantConsentToggle = scenario(
	"about-assistant-consent-toggle",
	"about",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers("about.Settings.SetAssistantConsent")
	account(E2eAccounts.Canonical.id)

	openSettingsFromSummary()
	scrollUntilVisible(SettingsUiTags.AssistantConsentSwitch, Scroll.ContentDown, Within.Action)
	waitVisible(SettingsUiTags.AssistantConsentSwitch, Within.Assert)
	assertChecked(SettingsUiTags.AssistantConsentSwitch, false)
	tap(SettingsUiTags.AssistantConsentSwitch)
	assertChecked(SettingsUiTags.AssistantConsentSwitch, true)
	expectRequest("PUT", "/assistant/v1/consent", status = 200)
	tap(SettingsUiTags.AssistantConsentSwitch)
	assertChecked(SettingsUiTags.AssistantConsentSwitch, false)
	expectRequest("PUT", "/assistant/v1/consent", status = 200, atLeast = 2)
}

private val aboutAssistantMemoryDelete = scenario(
	"about-assistant-memory-delete",
	"about",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers(
		"about.Settings.OpenAssistantMemory",
		"assistant.AssistantMemory.DeleteEntry"
	)
	account(E2eAccounts.Canonical.id)

	openSettingsFromSummary()
	openAssistantMemory()
	waitVisible(AssistantUiTags.Memory.entry("mem-1"), Within.Assert)
	tap(AssistantUiTags.Memory.delete("mem-1"))
	waitGone(AssistantUiTags.Memory.entry("mem-1"), Within.Action)
	waitVisible(AssistantUiTags.Memory.entry("mem-2"), Within.Assert)
}

// A memory waiting for the student's answer shows «¿Sigue aplicando?» in the list; answering it saves the answer
// and loads the list again, where the memory no longer asks.
private val aboutAssistantMemoryStillApplies = scenario(
	"about-assistant-memory-still-applies",
	"about",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers("assistant.AssistantMemory.AnswerStillApplies")
	account(E2eAccounts.Canonical.id)

	openSettingsFromSummary()
	openAssistantMemory()
	waitVisible(AssistantUiTags.Memory.stillAppliesYes("mem-2"), Within.Assert)
	tap(AssistantUiTags.Memory.stillAppliesYes("mem-2"))
	waitGone(AssistantUiTags.Memory.stillAppliesYes("mem-2"), Within.Action)
	waitVisible(AssistantUiTags.Memory.entry("mem-2"), Within.Action)
	waitVisible(AssistantUiTags.Memory.entry("mem-1"), Within.Assert)
}

private val aboutAssistantMemoryClear = scenario(
	"about-assistant-memory-clear",
	"about",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers(
		"assistant.AssistantMemory.ClearAll",
		"assistant.AssistantMemory.ConfirmClearAll"
	)
	account(E2eAccounts.Canonical.id)

	openSettingsFromSummary()
	openAssistantMemory()
	tap(AssistantUiTags.MemoryClearAll)
	waitVisible(AssistantUiTags.ClearAllDialog, Within.Assert)
	tap(AssistantUiTags.ClearAllConfirm)
	waitVisible(AssistantUiTags.MemoryEmpty, Within.Action)
}

private val aboutAssistantMemoryRetry = scenario(
	"about-assistant-memory-retry",
	"about",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers("assistant.AssistantMemory.Retry")
	account(E2eAccounts.Canonical.id)

	mockState("assistant-memories", "Failing")
	openSettingsFromSummary()
	scrollUntilVisible(SettingsUiTags.AssistantMemoryItem, Scroll.ContentDown, Within.Action)
	tap(SettingsUiTags.AssistantMemoryItem)
	waitVisible(AssistantUiTags.MemoryFailed, Within.Wait)
	mockState("assistant-memories", "Recovered")
	tap(BaseUiTags.ErrorViewRetryButton)
	waitVisible(AssistantUiTags.MemoryContent, Within.Wait)
}

/** From Settings: scrolls to the assistant's conversations row, asks for the deletion and confirms it. */
private val aboutAssistantConversationsDelete = scenario(
	"about-assistant-conversations-delete",
	"about",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers(
		"about.Settings.RequestDeleteAssistantConversations",
		"about.Settings.ConfirmDeleteAssistantConversations"
	)
	account(E2eAccounts.Canonical.id)

	openSettingsFromSummary()
	scrollUntilVisible(SettingsUiTags.AssistantConversationsItem, Scroll.ContentDown, Within.Action)
	tap(SettingsUiTags.AssistantConversationsItem)
	waitVisible(SettingsUiTags.DeleteAssistantConversationsDialog, Within.Assert)
	tap(SettingsUiTags.DeleteAssistantConversationsConfirm)
	waitVisible(BaseUiTags.SnackbarContainer, Within.Action)
	waitVisible(text(Copy.AssistantConversationsDeleted), Within.Action)
}

/**
 * Settings opens from the avatar with the card of the student on top (name, career and carnet, which only show who
 * is signed in), and «Acerca de TuIndice» opens from its list: the credits are there, and back returns to Settings.
 */
private val aboutSettingsIdentityAndAbout = scenario(
	"about-settings-identity-and-about",
	"about",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers("about.Settings.OpenAbout")
	account(E2eAccounts.Canonical.id)

	openSettingsFromSummary()
	waitVisible(SettingsUiTags.IdentityCard, Within.Action)
	waitVisible(SettingsUiTags.IdentityName, Within.Assert)
	waitVisible(SettingsUiTags.IdentityCareer, Within.Assert)
	waitVisible(SettingsUiTags.IdentityCarnet, Within.Assert)
	openAboutFromSettings()
	waitVisible(AboutUiTags.OpenCreativeCommons, Within.Assert)
	waitVisible(AboutUiTags.OpenGithub, Within.Assert)
	// Back from About is Settings, and back from Settings is the section it was opened over.
	closeSettings()
	waitVisible(SettingsUiTags.ContentContainer, Within.Action)
	closeSettings()
	waitVisible(SummaryUiTags.ContentContainer, Within.Action)
}

/** The scenarios of the `about` module; list every new one here. */
val aboutScenarios: List<Scenario> = listOf(
	aboutSmoke,
	aboutInternalBrowserLinks,
	aboutExternalUrlLinks,
	aboutPlatformEdgeTriggers,
	aboutUsageDataConsent,
	aboutAssistantConsentToggle,
	aboutAssistantMemoryDelete,
	aboutAssistantMemoryClear,
	aboutAssistantMemoryRetry,
	aboutAssistantMemoryStillApplies,
	aboutAssistantConversationsDelete,
	aboutSettingsIdentityAndAbout
)
