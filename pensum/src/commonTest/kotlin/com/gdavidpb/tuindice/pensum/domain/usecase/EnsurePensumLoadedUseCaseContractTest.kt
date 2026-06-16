package com.gdavidpb.tuindice.pensum.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.pensum.domain.usecase.exceptionhandler.UpdatePensumExceptionHandler
import com.gdavidpb.tuindice.pensum.testing.RecordingPensumRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class EnsurePensumLoadedUseCaseContractTest {
    @Test
    fun execute_whenSelectedPensumIsCached_thenSkipsRemoteRefresh() = runTest {
        val repository = RecordingPensumRepository(hasCachedPensum = true)
        val useCase = createUseCase(repository = repository)

        useCase.execute(Unit).test {
            assertEquals(
                EnsurePensumLoadedUseCase.Result.Cached,
                awaitLoadingThenData(this)
            )

            awaitComplete()
        }

        assertEquals(1, repository.hasSelectedPensumResponseCalls)
        assertEquals(0, repository.refreshCalls)
    }

    @Test
    fun execute_whenSelectedPensumIsMissing_thenRefreshesRemote() = runTest {
        val repository = RecordingPensumRepository(hasCachedPensum = false)
        val useCase = createUseCase(repository = repository)

        useCase.execute(Unit).test {
            assertEquals(
                EnsurePensumLoadedUseCase.Result.RefreshStarted,
                awaitLoadingThenData(this)
            )
            assertEquals(
                EnsurePensumLoadedUseCase.Result.RefreshSucceeded,
                assertIs<UseCaseState.Data<EnsurePensumLoadedUseCase.Result>>(awaitItem()).value
            )

            awaitComplete()
        }

        assertEquals(1, repository.hasSelectedPensumResponseCalls)
        assertEquals(1, repository.refreshCalls)
    }

    private fun createUseCase(
        repository: RecordingPensumRepository,
        reportingRepository: RecordingReportingRepository = RecordingReportingRepository()
    ): EnsurePensumLoadedUseCase {
        return EnsurePensumLoadedUseCase(
            pensumRepository = repository,
            reportingRepository = reportingRepository,
            exceptionHandler = UpdatePensumExceptionHandler(
                networkRepository = FakeNetworkRepository(isAvailable = true)
            )
        )
    }
}
