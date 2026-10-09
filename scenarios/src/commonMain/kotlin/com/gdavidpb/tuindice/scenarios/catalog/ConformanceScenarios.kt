package com.gdavidpb.tuindice.scenarios.catalog

import com.gdavidpb.tuindice.about.ui.AboutUiTags
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.StepBuilder
import com.gdavidpb.tuindice.scenariokit.dsl.SwipeDirection
import com.gdavidpb.tuindice.scenariokit.dsl.assertChecked
import com.gdavidpb.tuindice.scenariokit.dsl.assertEnabled
import com.gdavidpb.tuindice.scenariokit.dsl.back
import com.gdavidpb.tuindice.scenariokit.dsl.doubleTap
import com.gdavidpb.tuindice.scenariokit.dsl.enterSecureText
import com.gdavidpb.tuindice.scenariokit.dsl.enterText
import com.gdavidpb.tuindice.scenariokit.dsl.expectRequest
import com.gdavidpb.tuindice.scenariokit.dsl.foreground
import com.gdavidpb.tuindice.scenariokit.dsl.mockState
import com.gdavidpb.tuindice.scenariokit.dsl.onPlatform
import com.gdavidpb.tuindice.scenariokit.dsl.relaunch
import com.gdavidpb.tuindice.scenariokit.dsl.scenario
import com.gdavidpb.tuindice.scenariokit.dsl.scrollUntilVisible
import com.gdavidpb.tuindice.scenariokit.dsl.submitTextEntry
import com.gdavidpb.tuindice.scenariokit.dsl.swipeFrom
import com.gdavidpb.tuindice.scenariokit.dsl.swipeScreen
import com.gdavidpb.tuindice.scenariokit.dsl.system
import com.gdavidpb.tuindice.scenariokit.dsl.tag
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.dsl.tapAt
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
import com.gdavidpb.tuindice.scenarios.fixture.E2eFixtures
import com.gdavidpb.tuindice.scenarios.fixture.E2eInputs
import com.gdavidpb.tuindice.scenarios.fixture.Start
import com.gdavidpb.tuindice.scenarios.shared.Within
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.ui.MaincoreUiTags
import com.gdavidpb.tuindice.wizard.ui.CoachmarkUiTags
import kotlin.time.Duration.Companion.milliseconds

private const val SWIPE_MS = 600L

/** An element of the system's share surface on each platform. */
private const val ANDROID_SHARE_ELEMENT = "com.android.intentresolver:id/chooser_container"
private const val IOS_SHARE_ELEMENT = "PopoverDismissRegion"

/** Where the top bar of About has nothing to tap (fractions of the screen), over the dimmed area of the iOS sheet. */
private const val SHEET_DISMISS_X = 0.6
private const val SHEET_DISMISS_Y = 0.1

// The centre of the last of the five items of the bottom bar, as fractions of the screen.
private const val BOTTOM_BAR_LAST_ITEM_X = 0.9
private const val BOTTOM_BAR_Y = 0.96

// Two texts of the same length that share no letter, so a character left behind shows in the read-back.
private const val SEARCH_BEFORE = "pr"
private const val SEARCH_AFTER = "ma"

// The usage-data switch of About before it is tapped: the app starts with the consent off.
private const val CONSENT_BEFORE = false

private fun canonical() = Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()

private fun clean() = Start.Clean().toLaunchSpec()

private val canonicalAccount = E2eAccounts.Canonical

/** From the seeded summary to the tab of [item], waiting for its [content]. */
private fun StepBuilder.openTab(item: String, content: String) {
	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	tap(item)
	waitVisible(content, Within.Long)
}

/** Types the canonical USB id, which the field masks, so the sign-in button still waits for a password. */
private fun StepBuilder.typeUsbId() {
	tap(AuthUiTags.UsbIdTextField)
	enterText(AuthUiTags.UsbIdTextField, canonicalAccount.usbIdDigits, expect = canonicalAccount.usbIdFormatted)
}

/** Types the canonical password and puts the keyboard away, so the sign-in button is on screen. */
private fun StepBuilder.typePassword() {
	tap(AuthUiTags.PasswordTextField)
	enterSecureText(AuthUiTags.PasswordTextField, canonicalAccount.password)
	tap(AuthUiTags.KeyboardDismissArea)
}

