package com.gdavidpb.tuindice.wizard.domain.usecase

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ShouldStartWizardUseCaseTest {
	@Test
	fun execute_returnsTrueOnlyForActiveSessionAndIncompleteWizard() = runTest {
		val shouldStart = createUseCase(
			sessionRepository = FakeSessionRepository(),
			settingsRepository = FakeSettingsRepository(wizardCompleted = false)
		).execute(Unit).firstValue()

		val completed = createUseCase(
			sessionRepository = FakeSessionRepository(),
			settingsRepository = FakeSettingsRepository(wizardCompleted = true)
		).execute(Unit).firstValue()

		val noSession = createUseCase(
			sessionRepository = FakeSessionRepository(sessionId = ""),
			settingsRepository = FakeSettingsRepository(wizardCompleted = false)
		).execute(Unit).firstValue()

		assertTrue(shouldStart)
		assertFalse(completed)
		assertFalse(noSession)
	}

	private fun createUseCase(
		sessionRepository: FakeSessionRepository,
		settingsRepository: FakeSettingsRepository
	) = ShouldStartWizardUseCase(
		settingsRepository = settingsRepository,
		sessionRepository = sessionRepository,
		reportingRepository = RecordingReportingRepository()
	)

	private suspend fun Flow<UseCaseState<Boolean, Nothing>>.firstValue(): Boolean {
		return (first { state -> state is UseCaseState.Data } as UseCaseState.Data<Boolean>)
			.value
	}
}
