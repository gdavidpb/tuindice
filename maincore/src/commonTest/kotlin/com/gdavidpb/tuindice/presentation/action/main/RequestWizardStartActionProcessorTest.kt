package com.gdavidpb.tuindice.presentation.action.main

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.wizard.domain.usecase.ShouldStartWizardUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class RequestWizardStartActionProcessorTest {
	@Test
	fun process_navigatesOnlyWhenSessionIsActiveAndWizardIncomplete() = runTest {
		val effects = mutableListOf<Main.Effect>()
		val processor = createProcessor(
			sessionRepository = FakeSessionRepository(),
			settingsRepository = FakeSettingsRepository(wizardCompleted = false)
		)

		val state = reduce(
			initialState = Main.State.Content(startDestination = SummaryDestination.NavGraph),
			mutations = processor.process(Main.Action.RequestWizardStart, effects::add)
		) as Main.State.Content

		assertEquals(true, state.wizardStartRequested)
		assertContentEquals(listOf(Main.Effect.NavigateToWizard), effects)
	}

	@Test
	fun process_doesNotNavigateWhenWizardIsCompletedOrSessionMissingOrAlreadyRequested() = runTest {
		val completedEffects = mutableListOf<Main.Effect>()
		val completedState = reduce(
			initialState = Main.State.Content(startDestination = SummaryDestination.NavGraph),
			mutations = createProcessor(
				sessionRepository = FakeSessionRepository(),
				settingsRepository = FakeSettingsRepository(wizardCompleted = true)
			).process(Main.Action.RequestWizardStart, completedEffects::add)
		) as Main.State.Content

		val noSessionEffects = mutableListOf<Main.Effect>()
		val noSessionState = reduce(
			initialState = Main.State.Content(startDestination = SummaryDestination.NavGraph),
			mutations = createProcessor(
				sessionRepository = FakeSessionRepository(sessionId = ""),
				settingsRepository = FakeSettingsRepository(wizardCompleted = false)
			).process(Main.Action.RequestWizardStart, noSessionEffects::add)
		) as Main.State.Content

		val duplicateEffects = mutableListOf<Main.Effect>()
		val duplicateState = reduce(
			initialState = Main.State.Content(
				startDestination = SummaryDestination.NavGraph,
				wizardStartRequested = true
			),
			mutations = createProcessor(
				sessionRepository = FakeSessionRepository(),
				settingsRepository = FakeSettingsRepository(wizardCompleted = false)
			).process(Main.Action.RequestWizardStart, duplicateEffects::add)
		) as Main.State.Content

		assertEquals(false, completedState.wizardStartRequested)
		assertEquals(false, noSessionState.wizardStartRequested)
		assertEquals(true, duplicateState.wizardStartRequested)
		assertContentEquals(emptyList(), completedEffects)
		assertContentEquals(emptyList(), noSessionEffects)
		assertContentEquals(emptyList(), duplicateEffects)
	}

	private fun createProcessor(
		sessionRepository: FakeSessionRepository,
		settingsRepository: FakeSettingsRepository
	) = RequestWizardStartActionProcessor(
		shouldStartWizardUseCase = ShouldStartWizardUseCase(
			settingsRepository = settingsRepository,
			sessionRepository = sessionRepository,
			reportingRepository = RecordingReportingRepository()
		)
	)

	private suspend fun reduce(
		initialState: Main.State,
		mutations: Flow<Mutation<Main.State>>
	): Main.State {
		var state = initialState
		mutations.collect { mutation ->
			state = mutation(state)
		}
		return state
	}
}