/** Launch: the seeded session lands on the summary with no coachmark; [conformanceLaunchClean] needs it run first. */
private val conformanceLaunchSeeded = scenario("conformance-launch-seeded", "conformance", canonical()) {
	account(canonicalAccount.id)

	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	waitGone(CoachmarkUiTags.Bubble, Within.Assert)
}

/** Launch: after a seeded launch, the harness resets the app and a clean launch lands on the sign-in screen. */
private val conformanceLaunchClean = scenario("conformance-launch-clean", "conformance", clean()) {
	waitVisible(AuthUiTags.SignInIdleContainer, Within.Sync)
}

/** Typing and re-reading: the masked USB id field must read back the formatted digits. */
private val conformanceTypeReadback = scenario("conformance-type-readback", "conformance", clean()) {
	waitVisible(AuthUiTags.SignInIdleContainer, Within.Sync)
	typeUsbId()
}

/** Replacing text: the email field is typed, then its whole content is replaced. */
private val conformanceTypeReplace = scenario("conformance-type-replace", "conformance", clean()) {
	waitVisible(AuthUiTags.SignInIdleContainer, Within.Sync)
	tap(AuthUiTags.IdentifierModeToggle)
	waitVisible(text(Copy.UsbEmailHint), Within.Assert)
	tap(AuthUiTags.UsbIdTextField)
	enterText(AuthUiTags.UsbIdTextField, E2eAccounts.CanonicalEmail.usbIdDigits)
	enterText(AuthUiTags.UsbIdTextField, "mail", replace = true)
}

/**
 * Replacing text in a field that took its focus back on returning: the record search field is typed, its
 * result opens a screen, and the way back leaves the field focused. Its content is replaced without the
 * keyboard having been put away first.
 */
private val conformanceTypeReplaceAfterBack = scenario(
	"conformance-type-replace-after-back",
	"conformance",
	canonical()
) {
	account(canonicalAccount.id)

	val subject = E2eFixtures.SubjectEp1308.value

	openTab(MaincoreUiTags.TuIndiceBottomBarRecordItem, RecordUiTags.ContentContainer)
	tap(RecordUiTags.CreateSyntheticTermFab)
	waitVisible(RecordUiTags.CreateSyntheticTermScreen, Within.Action)
	tap(RecordUiTags.CreateSyntheticTermSearchTab)
	waitVisible(RecordUiTags.CreateSyntheticTermSearchField, Within.Action)
	tap(RecordUiTags.CreateSyntheticTermSearchField)
	enterText(RecordUiTags.CreateSyntheticTermSearchField, SEARCH_BEFORE, replace = true)
	submitTextEntry()
	waitVisible(RecordUiTags.createSyntheticTermSubjectStatsButton(subject), Within.Wait)
	tap(RecordUiTags.createSyntheticTermSubjectStatsButton(subject))
	waitVisible(SubjectsUiTags.Content, Within.Long)
	tap(MaincoreUiTags.TuIndiceTopBarBackButton)
	waitVisible(RecordUiTags.CreateSyntheticTermSearchField, Within.Action)
	tap(RecordUiTags.CreateSyntheticTermSearchField)
	enterText(RecordUiTags.CreateSyntheticTermSearchField, SEARCH_AFTER, replace = true)
}

/** A secure field cannot be read back; typing in it is proven by what it enables. */
private val conformanceSecureField = scenario("conformance-secure-field", "conformance", clean()) {
	waitVisible(AuthUiTags.SignInIdleContainer, Within.Sync)
	typeUsbId()
	tap(AuthUiTags.KeyboardDismissArea)
	assertEnabled(AuthUiTags.SignInButton, false)
	typePassword()
	assertEnabled(AuthUiTags.SignInButton, true)
}

/** Enabled state: the sign-in button is disabled while the form is empty and enabled once it is filled. */
private val conformanceEnabled = scenario("conformance-enabled", "conformance", clean()) {
	waitVisible(AuthUiTags.SignInIdleContainer, Within.Sync)
	assertEnabled(AuthUiTags.SignInButton, false)
	typePassword()
	typeUsbId()
	tap(AuthUiTags.KeyboardDismissArea)
	assertEnabled(AuthUiTags.SignInButton, true)
}

