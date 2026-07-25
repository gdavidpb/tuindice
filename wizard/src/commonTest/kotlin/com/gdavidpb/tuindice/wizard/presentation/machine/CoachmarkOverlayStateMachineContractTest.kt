package com.gdavidpb.tuindice.wizard.presentation.machine

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.withUnconfinedTestDispatchers
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversAlphabet
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversEffects
import com.gdavidpb.tuindice.testkit.mvi.assertMachineHasNoShadowedRows
import com.gdavidpb.tuindice.testkit.mvi.assertMachineRandomWalk
import com.gdavidpb.tuindice.testkit.mvi.assertMachineStatesReachable
import com.gdavidpb.tuindice.testkit.mvi.exportToMermaid
import com.gdavidpb.tuindice.wizard.domain.model.CoachmarkResolution
import com.gdavidpb.tuindice.wizard.domain.usecase.MarkCoachmarkSeenUseCase
import com.gdavidpb.tuindice.wizard.domain.usecase.ResolveCoachmarkUseCase
import com.gdavidpb.tuindice.wizard.presentation.contract.CoachmarkOverlay
import com.gdavidpb.tuindice.wizard.presentation.model.CoachmarkId
import com.gdavidpb.tuindice.wizard.presentation.model.CoachmarkSurface
import com.gdavidpb.tuindice.wizard.presentation.model.persistedId
import com.gdavidpb.tuindice.wizard.presentation.viewmodel.CoachmarkOverlayViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CoachmarkOverlayStateMachineContractTest {
	@Test
	fun machine_coversAlphabet_andStateIsReachable() {
		val machine = createViewModel().machine

		assertMachineCoversAlphabet(
			machine,
			CoachmarkOverlay.Action::class,
			CoachmarkOverlayInternalEvent::class
		)

		assertMachineHasNoShadowedRows(
			machine,
			CoachmarkOverlay.Action::class,
			CoachmarkOverlayInternalEvent::class
		)

		assertMachineStatesReachable(
			machine = machine,
			initialState = CoachmarkOverlay.State::class
		)

		assertMachineCoversEffects(machine, CoachmarkOverlay.Effect::class)
	}

	@Test
	fun machine_exportsDeclaredTransitionsToMermaid() {
		val diagram = createViewModel().machine.exportToMermaid(
			machineName = "coachmark-overlay",
			initialState = CoachmarkOverlay.State::class
		)

		println(diagram)

		val expectedFragments = listOf(
			"SurfaceChanged",
			"PreviousActionClick",
			"PrimaryActionClick",
			"CoachmarkResolved"
		)

		for (fragment in expectedFragments) {
			assertTrue(
				diagram.contains(fragment),
				"Expected Mermaid export to mention '$fragment':\n$diagram"
			)
		}
	}

	@Test
	fun machine_survivesSeededRandomWalk() = runTest {
		val screenMachine = createScreenMachine()

		assertMachineRandomWalk(
			screenMachine = screenMachine,
			sampleEvents = listOf(
				CoachmarkOverlay.Action.SurfaceChanged(
					CoachmarkSurface(
						visitKey = "summary:1",
						eligibleCoachmarkIds = listOf(CoachmarkId.Summary)
					)
				),
				CoachmarkOverlay.Action.PreviousActionClick,
				CoachmarkOverlay.Action.PrimaryActionClick,
				CoachmarkOverlayInternalEvent.CoachmarkResolved(
					visitKey = "summary:1",
					resolution = CoachmarkResolution(
						pendingCoachmarkIds = listOf("Summary")
					)
				)
			),
			coroutineScope = backgroundScope,
			minRowCoverage = 0.5
		)
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	@Test
	fun coachmarkActions_navigatePendingCoachmarksBeforeClosingVisit() = runTest {
		withUnconfinedTestDispatchers { dispatchers ->
			val settingsRepository = FakeSettingsRepository()
			val viewModel = createViewModel(
				settingsRepository = settingsRepository,
				dispatchers = dispatchers
			)

			viewModel.state.test {
				assertFalse(awaitItem().isVisible)

				viewModel.surfaceChangedAction(
					CoachmarkSurface(
						visitKey = "record:1",
						eligibleCoachmarkIds = listOf(CoachmarkId.Record, CoachmarkId.RecordControls)
					)
				)
				assertFalse(awaitItem().isVisible)

				val firstCoachmarkState = awaitItem()
				assertEquals(CoachmarkId.Record, firstCoachmarkState.activeCoachmark?.id)
				assertTrue(firstCoachmarkState.hasNextCoachmark)

				viewModel.primaryActionClickAction()

				val secondCoachmarkState = awaitItem()
				assertEquals(CoachmarkId.RecordControls, secondCoachmarkState.activeCoachmark?.id)
				assertFalse(secondCoachmarkState.hasNextCoachmark)
				assertTrue(secondCoachmarkState.hasPreviousCoachmark)
				advanceUntilIdle()
				assertEquals(
					setOf(CoachmarkId.Record.persistedId),
					settingsRepository.getSeenCoachmarkIds()
				)

				viewModel.previousActionClickAction()

				val previousCoachmarkState = awaitItem()
				assertEquals(CoachmarkId.Record, previousCoachmarkState.activeCoachmark?.id)
				assertTrue(previousCoachmarkState.hasNextCoachmark)
				assertFalse(previousCoachmarkState.hasPreviousCoachmark)
				assertEquals(
					setOf(CoachmarkId.Record.persistedId),
					settingsRepository.getSeenCoachmarkIds()
				)

				viewModel.primaryActionClickAction()

				val secondCoachmarkStateAgain = awaitItem()
				assertEquals(CoachmarkId.RecordControls, secondCoachmarkStateAgain.activeCoachmark?.id)
				assertFalse(secondCoachmarkStateAgain.hasNextCoachmark)
				assertTrue(secondCoachmarkStateAgain.hasPreviousCoachmark)

				viewModel.primaryActionClickAction()

				assertNull(awaitItem().activeCoachmark)
				advanceUntilIdle()
				assertEquals(
					setOf(CoachmarkId.Record.persistedId, CoachmarkId.RecordControls.persistedId),
					settingsRepository.getSeenCoachmarkIds()
				)

				cancelAndIgnoreRemainingEvents()
			}
		}
	}

	private fun createViewModel(
		settingsRepository: FakeSettingsRepository = FakeSettingsRepository(),
		dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
	): CoachmarkOverlayViewModel {
		return CoachmarkOverlayViewModel(
			screenMachine = createScreenMachine(settingsRepository = settingsRepository),
			eventPublisher = NoOpEventPublisher,
			dispatchers = dispatchers
		)
	}

	private fun createScreenMachine(
		settingsRepository: FakeSettingsRepository = FakeSettingsRepository()
	): CoachmarkOverlayMachine {
		val reportingRepository = RecordingReportingRepository()

		return CoachmarkOverlayMachine(
			resolveCoachmarkUseCase = ResolveCoachmarkUseCase(
				settingsRepository = settingsRepository,
				sessionRepository = FakeSessionRepository(),
				reportingRepository = reportingRepository
			),
			markCoachmarkSeenUseCase = MarkCoachmarkSeenUseCase(
				settingsRepository = settingsRepository,
				reportingRepository = reportingRepository
			)
		)
	}
}
