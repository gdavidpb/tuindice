package com.gdavidpb.tuindice.scenarios.catalog

import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusType
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.StepBuilder
import com.gdavidpb.tuindice.scenariokit.dsl.SwipeDirection
import com.gdavidpb.tuindice.scenariokit.dsl.doubleTap
import com.gdavidpb.tuindice.scenariokit.dsl.scenario
import com.gdavidpb.tuindice.scenariokit.dsl.scrollUntilVisible
import com.gdavidpb.tuindice.scenariokit.dsl.swipeScreen
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.dsl.text
import com.gdavidpb.tuindice.scenariokit.dsl.waitGone
import com.gdavidpb.tuindice.scenariokit.dsl.waitVisible
import com.gdavidpb.tuindice.scenariokit.model.Scenario
import com.gdavidpb.tuindice.scenariokit.model.Scroll
import com.gdavidpb.tuindice.scenarios.fixture.Copy
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccount
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts
import com.gdavidpb.tuindice.scenarios.fixture.E2eFixtures
import com.gdavidpb.tuindice.scenarios.fixture.E2eInputs
import com.gdavidpb.tuindice.scenarios.fixture.Start
import com.gdavidpb.tuindice.scenarios.shared.Within
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.ui.MaincoreUiTags
import kotlin.time.Duration.Companion.milliseconds

private const val ZOOM_IN_TAPS = 4
private const val SWIPE_MS = 600

private fun seeded(account: E2eAccount) = Start.Seeded(account).toLaunchSpec()

/** From the seeded summary to the pensum tab, waiting for [loaded] to show on arrival. */
private fun StepBuilder.openPensum(loaded: String = PensumUiTags.PensumScreen) {
	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	tap(MaincoreUiTags.TuIndiceBottomBarPensumItem)
	waitVisible(loaded, Within.Long)
}

/** Opens the version and modality dialog and picks the 2018 long internship pensum. */
private fun StepBuilder.chooseLongInternship2018() {
	tap(PensumUiTags.PensumContextSummary)
	waitVisible(BaseUiTags.ConfirmationDialogSheet, Within.Action)
	scrollUntilVisible(PensumUiTags.versionOption(E2eInputs.OlderPensumYear), Scroll.ContentForward, Within.Action)
	tap(PensumUiTags.versionOption(E2eInputs.OlderPensumYear))
	tap(PensumUiTags.modalityOption(E2eInputs.LongInternship))
	tap(BaseUiTags.ConfirmationDialogPositiveButton)
}

/** From the subject detail sheet to its statistics screen, and back to the pensum. */
private fun StepBuilder.openStatisticsAndGoBack() {
	tap(text(Copy.PensumViewStats))
	waitVisible(SubjectsUiTags.Content, Within.Long)
	waitVisible(SubjectsUiTags.SegmentStudentsMetric, Within.Assert)
	waitVisible(SubjectsUiTags.SegmentAttemptsMetric, Within.Assert)
	tap(MaincoreUiTags.TuIndiceTopBarBackButton)
	waitVisible(PensumUiTags.PensumScreen, Within.Action)
}

