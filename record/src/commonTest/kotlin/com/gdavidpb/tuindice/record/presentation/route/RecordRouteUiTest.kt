package com.gdavidpb.tuindice.record.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.model.TopBarBannerBehavior
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.model.RecordRouteViewState
import com.gdavidpb.tuindice.record.presentation.model.RecordTopBarViewModeState
import com.gdavidpb.tuindice.record.presentation.viewmodel.RecordViewModel
import com.gdavidpb.tuindice.record.testing.ControllableAcademicRecordRepository
import com.gdavidpb.tuindice.record.testing.RecordRouteFixture
import com.gdavidpb.tuindice.record.testing.academicAttempt
import com.gdavidpb.tuindice.record.testing.academicTerm
import com.gdavidpb.tuindice.record.testing.recordRouteFixture
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class RecordRouteUiTest {
	@Test
	fun when_theRouteOpensOverAStoredRecord_then_itDrawsIt_refreshesOnce_andHandsTheBarItsActions() =
		runTuIndiceUiTest {
			val fixture = recordRouteFixture(record = projectionRecord())
			val host = RouteHost()

			setTuIndiceTestContent {
				Route(viewModel = fixture.viewModel, host = host)
			}

			awaitContent(fixture)

			assertNodeVisible(RecordUiTags.ContentContainer)
			assertNodeVisible(RecordUiTags.TermSelectorRow)
			// What is stored is shown at once; the refresh behind it does not have to be forced.
			waitUntil(timeoutMillis = StateTimeoutMillis) {
				fixture.academicRecordRepository.updateAcademicRecordCalls == 1
			}
			assertEquals(listOf(false), fixture.academicRecordRepository.updateAcademicRecordForceRemoteCalls)
			// The bar's icons live outside the route: this is how they reach it.
			assertNotNull(host.onViewModeChange)
			assertNotNull(host.onTermSelection)
			assertNotNull(host.onSchedule)
			assertTrue(host.navigations.isEmpty())
			assertTrue(host.snackBars.isEmpty())
			assertNodeHidden(RecordUiTags.TermSelectionSheet)
		}

	@Test
	fun when_theBarSwitchesTheMode_then_theChoiceIsStored_andTheBannerOfThatModeIsAnnounced() = runTuIndiceUiTest {
		val fixture = recordRouteFixture(record = projectionRecord())
		val host = RouteHost()

		setTuIndiceTestContent {
			Route(viewModel = fixture.viewModel, host = host)
		}

		awaitContent(fixture)
		assertTrue(host.banners.isEmpty(), "opening the record announces nothing")

		runOnIdle { assertNotNull(host.onViewModeChange).invoke(RecordViewMode.Historical) }

		waitUntil(timeoutMillis = StateTimeoutMillis) { host.banners.isNotEmpty() }

		assertEquals(listOf(RecordViewMode.Historical), fixture.selectionRepository.setViewModeCalls)
		// The banner waits for the record to be in the mode it announces.
		val content = assertIs<Record.State.Content>(fixture.viewModel.state.value)

		assertEquals(RecordViewMode.Historical, content.viewMode)
		assertEquals(HistoricalTermId, content.selectedTermId)
		assertIs<TopBarBannerBehavior.AutoDismiss>(host.banners.single())
	}

	@Test
	fun when_theBarOpensTheTermList_then_theSheetAppears_andChoosingAnotherTermSelectsIt() = runTuIndiceUiTest {
		val fixture = recordRouteFixture(record = projectionRecord())
		val host = RouteHost()

		setTuIndiceTestContent {
			Route(viewModel = fixture.viewModel, host = host)
		}

		val shown = awaitContent(fixture).selectedTermId
		val other = listOf(CurrentTermId, SyntheticTermId).first { termId -> termId != shown }

		assertNodeHidden(RecordUiTags.TermSelectionSheet)

		runOnIdle { assertNotNull(host.onTermSelection).invoke() }
		waitUntil(timeoutMillis = StateTimeoutMillis) {
			onAllNodesWithTag(RecordUiTags.termSelectionOption(other)).fetchSemanticsNodes().isNotEmpty()
		}

		onNodeWithTag(RecordUiTags.termSelectionOption(other)).performClick()

		waitUntil(timeoutMillis = StateTimeoutMillis) {
			(fixture.viewModel.state.value as? Record.State.Content)?.selectedTermId == other
		}

		assertEquals(RecordViewMode.Projection to other, fixture.selectionRepository.setSelectedTermCalls.last())
		// Choosing closes the list.
		waitUntil(timeoutMillis = StateTimeoutMillis) {
			onAllNodesWithTag(RecordUiTags.TermSelectionSheet).fetchSemanticsNodes().isEmpty()
		}
	}

	@Test
	fun when_theBarOpensTheSchedule_then_theRouteNavigatesToIt() = runTuIndiceUiTest {
		val fixture = recordRouteFixture(record = projectionRecord())
		val host = RouteHost()

		setTuIndiceTestContent {
			Route(viewModel = fixture.viewModel, host = host)
		}

		awaitContent(fixture)

		runOnIdle { assertNotNull(host.onSchedule).invoke() }

		assertEquals(listOf("schedule"), host.navigations)
	}

	@Test
	fun when_aSimulatedTermIsOnScreen_then_itsActionsAndTheCreateButtonNavigateWithItsId() = runTuIndiceUiTest {
		val fixture = recordRouteFixture(record = projectionRecord())
		val host = RouteHost()

		fixture.selectionRepository.setSelectedTermId(viewMode = RecordViewMode.Projection, termId = SyntheticTermId)

		setTuIndiceTestContent {
			Route(viewModel = fixture.viewModel, host = host)
		}

		assertEquals(SyntheticTermId, awaitContent(fixture).selectedTermId)
		assertNodeVisible(RecordUiTags.EditSyntheticTermButton)

		onNodeWithTag(RecordUiTags.EditSyntheticTermButton).performClick()
		onNodeWithTag(RecordUiTags.DeleteSyntheticTermButton).performClick()
		onNodeWithTag(RecordUiTags.CreateSyntheticTermFab).performClick()

		assertEquals(listOf("update:$SyntheticTermId", "delete:$SyntheticTermId", "create"), host.navigations)
		// Deleting is confirmed elsewhere: the route only asks for the confirmation.
		assertTrue(fixture.academicRecordRepository.deletedTermIds.isEmpty())
	}

	@Test
	fun when_theCurrentTermIsOnScreen_then_theProofButtonNavigatesToTheProof() = runTuIndiceUiTest {
		val fixture = recordRouteFixture(record = projectionRecord())
		val host = RouteHost()

		fixture.selectionRepository.setSelectedTermId(viewMode = RecordViewMode.Projection, termId = CurrentTermId)

		setTuIndiceTestContent {
			Route(viewModel = fixture.viewModel, host = host)
		}

		assertEquals(CurrentTermId, awaitContent(fixture).selectedTermId)
		assertNodeVisible(RecordUiTags.EnrollmentProofButton)

		onNodeWithTag(RecordUiTags.EnrollmentProofButton).performClick()

		assertEquals(listOf("proof"), host.navigations)
		assertNodeHidden(RecordUiTags.EditSyntheticTermButton)
	}

	@Test
	fun when_theFirstLoadFails_then_retryAsksForTheRecordAgain() = runTuIndiceUiTest {
		val repository = ControllableAcademicRecordRepository(initialHasSynced = false).apply {
			recordAvailable = false
			updateAcademicRecordThrowable = IllegalStateException("record unavailable")
		}
		val fixture = recordRouteFixture(record = AcademicRecord(id = "record"), academicRecordRepository = repository)
		val host = RouteHost()

		setTuIndiceTestContent {
			Route(viewModel = fixture.viewModel, host = host)
		}

		waitUntil(timeoutMillis = StateTimeoutMillis) { fixture.viewModel.state.value is Record.State.Failed }
		assertNodeVisible(BaseUiTags.ErrorViewRetryButton)
		// Nothing was stored, so the first load went straight to the university.
		assertEquals(listOf(true), repository.updateAcademicRecordForceRemoteCalls)

		onNodeWithTag(BaseUiTags.ErrorViewRetryButton).assertIsDisplayed().performClick()

		waitUntil(timeoutMillis = StateTimeoutMillis) { repository.updateAcademicRecordCalls == 2 }
		assertTrue(host.navigations.isEmpty(), "a failure that is not about the password stays on the record")
	}

	@Test
	fun when_theRouteLeavesTheScreen_then_theBarsActionsAreWithdrawn() = runTuIndiceUiTest {
		val fixture = recordRouteFixture(record = projectionRecord())
		val host = RouteHost()
		val isOnScreen = mutableStateOf(true)

		setTuIndiceTestContent {
			if (isOnScreen.value) {
				Route(viewModel = fixture.viewModel, host = host)
			}
		}

		awaitContent(fixture)
		assertNotNull(host.onSchedule)

		runOnIdle { isOnScreen.value = false }
		waitForIdle()

		// An icon left wired to a route that is gone would act on a screen nobody sees.
		assertNull(host.onViewModeChange)
		assertNull(host.onTermSelection)
		assertNull(host.onSchedule)
	}

	@Test
	fun when_theStateIsContent_then_theBarShowsTheModeSwitch_andTheScheduleOnlyIfTheTermHasOne() {
		val content = Record.State.Content(
			viewMode = RecordViewMode.Projection,
			record = projectionRecord(),
			selectedTermId = CurrentTermId
		)

		val withoutSchedule = assertIs<RecordRouteViewState>(content.toRouteViewState())
		val withSchedule = assertIs<RecordRouteViewState>(
			content.copy(hasSelectedTermSchedule = true).toRouteViewState()
		)

		assertEquals(TopBarConfig.Record, withoutSchedule.topBarConfig)
		assertEquals(TopBarConfig.RecordWithSchedule, withSchedule.topBarConfig)
		assertEquals(RecordTopBarViewModeState(selectedMode = RecordViewMode.Projection), withSchedule.topBarViewModeState)
		assertTrue(withSchedule.isTopBarVisible && withSchedule.isBottomBarVisible)
	}

	@Test
	fun when_thereIsNoContentYet_then_theBarOffersNoActions_andNoModeSwitch() {
		listOf(Record.State.Idle, Record.State.Loading).forEach { state ->
			val viewState = assertIs<RecordRouteViewState>(state.toRouteViewState())

			assertNull(viewState.topBarConfig, "the actions of $state")
			assertNull(viewState.topBarViewModeState, "the mode switch of $state")
			assertEquals(state.topBarTitle, viewState.topBarTitle)
		}
	}

	private fun ComposeUiTest.awaitContent(fixture: RecordRouteFixture): Record.State.Content {
		waitUntil(timeoutMillis = StateTimeoutMillis) { fixture.viewModel.state.value is Record.State.Content }
		waitForIdle()

		return assertIs<Record.State.Content>(fixture.viewModel.state.value)
	}

	@Composable
	private fun Route(viewModel: RecordViewModel, host: RouteHost) {
		RecordRoute(
			onNavigateToUpdatePassword = { host.navigations += "password" },
			onNavigateToCreateSyntheticTerm = { host.navigations += "create" },
			onNavigateToUpdateSyntheticTerm = { termId -> host.navigations += "update:$termId" },
			onNavigateToDeleteSyntheticTermConfirmation = { termId -> host.navigations += "delete:$termId" },
			onTopBarViewModeChangeAvailable = { action -> host.onViewModeChange = action },
			onTopBarTermSelectionAvailable = { action -> host.onTermSelection = action },
			onTopBarScheduleAvailable = { action -> host.onSchedule = action },
			onNavigateToSchedule = { host.navigations += "schedule" },
			onNavigateToEnrollmentProof = { host.navigations += "proof" },
			showTopBarBanner = { behavior -> host.banners += behavior },
			showSnackBar = { message -> host.snackBars += message },
			viewModel = viewModel
		)
	}

	// What stands around the route in the app: the bar that borrows its actions and the navigator.
	private class RouteHost {
		var onViewModeChange: ((RecordViewMode) -> Unit)? = null
		var onTermSelection: (() -> Unit)? = null
		var onSchedule: (() -> Unit)? = null
		val navigations = mutableListOf<String>()
		val banners = mutableListOf<TopBarBannerBehavior>()
		val snackBars = mutableListOf<SnackBarMessage>()
	}

	// A closed term, the current one and one simulated after it: Projection lists the three, and
	// Historical only the first.
	private fun projectionRecord() = AcademicRecord(
		id = "record",
		terms = listOf(
			academicTerm(
				id = HistoricalTermId,
				kind = TermKind.HISTORICAL,
				periodYear = 2025,
				periodCode = AcademicTermPeriod.SEP_DEC,
				attempts = listOf(academicAttempt(subjectCode = "MA1111"))
			),
			academicTerm(
				id = CurrentTermId,
				kind = TermKind.CURRENT,
				periodYear = 2026,
				periodCode = AcademicTermPeriod.SEP_DEC,
				attempts = listOf(academicAttempt(subjectCode = "CI5311"))
			),
			academicTerm(
				id = SyntheticTermId,
				kind = TermKind.SYNTHETIC,
				periodYear = 2027,
				periodCode = AcademicTermPeriod.JAN_MAR,
				attempts = listOf(academicAttempt(subjectCode = "CI5437"))
			)
		)
	)

	private companion object {
		const val HistoricalTermId = "historical-2025"
		const val CurrentTermId = "current-2026"
		const val SyntheticTermId = "synthetic-2027"

		// A cold start of the view model plus the first resource load on the simulator.
		const val StateTimeoutMillis = 15_000L
	}
}
