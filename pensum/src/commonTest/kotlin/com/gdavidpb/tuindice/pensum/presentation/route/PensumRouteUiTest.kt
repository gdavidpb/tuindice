package com.gdavidpb.tuindice.pensum.presentation.route

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.pensum.data.source.LocalSettingsDataSource
import com.gdavidpb.tuindice.pensum.domain.model.PensumModality
import com.gdavidpb.tuindice.pensum.domain.model.PensumObservation
import com.gdavidpb.tuindice.pensum.domain.model.PensumOption
import com.gdavidpb.tuindice.pensum.domain.usecase.EnsurePensumLoadedUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.ObservePensumUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.SelectPensumModalityUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.SelectPensumSelectionUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.SelectPensumUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.SetPensumSummaryCollapsedUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.UpdatePensumUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.exceptionhandler.UpdatePensumExceptionHandler
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import com.gdavidpb.tuindice.pensum.presentation.machine.PensumMachine
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenSessionStore
import com.gdavidpb.tuindice.pensum.presentation.model.PensumTopBarActionBus
import com.gdavidpb.tuindice.pensum.presentation.viewmodel.PensumViewModel
import com.gdavidpb.tuindice.pensum.testing.FakeSettings
import com.gdavidpb.tuindice.pensum.testing.RecordingPensumRepository
import com.gdavidpb.tuindice.pensum.testing.SAMPLE_PENSUM_CAREER_NAME
import com.gdavidpb.tuindice.pensum.testing.sampleObservedPensum
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.TestTuIndiceDispatchers
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class PensumRouteUiTest {
	@Test
	fun when_pensumIsObserved_then_routeRendersItsContentWithoutTheSelectionSheet() = runTuIndiceUiTest {
		val fixture = PensumRouteFixture()
		setRouteContent(fixture)

		assertNodeVisible(PensumUiTags.PensumScreen)
		onNodeWithText(SAMPLE_PENSUM_CAREER_NAME).assertExists()
		onNodeWithText("Pensum 2019").assertExists()
		assertNodeHidden(PensumUiTags.PensumCurrentSelectionSummary)
		assertFalse(fixture.screenSessionStore.isSelectionSheetVisible)
	}

	@Test
	fun when_changePensumTopBarActionArrives_then_selectionSheetOpensAndIsRemembered() =
		runTuIndiceUiTest {
			val fixture = PensumRouteFixture()
			setRouteContent(fixture)

			runOnIdle { fixture.topBarActionBus.dispatch(TopBarAction.ChangePensumAction) }
			waitForIdle()

			assertNodeVisible(PensumUiTags.PensumCurrentSelectionSummary)
			onNodeWithText("Cambiar pensum").assertExists()
			assertTrue(fixture.screenSessionStore.isSelectionSheetVisible)
		}

	@Test
	fun when_anotherTopBarActionArrives_then_selectionSheetStaysClosed() = runTuIndiceUiTest {
		val fixture = PensumRouteFixture()
		setRouteContent(fixture)

		runOnIdle { fixture.topBarActionBus.dispatch(TopBarAction.SearchPensumAction) }
		waitForIdle()

		assertNodeHidden(PensumUiTags.PensumCurrentSelectionSummary)
		assertFalse(fixture.screenSessionStore.isSelectionSheetVisible)
	}

	@Test
	fun when_pensumContextIsTapped_then_selectionSheetOpensOnTheCurrentYear() = runTuIndiceUiTest {
		val fixture = PensumRouteFixture()
		setRouteContent(fixture)

		onNodeWithTag(PensumUiTags.PensumContextSummary).performClick()
		waitForIdle()

		assertNodeVisible(PensumUiTags.PensumCurrentSelectionSummary)
		onNodeWithTag(PensumUiTags.versionOption(year = 2019)).assertIsSelected()
		assertTrue(fixture.screenSessionStore.isSelectionSheetVisible)
	}

	@Test
	fun when_sessionStoreRemembersTheSheet_then_routeReopensItAndCancelForgetsIt() = runTuIndiceUiTest {
		val fixture = PensumRouteFixture()
		fixture.screenSessionStore.isSelectionSheetVisible = true
		setRouteContent(fixture)

		assertNodeVisible(PensumUiTags.PensumCurrentSelectionSummary)
		onNodeWithTag(BaseUiTags.ConfirmationDialogNegativeButton).performClick()
		waitUntil(timeoutMillis = TIMEOUT_MILLIS) { !fixture.screenSessionStore.isSelectionSheetVisible }
		waitForIdle()

		assertNodeHidden(PensumUiTags.PensumCurrentSelectionSummary)
		assertEquals(emptyList(), fixture.repository.selectedSelections)
	}

	@Test
	fun when_anotherYearIsApplied_then_viewModelSelectsItAndTheSheetCloses() = runTuIndiceUiTest {
		val fixture = PensumRouteFixture()
		fixture.screenSessionStore.isSelectionSheetVisible = true
		setRouteContent(fixture)

		onNodeWithTag(PensumUiTags.versionOption(year = 2018)).performClick()
		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).performClick()
		waitUntil(timeoutMillis = TIMEOUT_MILLIS) { fixture.repository.selectedSelections.isNotEmpty() }
		waitForIdle()

		assertEquals(listOf(2018 to "degree_project"), fixture.repository.selectedSelections)
		assertNodeHidden(PensumUiTags.PensumCurrentSelectionSummary)
		assertFalse(fixture.screenSessionStore.isSelectionSheetVisible)
	}
}

