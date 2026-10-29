package com.gdavidpb.tuindice.presentation.route

import androidx.compose.runtime.snapshots.Snapshot
import com.gdavidpb.tuindice.auth.presentation.navigation.AuthDestination
import com.gdavidpb.tuindice.base.data.source.InMemoryTermReportFocusDataSource
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination
import com.gdavidpb.tuindice.testing.createTestNavigator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

// A tap on a term report's push waits for the student to be signed in, shows the report once, and takes the request.
@OptIn(ExperimentalCoroutinesApi::class)
class TermReportFocusEffectTest {

	@Test
	fun aPendingReport_waitsForTheSignIn_thenIsShown_andTheRequestIsTaken() = runTest {
		val navigator = createTestNavigator(startKey = AuthDestination.SignIn)
		val focus = InMemoryTermReportFocusDataSource()
		focus.request(insightId = "term-1:2")

		backgroundScope.launch {
			showPendingTermReport(insightId = "term-1:2", navigator = navigator, focusRepository = focus)
		}
		runCurrent()

		assertEquals("term-1:2", focus.observeRequest().first())
		assertEquals(false, navigator.backStack.contains(SummaryDestination.TermReport(insightId = "term-1:2")))

		Snapshot.withMutableSnapshot { navigator.replaceAllForSignedIn(MainSection.SUMMARY) }
		Snapshot.sendApplyNotifications()
		runCurrent()

		assertEquals(true, navigator.backStack.contains(SummaryDestination.TermReport(insightId = "term-1:2")))
		assertNull(focus.observeRequest().first())
	}

	@Test
	fun takingARequest_doesNotTakeAnotherOne() = runTest {
		val navigator = createTestNavigator(startKey = AuthDestination.SignIn)
		val focus = InMemoryTermReportFocusDataSource()
		focus.request(insightId = "term-1:2")
		Snapshot.withMutableSnapshot { navigator.replaceAllForSignedIn(MainSection.SUMMARY) }

		showPendingTermReport(insightId = "term-1:2", navigator = navigator, focusRepository = focus)
		focus.request(insightId = "term-1:3")
		focus.consume(insightId = "term-1:2")

		assertEquals("term-1:3", focus.observeRequest().first())
	}
}