/** Finding an element by its text instead of a tag. */
private val conformanceTextQuery = scenario("conformance-text-query", "conformance", clean()) {
	waitVisible(AuthUiTags.SignInIdleContainer, Within.Sync)
	tap(AuthUiTags.IdentifierModeToggle)
	waitVisible(text(Copy.UsbEmailHint), Within.Assert)
}

/** Submitting a search: the text is typed, the action of the search keyboard is sent and the results show. */
private val conformanceSubmitSearch = scenario("conformance-submit-search", "conformance", canonical()) {
	account(canonicalAccount.id)

	openTab(MaincoreUiTags.TuIndiceBottomBarPensumItem, PensumUiTags.PensumScreen)
	tap(BaseUiTags.topBarActionButton(TopBarAction.SearchPensumAction))
	waitVisible(SubjectsUiTags.SearchScreen, Within.Action)
	tap(SubjectsUiTags.SearchTextField)
	enterText(SubjectsUiTags.SearchTextField, "ci")
	submitTextEntry()
	waitVisible(SubjectsUiTags.searchResult(E2eFixtures.SubjectCi2511.value), Within.Wait)
}

/**
 * Sending the IME action right after a touch that opens a screen which focuses its own field: the keyboard is not up
 * yet when the step starts, so the driver waits for it (and for the focus) instead of answering false.
 */
private val conformanceSubmitAfterOpen = scenario("conformance-submit-after-open", "conformance", canonical()) {
	account(canonicalAccount.id)

	openTab(MaincoreUiTags.TuIndiceBottomBarPensumItem, PensumUiTags.PensumScreen)
	tap(BaseUiTags.topBarActionButton(TopBarAction.SearchPensumAction))
	submitTextEntry()
	waitVisible(SubjectsUiTags.SearchScreen, Within.Action)
}

/**
 * Sending the IME action: the password sheet of an expired session sends its request when the action of the
 * keyboard is sent, and the backend sees the credential the field holds.
 */
private val conformanceSubmitTextEntry = scenario(
	"conformance-submit-text-entry",
	"conformance",
	Start.Seeded(E2eAccounts.SessionInvalidated).toLaunchSpec()
) {
	account(E2eAccounts.SessionInvalidated.id)

	waitVisible(AuthUiTags.UpdatePasswordIdleContainer, Within.Sync)
	tap(AuthUiTags.PasswordTextField)
	enterSecureText(AuthUiTags.PasswordTextField, E2eAccounts.SessionInvalidated.password)
	submitTextEntry()
	expectRequest(
		"POST",
		"/auth/v1/token",
		basicAuth = "${E2eAccounts.SessionInvalidated.backendIdentifier}:${E2eAccounts.SessionInvalidated.password}"
	)
}

/**
 * A mock state set in the middle of a scenario: the summary failed once and shows its retry; the state is put
 * back to the one before that failure, so the first retry fails again and only the second one loads. Without
 * the step the first retry loads, and the second tap finds no retry button.
 */
private val conformanceMockState = scenario(
	"conformance-mock-state",
	"conformance",
	Start.Seeded(E2eAccounts.SummaryRefreshRetry).toLaunchSpec()
) {
	account(E2eAccounts.SummaryRefreshRetry.id)

	waitVisible(BaseUiTags.ErrorViewContainer, Within.Sync)
	mockState("summary-refresh-retry", "InitialSyncUnavailable")
	tap(BaseUiTags.ErrorViewRetryButton)
	// The start already left one 503 on the user route (the failure that shows the retry button), and the journal counts
	// from the start of the scenario: the first retry, made in this state, is answered 503 as well, so the condition
	// between the two taps is the second 503. It is what fails if the state step did not take effect (the retry would be
	// answered 200 and the count would stay at one).
	expectRequest("GET", "/users/v1", status = 503, atLeast = 2)
	tap(BaseUiTags.ErrorViewRetryButton)
	expectRequest("GET", "/users/v1", status = 200)
	waitVisible(SummaryUiTags.ContentContainer, Within.Long)
}

/**
 * Vertical scrolling in both directions with an effect that is checked: the About list is longer than any phone
 * screen, so the first link leaves the view when the toggle at the bottom is scrolled to, and comes back when the
 * list is scrolled up; a scroll that does not move the content fails on the `waitGone` after it.
 */
