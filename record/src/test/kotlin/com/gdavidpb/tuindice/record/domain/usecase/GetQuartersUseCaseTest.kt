package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.record.FakeNetworkRepository
import com.gdavidpb.tuindice.record.FakeQuarterRepository
import com.gdavidpb.tuindice.record.FakeReportingRepository
import com.gdavidpb.tuindice.record.RecordFixtures
import com.gdavidpb.tuindice.record.domain.usecase.error.GetQuartersUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.GetQuartersExceptionHandler
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.SocketException
import java.util.concurrent.TimeoutException

class GetQuartersUseCaseTest {
    private val quarterRepository = FakeQuarterRepository()
    private val reportingRepository = FakeReportingRepository()
    private val networkRepository = FakeNetworkRepository()

    private lateinit var useCase: GetQuartersUseCase

    @Before
    fun setUp() {
        useCase = GetQuartersUseCase(
            quarterRepository = quarterRepository,
            exceptionHandler = GetQuartersExceptionHandler(
                networkRepository = networkRepository,
                reportingRepository = reportingRepository
            )
        )
    }

    @Test
    fun execute_whenSuccessful_emitsLoadingThenData() = runBlocking {
        val quarters = listOf(RecordFixtures.quarter())
        quarterRepository.quartersFlow = flowOf(quarters)

        val states = useCase.execute(Unit).toList()

        assertEquals(2, states.size)
        assertTrue(states[0] is UseCaseState.Loading)
        assertEquals(quarters, (states[1] as UseCaseState.Data).value)
    }

    @Test
    fun execute_whenRepositoryThrowsTimeout_emitsTimeoutError() = runBlocking {
        quarterRepository.quartersFlow = flow {
            throw TimeoutException("Timeout test")
        }

        val states = useCase.execute(Unit).toList()

        assertEquals(2, states.size)
        assertTrue(states[0] is UseCaseState.Loading)
        assertEquals(
            GetQuartersUseCaseError.Timeout,
            (states[1] as UseCaseState.Error).error
        )
    }

    @Test
    fun execute_whenRepositoryThrowsConnectionError_emitsNoConnectionError() = runBlocking {
        networkRepository.available = false
        quarterRepository.quartersFlow = flow {
            throw SocketException("Socket test")
        }

        val states = useCase.execute(Unit).toList()
        val error = (states[1] as UseCaseState.Error).error

        assertTrue(error is GetQuartersUseCaseError.NoConnection)
        assertFalse((error as GetQuartersUseCaseError.NoConnection).isNetworkAvailable)
    }
}