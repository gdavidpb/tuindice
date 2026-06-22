package com.gdavidpb.tuindice.wizard.domain.usecase

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.wizard.domain.model.CoachmarkResolution
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ResolveCoachmarkUseCaseTest {
	@Test
	fun execute_returnsEligibleUnseenCoachmarks() = runTest {
		val resolution = createUseCase().execute(
			ResolveCoachmarkUseCase.Params(
				eligibleCoachmarkIds = listOf("Summary", "Record"),
				allCoachmarkIds = setOf("Summary", "Record")
			)
		).firstValue()

		assertEquals(listOf("Summary", "Record"), resolution?.pendingCoachmarkIds)
	}

	@Test
	fun execute_skipsSeenCoachmarkIdsFromPendingQueue() = runTest {
		val resolution = createUseCase(
			settingsRepository = FakeSettingsRepository(
				seenCoachmarkIds = mutableSetOf("Summary")
			)
		).execute(
			ResolveCoachmarkUseCase.Params(
				eligibleCoachmarkIds = listOf("Summary", "Record"),
				allCoachmarkIds = setOf("Summary", "Record")
			)
		).firstValue()

		assertEquals(listOf("Record"), resolution?.pendingCoachmarkIds)
	}

	@Test
	fun execute_returnsNullWithoutActiveSession() = runTest {
		val resolution = createUseCase(
			sessionRepository = FakeSessionRepository(
				sessionId = "",
				usbId = "",
				accessToken = "",
				refreshToken = ""
			)
		).execute(
			ResolveCoachmarkUseCase.Params(
				eligibleCoachmarkIds = listOf("Summary"),
				allCoachmarkIds = setOf("Summary")
			)
		).firstValue()

		assertNull(resolution)
	}

	@Test
	fun execute_migratesLegacyCompletedOnboardingAsSeenCoachmarks() = runTest {
		val settingsRepository = FakeSettingsRepository(
			legacyOnboardingCompleted = true
		)
		val resolution = createUseCase(settingsRepository = settingsRepository).execute(
			ResolveCoachmarkUseCase.Params(
				eligibleCoachmarkIds = listOf("Summary"),
				allCoachmarkIds = setOf("Summary", "Record")
			)
		).firstValue()

		assertNull(resolution)
		assertEquals(setOf("Summary", "Record"), settingsRepository.getSeenCoachmarkIds())
	}

	private fun createUseCase(
		settingsRepository: FakeSettingsRepository = FakeSettingsRepository(),
		sessionRepository: FakeSessionRepository = FakeSessionRepository()
	) = ResolveCoachmarkUseCase(
		settingsRepository = settingsRepository,
		sessionRepository = sessionRepository,
		reportingRepository = RecordingReportingRepository()
	)

	private suspend fun Flow<UseCaseState<CoachmarkResolution?, Nothing>>.firstValue():
		CoachmarkResolution? {
		return (first { state -> state is UseCaseState.Data }
			as UseCaseState.Data<CoachmarkResolution?>).value
	}
}
