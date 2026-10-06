package com.gdavidpb.tuindice.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.model.OutdatedAppState
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.data.source.network.OutdatedAppEventDataSource
import com.gdavidpb.tuindice.domain.repository.OutdatedAppEventRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ObserveOutdatedAppUseCaseTest {
	@Test
	fun execute_emitsLoadingThenEveryRefusal_whileObserved() = runTest {
		val outdatedAppEventRepository = OutdatedAppEventDataSource()
		val useCase = ObserveOutdatedAppUseCase(
			outdatedAppEventRepository = outdatedAppEventRepository,
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(Unit).test {
			assertIs<UseCaseState.Loading>(awaitItem())

			outdatedAppEventRepository.notifyOutdatedApp(OutdatedAppState(minimumVersionCode = 52))
			assertEquals(
				UseCaseState.Data(OutdatedAppState(minimumVersionCode = 52)),
				awaitItem()
			)

			outdatedAppEventRepository.notifyOutdatedApp(OutdatedAppState(minimumVersionCode = 53))
			assertEquals(
				UseCaseState.Data(OutdatedAppState(minimumVersionCode = 53)),
				awaitItem()
			)

			// The source never completes: the observation lasts as long as its collector.
			expectNoEvents()
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun execute_doesNotReplay_whatWasToldBeforeItWasObserved() = runTest {
		val outdatedAppEventRepository = OutdatedAppEventDataSource()
		val useCase = ObserveOutdatedAppUseCase(
			outdatedAppEventRepository = outdatedAppEventRepository,
			reportingRepository = RecordingReportingRepository()
		)

		outdatedAppEventRepository.notifyOutdatedApp(OutdatedAppState(minimumVersionCode = 52))

		useCase.execute(Unit).test {
			assertIs<UseCaseState.Loading>(awaitItem())
			expectNoEvents()
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun execute_reportsAndEmitsError_whenTheObservationFails() = runTest {
		val failure = IllegalStateException("main-outdated-app-observation")
		val reportingRepository = RecordingReportingRepository()
		val useCase = ObserveOutdatedAppUseCase(
			outdatedAppEventRepository = object : OutdatedAppEventRepository {
				override fun observeOutdatedApp(): Flow<OutdatedAppState> = flow { throw failure }

				override suspend fun notifyOutdatedApp(state: OutdatedAppState) = Unit
			},
			reportingRepository = reportingRepository
		)

		useCase.execute(Unit).test {
			assertIs<UseCaseState.Loading>(awaitItem())
			assertIs<UseCaseState.Error<*>>(awaitItem())
			awaitComplete()
		}

		assertEquals(failure, reportingRepository.loggedExceptions.single())
		assertEquals("ObserveOutdatedAppUseCase", reportingRepository.customKeys["use-case"])
	}
}