private val pensumSmoke = scenario("pensum-smoke", "pensum", seeded(E2eAccounts.Canonical)) {
	covers("pensum.Pensum.ToggleSummaryCollapsed")
	account(E2eAccounts.Canonical.id)
	tags("smoke")

	openPensum()
	waitVisible(PensumUiTags.PensumSummaryContainer, Within.Assert)
	tap(PensumUiTags.PensumSummaryContainer)
	waitGone(PensumUiTags.PensumContextSummary, Within.Assert)
	tap(PensumUiTags.PensumSummaryContainer)
	waitVisible(PensumUiTags.PensumContextSummary, Within.Assert)
	waitVisible(PensumUiTags.Canvas, Within.Assert)
	waitVisible(PensumUiTags.CanvasLegend, Within.Assert)
	tap(PensumUiTags.statusFilter(PensumNodeStatusType.AVAILABLE))
	waitVisible(PensumUiTags.StatusFilterClear, Within.Assert)
	tap(PensumUiTags.statusFilter(PensumNodeStatusType.CURRENT))
	tap(PensumUiTags.StatusFilterClear)
	waitGone(PensumUiTags.StatusFilterClear, Within.Assert)
	waitVisible(PensumUiTags.FocusProgress, Within.Assert)
	doubleTap(PensumUiTags.Canvas)
	waitVisible(PensumUiTags.StickyTerms, Within.Assert)
	swipeScreen(SwipeDirection.Left, SWIPE_MS.milliseconds)
	waitVisible(PensumUiTags.StickyTerms, Within.Assert)
	tap(PensumUiTags.FitToScreen)
	repeat(ZOOM_IN_TAPS) { tap(PensumUiTags.ZoomIn) }
	waitVisible(PensumUiTags.FitToScreen, Within.Action)
	waitVisible(PensumUiTags.MinimapToggle, Within.Action)
	tap(PensumUiTags.MinimapToggle)
	waitVisible(PensumUiTags.Minimap, Within.Action)
	tap(PensumUiTags.statusFilter(PensumNodeStatusType.AVAILABLE))
	waitVisible(PensumUiTags.StatusFilterClear, Within.Assert)
	waitVisible(PensumUiTags.StickyTerms, Within.Assert)
	tap(MaincoreUiTags.TuIndiceBottomBarSummaryItem)
	waitVisible(SummaryUiTags.ContentContainer, Within.Wait)
	tap(MaincoreUiTags.TuIndiceBottomBarPensumItem)
	waitVisible(PensumUiTags.PensumScreen, Within.Wait)
	waitVisible(PensumUiTags.StatusFilterClear, Within.Assert)
	waitVisible(PensumUiTags.StickyTerms, Within.Assert)
	waitVisible(PensumUiTags.Minimap, Within.Action)
	tap(PensumUiTags.Minimap)
	tap(PensumUiTags.FocusProgress)
	tap(PensumUiTags.ZoomOut)
	tap(PensumUiTags.FitToScreen)
	waitGone(PensumUiTags.FitToScreen, Within.Action)
	waitGone(PensumUiTags.MinimapToggle, Within.Assert)
	waitGone(PensumUiTags.Minimap, Within.Assert)
	waitGone(PensumUiTags.StickyTerms, Within.Assert)
}

private val pensumSelection = scenario("pensum-selection", "pensum", seeded(E2eAccounts.Canonical)) {
	covers("pensum.Pensum.SelectSelection")
	account(E2eAccounts.Canonical.id)

	openPensum()
	chooseLongInternship2018()
	waitVisible(PensumUiTags.node(E2eFixtures.PensumNodeLongInternshipMath1.value), Within.Long)
	waitVisible(PensumUiTags.Canvas, Within.Assert)
}

private val pensumNodeDetail = scenario("pensum-node-detail", "pensum", seeded(E2eAccounts.Canonical)) {
	account(E2eAccounts.Canonical.id)

	openPensum()
	waitVisible(PensumUiTags.node(E2eFixtures.PensumNodeMath1.value), Within.Wait)
	tap(PensumUiTags.node(E2eFixtures.PensumNodeMath1.value))
	waitVisible(PensumUiTags.SubjectDetailSheet, Within.Action)
	waitVisible(PensumUiTags.SubjectDetailStatus, Within.Assert)
	waitVisible(PensumUiTags.SubjectDetailTermValue, Within.Assert)
	tap(BaseUiTags.ConfirmationDialogNegativeButton)
	waitGone(PensumUiTags.SubjectDetailSheet, Within.Action)
	waitVisible(PensumUiTags.focusedNode(E2eFixtures.PensumNodeMath1.value), Within.Assert)
	tap(PensumUiTags.node(E2eFixtures.PensumNodeMath1.value))
	waitVisible(PensumUiTags.SubjectDetailSheet, Within.Action)
	openStatisticsAndGoBack()
}

private val pensumDetailNavigation = scenario(
	"pensum-detail-navigation",
	"pensum",
	seeded(E2eAccounts.Canonical)
) {
	account(E2eAccounts.Canonical.id)

	openPensum()
	waitVisible(PensumUiTags.node(E2eFixtures.PensumNodeMath2.value), Within.Wait)
	tap(PensumUiTags.node(E2eFixtures.PensumNodeMath2.value))
	waitVisible(PensumUiTags.SubjectDetailSheet, Within.Action)
	tap(PensumUiTags.SubjectDetailMoreButton)
	waitVisible(PensumUiTags.SubjectDetailRouteContext, Within.Action)
	waitVisible(PensumUiTags.SubjectDetailSelectedRouteCard, Within.Assert)
	waitVisible(PensumUiTags.SubjectDetailRequirements, Within.Assert)
	tap(PensumUiTags.subjectDetailRequirement(E2eFixtures.PensumNodeMath1.value))
	waitVisible(PensumUiTags.SubjectDetailSheet, Within.Action)
	waitVisible(PensumUiTags.SubjectDetailRouteContext, Within.Assert)
	waitVisible(PensumUiTags.SubjectDetailSelectedRouteCard, Within.Assert)
	waitVisible(PensumUiTags.SubjectDetailUnlocks, Within.Assert)
	tap(PensumUiTags.subjectDetailUnlock(E2eFixtures.PensumNodeMath2.value))
	waitVisible(PensumUiTags.SubjectDetailSheet, Within.Action)
	waitVisible(PensumUiTags.SubjectDetailSelectedRouteCard, Within.Assert)
}

