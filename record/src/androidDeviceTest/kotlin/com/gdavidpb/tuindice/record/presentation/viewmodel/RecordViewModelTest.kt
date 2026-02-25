package com.gdavidpb.tuindice.record.presentation.viewmodel

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.SubjectGradeSet
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import com.gdavidpb.tuindice.record.domain.usecase.GetQuartersUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetSubjectGradeUseCase
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.GetQuartersExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.SetSubjectGradeExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.validator.SetSubjectGradeParamsValidator
import com.gdavidpb.tuindice.record.presentation.action.LoadQuartersActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.SetSubjectGradeActionProcessor
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.resource.DefaultRecordTextProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.net.SocketException

@RunWith(AndroidJUnit4::class)
class RecordViewModelTest {
    private lateinit var quarterRepository: FakeQuarterRepository
    private lateinit var networkRepository: FakeNetworkRepository
    private val textProvider = DefaultRecordTextProvider()

    @Before
    fun setUp() {
        quarterRepository = FakeQuarterRepository()
        networkRepository = FakeNetworkRepository()
    }

    @Test
    fun loadQuartersAction_whenRepositoryReturnsData_updatesStateToContent() = runBlocking {
        val quarter = createQuarter()
        quarterRepository.quartersFlow = flowOf(listOf(quarter))

        val viewModel = createViewModel()

        val contentDeferred = async {
            withTimeout(5_000L) {
                viewModel.state.first { it is Record.State.Content } as Record.State.Content
            }
        }

        viewModel.loadQuartersAction()
        val contentState = contentDeferred.await()

        assertEquals(listOf(quarter), contentState.quarters)
    }

    @Test
    fun loadQuartersAction_whenConnectionFails_setsFailedStateAndShowsSnackBarEffect() = runBlocking {
        quarterRepository.quartersFlow = flow {
            throw SocketException("No network")
        }
        networkRepository.available = false

        val viewModel = createViewModel()

        val failedStateDeferred = async {
            withTimeout(5_000L) {
                viewModel.state.first { it is Record.State.Failed }
            }
        }
        val effectDeferred = async {
            withTimeout(5_000L) {
                viewModel.effect.first()
            }
        }

        viewModel.loadQuartersAction()

        val failedState = failedStateDeferred.await()
        val effect = effectDeferred.await()

        assertTrue(failedState is Record.State.Failed)
        assertTrue(effect is Record.Effect.ShowSnackBar)
        assertEquals(
            textProvider.networkUnavailable(),
            (effect as Record.Effect.ShowSnackBar).message
        )
    }

    @Test
    fun updateSubjectAction_whenGradeIsInvalid_keepsContentStateAndShowsDefaultErrorEffect() = runBlocking {
        quarterRepository.quartersFlow = flowOf(listOf(createQuarter()))

        val viewModel = createViewModel()
        withTimeout(5_000L) {
            val contentDeferred = async {
                viewModel.state.first { it is Record.State.Content } as Record.State.Content
            }
            viewModel.loadQuartersAction()
            contentDeferred.await()
        }

        val effectDeferred = async {
            withTimeout(5_000L) {
                viewModel.effect.first()
            }
        }

        viewModel.updateSubjectAction(
            quarterId = "q1",
            subjectId = "s1",
            grade = 9,
            commit = true
        )

        val effect = effectDeferred.await()
        val state = viewModel.state.value

        assertTrue(effect is Record.Effect.ShowSnackBar)
        assertEquals(
            textProvider.defaultError(),
            (effect as Record.Effect.ShowSnackBar).message
        )
        assertTrue(state is Record.State.Content)
        assertTrue(quarterRepository.setSubjectGradeCalls.isEmpty())
    }

    @Test
    fun updateSubjectAction_whenGradeIsValid_dispatchesToRepositoryWithoutChangingState() = runBlocking {
        quarterRepository.quartersFlow = flowOf(listOf(createQuarter()))

        val viewModel = createViewModel()
        withTimeout(5_000L) {
            val contentDeferred = async {
                viewModel.state.first { it is Record.State.Content } as Record.State.Content
            }
            viewModel.loadQuartersAction()
            contentDeferred.await()
        }

        viewModel.updateSubjectAction(
            quarterId = "q1",
            subjectId = "s1",
            grade = 4,
            commit = true
        )

        withTimeout(5_000L) {
            while (quarterRepository.setSubjectGradeCalls.isEmpty()) {
                delay(50L)
            }
        }

        assertEquals(
            listOf(
                SubjectGradeSet(
                    id = "s1",
                    quarterId = "q1",
                    grade = 4,
                    commit = true
                )
            ),
            quarterRepository.setSubjectGradeCalls
        )
        assertTrue(viewModel.state.value is Record.State.Content)
    }

    private fun createViewModel() = RecordViewModel(
        loadQuartersActionProcessor = LoadQuartersActionProcessor(
            getQuartersUseCase = GetQuartersUseCase(
                quarterRepository = quarterRepository,
                exceptionHandler = GetQuartersExceptionHandler(
                    networkRepository = networkRepository,
                    reportingRepository = FakeReportingRepository()
                )
            ),
            textProvider = textProvider
        ),
        setSubjectGradeActionProcessor = SetSubjectGradeActionProcessor(
            setSubjectGradeUseCase = SetSubjectGradeUseCase(
                quarterRepository = quarterRepository,
                paramsValidator = SetSubjectGradeParamsValidator(),
                exceptionHandler = SetSubjectGradeExceptionHandler(
                    reportingRepository = FakeReportingRepository()
                )
            ),
            textProvider = textProvider
        )
    )

    private fun createQuarter() = Quarter(
        id = "q1",
        name = "Enero - Marzo 2024",
        startDate = 1704067200000L,
        endDate = 1711843200000L,
        grade = 4.5,
        gradeSum = 4.2,
        credits = 6,
        creditsSum = 12,
        isCurrent = true,
        isReadOnly = false,
        subjects = listOf(
            Subject(
                id = "s1",
                quarterId = "q1",
                code = "MA1111",
                name = "MATEMATICAS I",
                credits = 4,
                grade = 3
            )
        )
    )
}

private class FakeQuarterRepository : QuarterRepository {
    var quartersFlow: Flow<List<Quarter>> = flowOf(emptyList())
    val setSubjectGradeCalls = mutableListOf<SubjectGradeSet>()

    override suspend fun getQuartersFlow(): Flow<List<Quarter>> = quartersFlow

    override suspend fun getQuarters(): List<Quarter> = getQuartersFlow().first()

    override suspend fun removeQuarter(remove: QuarterRemove) = Unit

    override suspend fun setSubjectGrade(set: SubjectGradeSet) {
        setSubjectGradeCalls += set
    }
}

private class FakeNetworkRepository(
    var available: Boolean = true
) : NetworkRepository {
    override fun isAvailable(): Boolean = available
}

private class FakeReportingRepository : ReportingRepository {
    override fun setIdentifier(identifier: String) = Unit

    override fun logException(throwable: Throwable) = Unit

    override fun logMessage(message: String) = Unit

    override fun <T : Any> setCustomKey(key: String, value: T) = Unit
}
