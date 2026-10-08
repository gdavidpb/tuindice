package com.gdavidpb.tuindice.scenarios.catalog

import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.StepBuilder
import com.gdavidpb.tuindice.scenariokit.dsl.enterText
import com.gdavidpb.tuindice.scenariokit.dsl.scenario
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.dsl.text
import com.gdavidpb.tuindice.scenariokit.dsl.waitGone
import com.gdavidpb.tuindice.scenariokit.dsl.waitVisible
import com.gdavidpb.tuindice.scenariokit.model.Scenario
import com.gdavidpb.tuindice.scenarios.fixture.Copy
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts
import com.gdavidpb.tuindice.scenarios.fixture.E2eFixtures
import com.gdavidpb.tuindice.scenarios.fixture.Start
import com.gdavidpb.tuindice.scenarios.shared.Within
import com.gdavidpb.tuindice.scenarios.shared.openSubjectSearch
import com.gdavidpb.tuindice.scenarios.shared.searchSubjectsFor
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.ui.MaincoreUiTags
import kotlin.time.Duration

private fun canonical() = Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()

/** From the seeded summary to the pensum tab, waiting up to [loaded] for the pensum. */
private fun StepBuilder.openPensum(loaded: Duration = Within.Long) {
	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	tap(MaincoreUiTags.TuIndiceBottomBarPensumItem)
	waitVisible(PensumUiTags.PensumScreen, loaded)
}

/** Taps the search result of [code] and returns to the search once [detail] has shown. */
private fun StepBuilder.openResultAndGoBack(code: String, detail: String) {
	waitVisible(SubjectsUiTags.searchResult(code), Within.Wait)
	tap(SubjectsUiTags.searchResult(code))
	waitVisible(detail, Within.Long)
	tap(MaincoreUiTags.TuIndiceTopBarBackButton)
	waitVisible(SubjectsUiTags.SearchScreen, Within.Action)
}

private val subjectsSmoke = scenario("subjects-smoke", "subjects", canonical()) {
	covers("subjects.SubjectSearch.UpdateQuery")
	account(E2eAccounts.Canonical.id)
	tags("smoke")

	openPensum(Within.Wait)
	openSubjectSearch()
	searchSubjectsFor("ci")
	waitVisible(SubjectsUiTags.searchResult(E2eFixtures.SubjectCi2511.value), Within.Wait)
	waitVisible(SubjectsUiTags.searchResultStatsButton(E2eFixtures.SubjectCi2511.value), Within.Assert)
	tap(SubjectsUiTags.searchResult(E2eFixtures.SubjectCi2511.value))
	waitVisible(SubjectsUiTags.Content, Within.Long)
	tap(MaincoreUiTags.TuIndiceTopBarBackButton)
	waitVisible(SubjectsUiTags.SearchScreen, Within.Action)
	tap(MaincoreUiTags.TuIndiceTopBarBackButton)
	waitVisible(PensumUiTags.PensumScreen, Within.Action)
}

private val subjectsSearchQueryClear = scenario("subjects-search-query-clear", "subjects", canonical()) {
	covers("subjects.SubjectSearch.UpdateQuery")
	account(E2eAccounts.Canonical.id)

	openPensum()
	openSubjectSearch()
	waitVisible(SubjectsUiTags.SearchGuidance, Within.Assert)
	tap(SubjectsUiTags.SearchTextField)
	enterText(SubjectsUiTags.SearchTextField, "c")
	waitVisible(SubjectsUiTags.SearchGuidance, Within.Assert)
	waitGone(SubjectsUiTags.searchResult(E2eFixtures.SubjectCi4325.value), Within.Assert)
	tap(SubjectsUiTags.SearchClear)
	waitVisible(SubjectsUiTags.SearchGuidance, Within.Assert)
	searchSubjectsFor("ci")
	waitVisible(SubjectsUiTags.searchResult(E2eFixtures.SubjectCi2511.value), Within.Wait)
	waitVisible(SubjectsUiTags.searchResultStatus(E2eFixtures.SubjectCi2511.value, "approved"), Within.Assert)
	waitVisible(SubjectsUiTags.searchResultStatsButton(E2eFixtures.SubjectCi2511.value), Within.Assert)
	tap(SubjectsUiTags.searchResultStatsButton(E2eFixtures.SubjectCi2511.value))
	waitVisible(SubjectsUiTags.Content, Within.Long)
	tap(MaincoreUiTags.TuIndiceTopBarBackButton)
	waitVisible(SubjectsUiTags.SearchScreen, Within.Action)
	tap(SubjectsUiTags.SearchClear)
	waitVisible(SubjectsUiTags.SearchGuidance, Within.Assert)
	waitGone(SubjectsUiTags.searchResult(E2eFixtures.SubjectCi2511.value), Within.Assert)
	searchSubjectsFor("zzzznomatch")
	waitVisible(text(Copy.SubjectsSearchNoResults), Within.Wait)
	waitVisible(SubjectsUiTags.SearchClear, Within.Assert)
	tap(SubjectsUiTags.SearchClear)
	waitVisible(SubjectsUiTags.SearchGuidance, Within.Assert)
	tap(MaincoreUiTags.TuIndiceTopBarBackButton)
	waitVisible(PensumUiTags.PensumScreen, Within.Action)
}