private class PensumRouteFixture {
	val repository = RecordingPensumRepository(
		observations = listOf(PensumObservation.Content(pensum = sampleSelectablePensum()))
	)
	val topBarActionBus = PensumTopBarActionBus()
	val screenSessionStore = PensumScreenSessionStore()
	val viewModel = createPensumViewModel(repository)
}

@OptIn(ExperimentalTestApi::class)
private fun ComposeUiTest.setRouteContent(fixture: PensumRouteFixture) {
	setTuIndiceTestContent {
		PensumRoute(
			topBarActionBus = fixture.topBarActionBus,
			screenSessionStore = fixture.screenSessionStore,
			onNavigateToSubjectDetail = {},
			viewModel = fixture.viewModel
		)
	}

	waitUntil(timeoutMillis = TIMEOUT_MILLIS) { fixture.viewModel.state.value is Pensum.State.Content }
	waitForIdle()
}

private fun sampleSelectablePensum() = sampleObservedPensum().copy(
	availablePensums = listOf(
		PensumOption(id = "0800-2018", year = 2018),
		PensumOption(id = "0800-2019", year = 2019)
	),
	availableModalities = listOf(
		PensumModality(id = "degree_project", name = "Proyecto de Grado", isDefault = true),
		PensumModality(id = "long_internship", name = "Pasantía Larga", isDefault = false)
	)
)

private fun createPensumViewModel(repository: RecordingPensumRepository): PensumViewModel {
	val selectionRepository = LocalSettingsDataSource(settings = FakeSettings())
	val reportingRepository = RecordingReportingRepository()
	val exceptionHandler = UpdatePensumExceptionHandler(
		networkRepository = FakeNetworkRepository(isAvailable = true)
	)

	return PensumViewModel(
		screenMachine = PensumMachine(
			observePensumUseCase = ObservePensumUseCase(
				pensumRepository = repository,
				pensumSelectionRepository = selectionRepository,
				reportingRepository = reportingRepository
			),
			ensurePensumLoadedUseCase = EnsurePensumLoadedUseCase(
				pensumRepository = repository,
				reportingRepository = reportingRepository,
				exceptionHandler = exceptionHandler
			),
			updatePensumUseCase = UpdatePensumUseCase(
				pensumRepository = repository,
				reportingRepository = reportingRepository,
				exceptionHandler = exceptionHandler
			),
			selectPensumUseCase = SelectPensumUseCase(
				pensumRepository = repository,
				reportingRepository = reportingRepository,
				exceptionHandler = exceptionHandler
			),
			selectPensumModalityUseCase = SelectPensumModalityUseCase(
				pensumRepository = repository,
				reportingRepository = reportingRepository,
				exceptionHandler = exceptionHandler
			),
			selectPensumSelectionUseCase = SelectPensumSelectionUseCase(
				pensumRepository = repository,
				reportingRepository = reportingRepository,
				exceptionHandler = exceptionHandler
			),
			setPensumSummaryCollapsedUseCase = SetPensumSummaryCollapsedUseCase(
				pensumSelectionRepository = selectionRepository,
				reportingRepository = reportingRepository
			)
		),
		eventPublisher = NoOpEventPublisher,
		dispatchers = TestTuIndiceDispatchers(Dispatchers.Unconfined)
	)
}

private const val TIMEOUT_MILLIS = 5_000L