private val conformanceScroll = scenario("conformance-scroll", "conformance", canonical()) {
	account(canonicalAccount.id)

	openTab(MaincoreUiTags.TuIndiceBottomBarAboutItem, AboutUiTags.ContentContainer)
	waitVisible(AboutUiTags.OpenCreativeCommons, Within.Assert)
	waitGone(AboutUiTags.OpenKoin, Within.Assert)
	scrollUntilVisible(AboutUiTags.OpenKoin, Scroll.ContentDown, Within.Action)
	waitGone(AboutUiTags.OpenCreativeCommons, Within.Assert)
	scrollUntilVisible(AboutUiTags.OpenCreativeCommons, Scroll.ContentUp, Within.Action)
	waitGone(AboutUiTags.OpenKoin, Within.Assert)
}

/** A swipe over the screen with no element to start from moves a list: the last link of About comes into view. */
private val conformanceSwipeScreen = scenario("conformance-swipe-screen", "conformance", canonical()) {
	account(canonicalAccount.id)

	openTab(MaincoreUiTags.TuIndiceBottomBarAboutItem, AboutUiTags.ContentContainer)
	waitGone(AboutUiTags.OpenKotlin, Within.Assert)
	swipeScreen(SwipeDirection.Up, SWIPE_MS.milliseconds)
	waitVisible(AboutUiTags.OpenKotlin, Within.Action)
}

/** The usage-data switch of About reads its state and flips with a tap. */
private val conformanceChecked = scenario("conformance-checked", "conformance", canonical()) {
	account(canonicalAccount.id)

	openTab(MaincoreUiTags.TuIndiceBottomBarAboutItem, AboutUiTags.ContentContainer)
	scrollUntilVisible(AboutUiTags.UsageDataConsentToggle, Scroll.ContentDown, Within.Action)
	assertChecked(AboutUiTags.UsageDataConsentToggle, CONSENT_BEFORE)
	tap(AboutUiTags.UsageDataConsentToggle)
	assertChecked(AboutUiTags.UsageDataConsentToggle, !CONSENT_BEFORE)
}

/** A relaunch restarts the app keeping its session: About, left open, is replaced by the summary of a fresh start. */
private val conformanceRelaunch = scenario("conformance-relaunch", "conformance", canonical()) {
	account(canonicalAccount.id)

	openTab(MaincoreUiTags.TuIndiceBottomBarAboutItem, AboutUiTags.ContentContainer)
	relaunch()
	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	waitGone(AboutUiTags.ContentContainer, Within.Assert)
}

/** A tap that does not wait for the control to be enabled is made at once: the disabled sign-in button stays so. */
private val conformanceTapDisabled = scenario("conformance-tap-disabled", "conformance", clean()) {
	waitVisible(AuthUiTags.SignInIdleContainer, Within.Sync)
	assertEnabled(AuthUiTags.SignInButton, false)
	tap(tag(AuthUiTags.SignInButton), requireEnabled = false)
	waitVisible(AuthUiTags.SignInIdleContainer, Within.Assert)
	assertEnabled(AuthUiTags.SignInButton, false)
}

/**
 * Horizontal scrolling: a version option of the pensum context dialog is scrolled to. The four version options fit the
 * dialog on the screens the suite runs on (measured in E2c: with all of them in view, the scroll moves nothing), so
 * this proves the step does not break, not that it moves the content; see the consultation of the E2c report.
 */
private val conformanceScrollHorizontal = scenario("conformance-scroll-horizontal", "conformance", canonical()) {
	account(canonicalAccount.id)

	openTab(MaincoreUiTags.TuIndiceBottomBarPensumItem, PensumUiTags.PensumScreen)
	tap(PensumUiTags.PensumContextSummary)
	waitVisible(BaseUiTags.ConfirmationDialogSheet, Within.Action)
	scrollUntilVisible(PensumUiTags.versionOption(E2eInputs.OlderPensumYear), Scroll.ContentForward, Within.Action)
	tap(PensumUiTags.versionOption(E2eInputs.OlderPensumYear))
}

