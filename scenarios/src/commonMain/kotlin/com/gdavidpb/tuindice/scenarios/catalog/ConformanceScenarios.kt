package com.gdavidpb.tuindice.scenarios.catalog

import com.gdavidpb.tuindice.about.ui.AboutUiTags
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.StepBuilder
import com.gdavidpb.tuindice.scenariokit.dsl.SwipeDirection
import com.gdavidpb.tuindice.scenariokit.dsl.assertEnabled
import com.gdavidpb.tuindice.scenariokit.dsl.back
import com.gdavidpb.tuindice.scenariokit.dsl.doubleTap
import com.gdavidpb.tuindice.scenariokit.dsl.enterSecureText
import com.gdavidpb.tuindice.scenariokit.dsl.enterText
import com.gdavidpb.tuindice.scenariokit.dsl.expectRequest
import com.gdavidpb.tuindice.scenariokit.dsl.finishTextEntry
import com.gdavidpb.tuindice.scenariokit.dsl.foreground
import com.gdavidpb.tuindice.scenariokit.dsl.scenario
import com.gdavidpb.tuindice.scenariokit.dsl.scrollUntilVisible
import com.gdavidpb.tuindice.scenariokit.dsl.swipeFrom
import com.gdavidpb.tuindice.scenariokit.dsl.swipeScreen
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.dsl.tapAt
import com.gdavidpb.tuindice.scenariokit.dsl.text
import com.gdavidpb.tuindice.scenariokit.dsl.waitGone
import com.gdavidpb.tuindice.scenariokit.dsl.waitVisible
import com.gdavidpb.tuindice.scenariokit.model.Platform
import com.gdavidpb.tuindice.scenariokit.model.Scenario
import com.gdavidpb.tuindice.scenariokit.model.Scroll
import com.gdavidpb.tuindice.scenarios.fixture.Copy
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts
import com.gdavidpb.tuindice.scenarios.fixture.E2eFixtures
import com.gdavidpb.tuindice.scenarios.fixture.Start
import com.gdavidpb.tuindice.scenarios.shared.Within
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.ui.MaincoreUiTags
import com.gdavidpb.tuindice.wizard.ui.CoachmarkUiTags
import kotlin.time.Duration.Companion.milliseconds

// The grade slider of an attempt: a tap near its end raises the grade, one further in lowers it.
private const val SLIDER_HIGH_X = 0.95
private const val SLIDER_LOW_X = 0.75
private const val SLIDER_MIDDLE_Y = 0.5
private const val GRADE_BEFORE = 4
private const val GRADE_AFTER = 5

private const val OLDER_PENSUM_YEAR = 2018
private const val SWIPE_MS = 600L

// Two texts of the same length that share no letter, so a character left behind shows in the read-back.
private const val SEARCH_BEFORE = "pr"
private const val SEARCH_AFTER = "ma"

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
	finishTextEntry()
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

/** Ending a text entry: the search results are not hidden behind the keyboard. */
private val conformanceFinishTextEntry = scenario("conformance-finish-text-entry", "conformance", canonical()) {
	account(canonicalAccount.id)

	openTab(MaincoreUiTags.TuIndiceBottomBarPensumItem, PensumUiTags.PensumScreen)
	tap(BaseUiTags.topBarActionButton(TopBarAction.SearchPensumAction))
	waitVisible(SubjectsUiTags.SearchScreen, Within.Action)
	tap(SubjectsUiTags.SearchTextField)
	enterText(SubjectsUiTags.SearchTextField, "ci")
	finishTextEntry()
	waitVisible(SubjectsUiTags.searchResult(E2eFixtures.SubjectCi2511.value), Within.Wait)
}

/** Vertical scrolling: a toggle far down the About list becomes reachable and tappable. */
private val conformanceScroll = scenario("conformance-scroll", "conformance", canonical()) {
	account(canonicalAccount.id)

	openTab(MaincoreUiTags.TuIndiceBottomBarAboutItem, AboutUiTags.ContentContainer)
	scrollUntilVisible(AboutUiTags.UsageDataConsentToggle, Scroll.ContentDown, Within.Action)
	tap(AboutUiTags.UsageDataConsentToggle)
	waitVisible(AboutUiTags.ContentContainer, Within.Assert)
}

/** Horizontal scrolling: a version option off the edge of the pensum context dialog is brought into view. */
private val conformanceScrollHorizontal = scenario("conformance-scroll-horizontal", "conformance", canonical()) {
	account(canonicalAccount.id)

	openTab(MaincoreUiTags.TuIndiceBottomBarPensumItem, PensumUiTags.PensumScreen)
	tap(PensumUiTags.PensumContextSummary)
	waitVisible(BaseUiTags.ConfirmationDialogSheet, Within.Action)
	scrollUntilVisible(PensumUiTags.versionOption(OLDER_PENSUM_YEAR), Scroll.ContentForward, Within.Action)
	tap(PensumUiTags.versionOption(OLDER_PENSUM_YEAR))
}

/** Tapping at a point of an element: the grade slider takes the grade from where it is tapped. */
private val conformanceTapAt = scenario("conformance-tap-at", "conformance", canonical()) {
	account(canonicalAccount.id)

	val primary = E2eFixtures.PrimaryAttempt.value

	openTab(MaincoreUiTags.TuIndiceBottomBarRecordItem, RecordUiTags.ContentContainer)
	tap(RecordUiTags.termChip(E2eFixtures.CurrentTerm.value))
	waitVisible(RecordUiTags.attemptGradeValue(primary, GRADE_BEFORE), Within.Action)
	tapAt(RecordUiTags.attemptGradeSlider(primary), SLIDER_HIGH_X, SLIDER_MIDDLE_Y)
	waitVisible(RecordUiTags.attemptGradeValue(primary, GRADE_AFTER), Within.Action)
	tapAt(RecordUiTags.attemptGradeSlider(primary), SLIDER_LOW_X, SLIDER_MIDDLE_Y)
	waitVisible(RecordUiTags.attemptGradeValue(primary, GRADE_BEFORE), Within.Action)
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
	foreground()
	waitVisible(AboutUiTags.ContentContainer, Within.Action)
}

/** The system back action exists only on Android; iOS answers `false` to it, which `DriverContract` checks. */
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
	expectRequest("POST", "/record/v5/sync")
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
	conformanceFinishTextEntry,
	conformanceScroll,
	conformanceScrollHorizontal,
	conformanceTapAt,
	conformanceDoubleTapSwipe,
	conformanceSwipeFromElement,
	conformanceSheetTags,
	conformanceForeground,
	conformanceBack,
	conformanceBackend
)
