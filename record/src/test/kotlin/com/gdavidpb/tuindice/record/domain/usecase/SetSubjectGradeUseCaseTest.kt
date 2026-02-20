package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.record.FakeQuarterRepository
import com.gdavidpb.tuindice.record.FakeReportingRepository
import com.gdavidpb.tuindice.record.domain.exception.SubjectIllegalArgumentException
import com.gdavidpb.tuindice.record.domain.model.SubjectGradeSet
import com.gdavidpb.tuindice.record.domain.usecase.error.SubjectUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.SetSubjectGradeExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.param.SetSubjectGradeParams
import com.gdavidpb.tuindice.record.domain.usecase.validator.SetSubjectGradeParamsValidator
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SetSubjectGradeUseCaseTest {
    private val quarterRepository = FakeQuarterRepository()
    private val reportingRepository = FakeReportingRepository()

    private lateinit var useCase: SetSubjectGradeUseCase

    @Before
    fun setUp() {
        useCase = SetSubjectGradeUseCase(
            quarterRepository = quarterRepository,
            paramsValidator = SetSubjectGradeParamsValidator(),
            exceptionHandler = SetSubjectGradeExceptionHandler(
                reportingRepository = reportingRepository
            )
        )
    }

    @Test
    fun execute_whenValidParams_emitsLoadingThenDataAndDispatchesToRepository() = runBlocking {
        val params = SetSubjectGradeParams(
            quarterId = "q1",
            subjectId = "s1",
            grade = 4,
            dispatchToRemote = true
        )

        val states = useCase.execute(params).toList()

        assertEquals(2, states.size)
        assertTrue(states[0] is UseCaseState.Loading)
        assertEquals(Unit, (states[1] as UseCaseState.Data).value)
        assertEquals(
            listOf(
                SubjectGradeSet(
                    id = "s1",
                    quarterId = "q1",
                    grade = 4,
                    dispatchToRemote = true
                )
            ),
            quarterRepository.setSubjectGradeCalls
        )
    }

    @Test
    fun execute_whenInvalidGrade_emitsOutOfRangeErrorAndDoesNotDispatch() = runBlocking {
        val params = SetSubjectGradeParams(
            quarterId = "q1",
            subjectId = "s1",
            grade = 9,
            dispatchToRemote = false
        )

        val states = useCase.execute(params).toList()

        assertEquals(1, states.size)
        assertEquals(
            SubjectUseCaseError.OutOfRangeGrade,
            (states[0] as UseCaseState.Error).error
        )
        assertTrue(quarterRepository.setSubjectGradeCalls.isEmpty())
    }

    @Test
    fun execute_whenRepositoryThrowsDomainException_emitsParsedError() = runBlocking {
        val params = SetSubjectGradeParams(
            quarterId = "q1",
            subjectId = "s1",
            grade = 5,
            dispatchToRemote = false
        )
        quarterRepository.setSubjectGradeThrowable = SubjectIllegalArgumentException(
            SubjectUseCaseError.OutOfRangeGrade
        )

        val states = useCase.execute(params).toList()

        assertEquals(2, states.size)
        assertTrue(states[0] is UseCaseState.Loading)
        assertEquals(
            SubjectUseCaseError.OutOfRangeGrade,
            (states[1] as UseCaseState.Error).error
        )
    }
}