/** Tapping at a point of an element: the grade slider takes the grade from where it is tapped. */
private val conformanceTapAt = scenario("conformance-tap-at", "conformance", canonical()) {
	account(canonicalAccount.id)

	val primary = E2eFixtures.PrimaryAttempt.value

	openTab(MaincoreUiTags.TuIndiceBottomBarRecordItem, RecordUiTags.ContentContainer)
	tap(RecordUiTags.termChip(E2eFixtures.CurrentTerm.value))
	waitVisible(RecordUiTags.attemptGradeValue(primary, E2eInputs.GradeBefore), Within.Action)
	tapAt(RecordUiTags.attemptGradeSlider(primary), E2eInputs.SliderHighX, E2eInputs.SliderMiddleY)
	waitVisible(RecordUiTags.attemptGradeValue(primary, E2eInputs.GradeAfter), Within.Action)
	tapAt(RecordUiTags.attemptGradeSlider(primary), E2eInputs.SliderLowX, E2eInputs.SliderMiddleY)
	waitVisible(RecordUiTags.attemptGradeValue(primary, E2eInputs.GradeBefore), Within.Action)
}

/** Double tap and swipe over a gesture surface: the pensum canvas keeps its sticky term header. */
private val conformanceDoubleTapSwipe = scenario("conformance-double-tap-swipe", "conformance", canonical()) {
	account(canonicalAccount.id)

	openTab(MaincoreUiTags.TuIndiceBottomBarPensumItem, PensumUiTags.PensumScreen)
	waitVisible(PensumUiTags.Canvas, Within.Assert)
	doubleTap(PensumUiTags.Canvas)
	waitVisible(PensumUiTags.StickyTerms, Within.Assert)
	swipeScreen(SwipeDirection.Left, SWIPE_MS.milliseconds)
	waitVisible(PensumUiTags.StickyTerms, Within.Assert)
}

/**
 * Elements of the system outside the app: the share surface of About is the system's on both platforms, with elements
 * of its own on each (so this scenario holds one branch per platform, the only conformance one that does).
 */
private val conformanceSystem = scenario("conformance-system", "conformance", canonical()) {
	account(canonicalAccount.id)

	openTab(MaincoreUiTags.TuIndiceBottomBarAboutItem, AboutUiTags.ContentContainer)
	scrollUntilVisible(AboutUiTags.ShareApp, Scroll.ContentDown, Within.Action)
	tap(AboutUiTags.ShareApp)
	onPlatform(Platform.Android) {
		waitVisible(system(ANDROID_SHARE_ELEMENT), Within.Action)
		// Bringing the app back closes the chooser.
		foreground()
		waitGone(system(ANDROID_SHARE_ELEMENT), Within.Action)
	}
	onPlatform(Platform.Ios) {
		waitVisible(system(IOS_SHARE_ELEMENT), Within.Action)
		// The dimmed area closes the sheet; the tap goes where the top bar of About has nothing to activate.
		tapAtScreen(SHEET_DISMISS_X, SHEET_DISMISS_Y)
		waitGone(system(IOS_SHARE_ELEMENT), Within.Action)
	}
	waitVisible(AboutUiTags.ContentContainer, Within.Assert)
}

/**
 * The effect of the double tap: it zooms the canvas, which reveals the minimap toggle, and a second one zooms back to
 * fit. Android only: no delivery of the double tap that XCUITest offers makes the canvas zoom on the simulator, so on
 * iOS the primitive is only fired (`conformance-double-tap-swipe`) and the gap is written down in
 * `e2e/platform/ios/double-tap-canvas.md`.
 */
private val conformanceDoubleTapEffect = scenario("conformance-double-tap-effect", "conformance", canonical()) {
	platforms(Platform.Android)
	account(canonicalAccount.id)

	openTab(MaincoreUiTags.TuIndiceBottomBarPensumItem, PensumUiTags.PensumScreen)
	waitVisible(PensumUiTags.Canvas, Within.Assert)
	waitVisible(PensumUiTags.FitToScreen, Within.Assert)
	waitGone(PensumUiTags.MinimapToggle, Within.Assert)
	doubleTap(PensumUiTags.Canvas)
	waitVisible(PensumUiTags.MinimapToggle, Within.Assert)
	swipeScreen(SwipeDirection.Left, SWIPE_MS.milliseconds)
	waitVisible(PensumUiTags.StickyTerms, Within.Assert)
	doubleTap(PensumUiTags.Canvas)
	waitGone(PensumUiTags.FitToScreen, Within.Assert)
}

