package com.gdavidpb.tuindice.presentation.navigation

import com.gdavidpb.tuindice.auth.presentation.navigation.AuthDestination
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.pensum.presentation.navigation.PensumDestination
import com.gdavidpb.tuindice.record.presentation.navigation.RecordDestination
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination
import com.gdavidpb.tuindice.testing.createTestNavigator
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

// Where the lines of the Summary hub lead, once the host connected them to its navigator.
class SummaryNavigationTest {
	private val navigator = createTestNavigator(startKey = SummaryDestination.Summary)

	@Test
	fun theRecordAndThePensumLinks_changeTheTab() {
		val navigation = navigator.toSummaryNavDependencies(onNavigateToEvaluationsWeek = {})

		navigation.onNavigateToRecord()

		assertEquals(MainSection.RECORD, navigator.currentTab)

		navigation.onNavigateToPensum()

		assertEquals(MainSection.PENSUM, navigator.currentTab)
		assertEquals(listOf(SummaryDestination.Summary, PensumDestination.Pensum), navigator.backStack)
	}

	@Test
	fun theScheduleLink_opensTheSheetOverSummary() {
		val navigation = navigator.toSummaryNavDependencies(onNavigateToEvaluationsWeek = {})

		navigation.onOpenSchedule()

		assertEquals(MainSection.SUMMARY, navigator.currentTab)
		assertEquals(RecordDestination.ScheduleDialog, navigator.currentKey)
	}

	@Test
	fun theEvaluationsWeek_isLeftToTheHost() {
		val asked = mutableListOf<LocalDate>()
		val navigation = navigator.toSummaryNavDependencies(onNavigateToEvaluationsWeek = { asked += it })

		navigation.onNavigateToEvaluationsWeek(LocalDate(2026, 10, 12))

		assertEquals(listOf(LocalDate(2026, 10, 12)), asked)
		assertEquals(MainSection.SUMMARY, navigator.currentTab)
	}

	@Test
	fun theUpdatePasswordLink_opensTheDialog() {
		val navigation = navigator.toSummaryNavDependencies(onNavigateToEvaluationsWeek = {})

		navigation.onNavigateToUpdatePassword()

		assertEquals(AuthDestination.UpdatePasswordDialog, navigator.currentKey)
	}
}