private val subjectsSearchFailedRetry = scenario("subjects-search-failed-retry", "subjects", canonical()) {
	covers("subjects.SubjectSearch.Retry")
	account(E2eAccounts.Canonical.id)

	openPensum()
	openSubjectSearch()
	searchSubjectsFor("rx")
	waitVisible(SubjectsUiTags.SearchRetry, Within.Wait)
	tap(SubjectsUiTags.SearchRetry)
	waitVisible(SubjectsUiTags.searchResult(E2eFixtures.SubjectRx.value), Within.Wait)
	tap(MaincoreUiTags.TuIndiceTopBarBackButton)
	waitVisible(PensumUiTags.PensumScreen, Within.Action)
}

private val subjectsDetailTabsTooltip = scenario("subjects-detail-tabs-tooltip", "subjects", canonical()) {
	covers("subjects.SubjectDetail.SelectSubjectSegmentTab")
	account(E2eAccounts.Canonical.id)

	openPensum()
	openSubjectSearch()
	searchSubjectsFor("ci")
	waitVisible(SubjectsUiTags.searchResult(E2eFixtures.SubjectCi2511.value), Within.Wait)
	tap(SubjectsUiTags.searchResult(E2eFixtures.SubjectCi2511.value))
	waitVisible(SubjectsUiTags.Content, Within.Long)
	waitVisible(SubjectsUiTags.CareerTab, Within.Assert)
	waitVisible(SubjectsUiTags.GlobalTab, Within.Assert)
	tap(SubjectsUiTags.GlobalTab)
	waitVisible(SubjectsUiTags.SegmentStudentsMetric, Within.Assert)
	tap(SubjectsUiTags.SegmentStudentsMetric)
	waitVisible(text(Copy.StudentsTooltipLine1), Within.Assert)
	waitVisible(text(Copy.StudentsTooltipLine2), Within.Assert)
	tap(SubjectsUiTags.CareerTab)
	waitVisible(SubjectsUiTags.SegmentAttemptsMetric, Within.Assert)
	tap(SubjectsUiTags.SegmentAttemptsMetric)
	waitVisible(text(Copy.AttemptsTooltipLine1), Within.Assert)
	waitVisible(text(Copy.AttemptsTooltipLine2), Within.Assert)
	tap(MaincoreUiTags.TuIndiceTopBarBackButton)
	waitVisible(SubjectsUiTags.SearchScreen, Within.Action)
	tap(MaincoreUiTags.TuIndiceTopBarBackButton)
	waitVisible(PensumUiTags.PensumScreen, Within.Action)
}

private val subjectsDetailUnavailable = scenario("subjects-detail-unavailable", "subjects", canonical()) {
	account(E2eAccounts.Canonical.id)

	openPensum()
	openSubjectSearch()
	searchSubjectsFor("qa")
	openResultAndGoBack(E2eFixtures.SubjectQa.value, SubjectsUiTags.Unavailable)
}

private val subjectsDetailFailedRetry = scenario("subjects-detail-failed-retry", "subjects", canonical()) {
	covers("subjects.SubjectDetail.RefreshSubjectDetail")
	account(E2eAccounts.Canonical.id)

	openPensum()
	openSubjectSearch()
	searchSubjectsFor("qb")
	waitVisible(SubjectsUiTags.searchResult(E2eFixtures.SubjectQb.value), Within.Wait)
	tap(SubjectsUiTags.searchResult(E2eFixtures.SubjectQb.value))
	waitVisible(SubjectsUiTags.Failed, Within.Long)
	tap(SubjectsUiTags.Retry)
	waitVisible(SubjectsUiTags.Content, Within.Long)
	tap(MaincoreUiTags.TuIndiceTopBarBackButton)
	waitVisible(SubjectsUiTags.SearchScreen, Within.Action)
}

/** The scenarios of this module; list every new one here. */
val subjectsScenarios: List<Scenario> = listOf(
	subjectsSmoke,
	subjectsSearchQueryClear,
	subjectsSearchFailedRetry,
	subjectsDetailTabsTooltip,
	subjectsDetailUnavailable,
	subjectsDetailFailedRetry
)