/**
 * A tap at a point of the screen with no element to aim at: the About item of the bottom bar sits where its fraction
 * of the screen says (the bar has five items of the same width, About the last, and the bar is the bottom strip), so
 * only a delivered tap there opens About.
 */
private val conformanceTapAtScreen = scenario("conformance-tap-at-screen", "conformance", canonical()) {
	account(canonicalAccount.id)

	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	tapAtScreen(BOTTOM_BAR_LAST_ITEM_X, BOTTOM_BAR_Y)
	waitVisible(AboutUiTags.ContentContainer, Within.Action)
}

/** A swipe that starts on an element: the schedule sheet closes when it is dragged down by its title. */
private val conformanceSwipeFromElement = scenario(
	"conformance-swipe-from-element",
	"conformance",
	Start.Seeded(E2eAccounts.AnnulledProvisional).toLaunchSpec()
) {
	account(E2eAccounts.AnnulledProvisional.id)

	openTab(MaincoreUiTags.TuIndiceBottomBarRecordItem, RecordUiTags.ContentContainer)
	tap(RecordUiTags.termChip(E2eFixtures.CurrentTerm.value))
	tap(BaseUiTags.topBarActionButton(TopBarAction.RecordScheduleAction))
	waitVisible(RecordUiTags.ScheduleTitle, Within.Action)
	swipeFrom(RecordUiTags.ScheduleTitle, SwipeDirection.Down, SWIPE_MS.milliseconds)
	waitGone(RecordUiTags.ScheduleSheet, Within.Action)
}

/** Tags inside a sheet or dialog: the sign-out confirmation shows its message and closes with its button. */
private val conformanceSheetTags = scenario("conformance-sheet-tags", "conformance", canonical()) {
	account(canonicalAccount.id)

	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	tap(BaseUiTags.topBarActionButton(TopBarAction.SignOutAction))
	waitVisible(AuthUiTags.SignOutMessageText, Within.Action)
	tap(BaseUiTags.ConfirmationDialogNegativeButton)
	waitGone(BaseUiTags.ConfirmationDialogSheet, Within.Action)
}

/** Returning to the app: an external link leaves it, and bringing it to the foreground finds About as it was. */
private val conformanceForeground = scenario("conformance-foreground", "conformance", canonical()) {
	account(canonicalAccount.id)

	openTab(MaincoreUiTags.TuIndiceBottomBarAboutItem, AboutUiTags.ContentContainer)
	tap(AboutUiTags.OpenCreativeCommons)
	waitBackgrounded(Within.Action)
	foreground()
	waitVisible(AboutUiTags.ContentContainer, Within.Action)
}

/** The system back action exists only on Android: the iOS driver always answers `false` to `pressBack`. */
private val conformanceBack = scenario("conformance-back", "conformance", canonical()) {
	platforms(Platform.Android)
	account(canonicalAccount.id)

	openTab(MaincoreUiTags.TuIndiceBottomBarAboutItem, AboutUiTags.ContentContainer)
	back()
	waitVisible(SummaryUiTags.ContentContainer, Within.Wait)
}

/** Backend access: the seeded start makes the app sync its record, which the mock server saw. */
private val conformanceBackend = scenario("conformance-backend", "conformance", canonical()) {
	account(canonicalAccount.id)

	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	expectRequest("POST", "/record/v5/sync", status = 200)
}

/** The conformance scenarios, which hold the two drivers to the same behaviour; list every new one here. */
val conformanceScenarios: List<Scenario> = listOf(
	conformanceLaunchSeeded,
	conformanceLaunchClean,
	conformanceTypeReadback,
	conformanceTypeReplace,
	conformanceTypeReplaceAfterBack,
	conformanceSecureField,
	conformanceEnabled,
	conformanceTextQuery,
	conformanceSubmitSearch,
	conformanceSubmitAfterOpen,
	conformanceSubmitTextEntry,
	conformanceMockState,
	conformanceScroll,
	conformanceSwipeScreen,
	conformanceChecked,
	conformanceRelaunch,
	conformanceTapDisabled,
	conformanceScrollHorizontal,
	conformanceTapAt,
	conformanceTapAtScreen,
	conformanceDoubleTapSwipe,
	conformanceDoubleTapEffect,
	conformanceSystem,
	conformanceSwipeFromElement,
	conformanceSheetTags,
	conformanceForeground,
	conformanceBack,
	conformanceBackend
)
