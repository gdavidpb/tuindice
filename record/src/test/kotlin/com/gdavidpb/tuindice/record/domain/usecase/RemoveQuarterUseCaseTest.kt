package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.record.FakeQuarterRepository
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.usecase.param.RemoveQuarterParams
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RemoveQuarterUseCaseTest {
    private val quarterRepository = FakeQuarterRepository()

    private lateinit var useCase: RemoveQuarterUseCase

    @Before
    fun setUp() {
        useCase = RemoveQuarterUseCase(quarterRepository)
    }

    @Test
    fun execute_whenSuccessful_emitsLoadingThenDataAndDispatchesToRepository() = runBlocking {
        val params = RemoveQuarterParams(quarterId = "q-01")

        val states = useCase.execute(params).toList()

        assertEquals(2, states.size)
        assertTrue(states[0] is UseCaseState.Loading)
        assertEquals(Unit, (states[1] as UseCaseState.Data).value)
        assertEquals(listOf(QuarterRemove(id = "q-01")), quarterRepository.removeQuarterCalls)
    }

    @Test
    fun execute_whenRepositoryThrows_emitsNullError() = runBlocking {
        quarterRepository.removeQuarterThrowable = IllegalStateException("test")

        val states = useCase.execute(RemoveQuarterParams(quarterId = "q-01")).toList()

        assertEquals(2, states.size)
        assertTrue(states[0] is UseCaseState.Loading)
        assertNull((states[1] as UseCaseState.Error).error)
    }
}