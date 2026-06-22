package com.gdavidpb.tuindice.wizard.domain.usecase

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MarkCoachmarkSeenUseCaseTest {
	@Test
	fun execute_marksKnownCoachmarkAsSeen() = runTest {
		val settingsRepository = FakeSettingsRepository()

		createUseCase(settingsRepository).execute(
			MarkCoachmarkSeenUseCase.Params(
				coachmarkId = "Summary",
				allCoachmarkIds = setOf("Summary", "Record")
			)
			).first { state -> state is UseCaseState.Data }

		assertEquals(setOf("Summary"), settingsRepository.getSeenCoachmarkIds())
	}

	@Test
	fun execute_ignoresUnknownCoachmarkIds() = runTest {
		val settingsRepository = FakeSettingsRepository()

		createUseCase(settingsRepository).execute(
			MarkCoachmarkSeenUseCase.Params(
				coachmarkId = "Unknown",
				allCoachmarkIds = setOf("Summary", "Record")
			)
			).first { state -> state is UseCaseState.Data }

		assertEquals(emptySet(), settingsRepository.getSeenCoachmarkIds())
	}

	private fun createUseCase(settingsRepository: FakeSettingsRepository) =
		MarkCoachmarkSeenUseCase(
			settingsRepository = settingsRepository,
			reportingRepository = RecordingReportingRepository()
		)
}