private val pensumRefreshNotFound = scenario(
	"pensum-refresh-not-found",
	"pensum",
	seeded(E2eAccounts.PensumNotFound)
) {
	account(E2eAccounts.PensumNotFound.id)

	openPensum(BaseUiTags.EmptyViewContainer)
	waitVisible(BaseUiTags.EmptyViewTitle, Within.Assert)
	waitVisible(BaseUiTags.EmptyViewMessage, Within.Assert)
}

private val pensumRefreshFailedRetry = scenario(
	"pensum-refresh-failed-retry",
	"pensum",
	seeded(E2eAccounts.PensumRetry)
) {
	covers("pensum.Pensum.RefreshPensum")
	account(E2eAccounts.PensumRetry.id)

	openPensum(BaseUiTags.ErrorViewContainer)
	tap(BaseUiTags.ErrorViewRetryButton)
	waitVisible(PensumUiTags.PensumScreen, Within.Long)
	waitVisible(PensumUiTags.Canvas, Within.Assert)
}

private val pensumRecordUnavailable = scenario(
	"pensum-record-unavailable",
	"pensum",
	seeded(E2eAccounts.PensumRecordUnavailable)
) {
	covers("pensum.Pensum.EnsurePensumLoaded")
	account(E2eAccounts.PensumRecordUnavailable.id)

	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	waitVisible(MaincoreUiTags.TuIndiceBottomBarRecordItem, Within.Long)
	tap(MaincoreUiTags.TuIndiceBottomBarPensumItem)
	waitVisible(BaseUiTags.EmptyViewContainer, Within.Long)
	waitVisible(BaseUiTags.EmptyStateAnimation, Within.Assert)
	waitVisible(BaseUiTags.EmptyViewTitle, Within.Assert)
	waitVisible(BaseUiTags.EmptyViewMessage, Within.Assert)
	waitGone(BaseUiTags.EmptyViewActionButton, Within.Assert)
}

private val pensumCurrentAbsent = scenario(
	"pensum-current-absent",
	"pensum",
	seeded(E2eAccounts.PensumNoCurrent)
) {
	account(E2eAccounts.PensumNoCurrent.id)

	openPensum()
	waitVisible(PensumUiTags.Canvas, Within.Assert)
	waitGone(PensumUiTags.FocusProgress, Within.Assert)
}

private val pensumEquivalenceFulfilled = scenario(
	"pensum-equivalence-fulfilled",
	"pensum",
	seeded(E2eAccounts.PensumEquivalence)
) {
	account(E2eAccounts.PensumEquivalence.id)

	openPensum()
	waitVisible(PensumUiTags.node(E2eFixtures.PensumNodeLanguage1.value), Within.Wait)
	tap(PensumUiTags.node(E2eFixtures.PensumNodeLanguage1.value))
	waitVisible(PensumUiTags.SubjectDetailSheet, Within.Action)
	waitVisible(PensumUiTags.SubjectDetailMoreButton, Within.Assert)
	tap(PensumUiTags.SubjectDetailMoreButton)
	waitVisible(text(Copy.PensumFulfilledBy), Within.Action)
	waitVisible(text(Copy.PensumSubjectLanguage1), Within.Assert)
	openStatisticsAndGoBack()
}

/** The scenarios of this module; list every new one here. */
val pensumScenarios: List<Scenario> = listOf(
	pensumSmoke,
	pensumSelection,
	pensumNodeDetail,
	pensumDetailNavigation,
	pensumRefreshNotFound,
	pensumRefreshFailedRetry,
	pensumRecordUnavailable,
	pensumCurrentAbsent,
	pensumEquivalenceFulfilled
)
