package com.gdavidpb.tuindice.record.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.domain.repository.NetworkStatusGateway
import com.gdavidpb.tuindice.base.domain.repository.ReportingGateway
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
import com.gdavidpb.tuindice.record.presentation.resource.RecordTextProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

class RecordViewModelTest {
	@Test
	fun loadQuartersAction_whenDataExists_updatesStateToContent() = runBlocking {
		val viewModel = RecordViewModel(
			loadQuartersActionProcessor = LoadQuartersActionProcessor(
				getQuartersUseCase = GetQuartersUseCase(
					quarterRepository = RecordViewModelFakeQuarterRepository(
						quarters = listOf(recordViewModelSampleQuarter())
					),
					exceptionHandler = GetQuartersExceptionHandler(
						networkRepository = RecordViewModelFakeNetworkStatusGateway(),
						reportingRepository = RecordViewModelFakeReportingGateway()
					)
				),
				textProvider = RecordViewModelFakeTextProvider
			),
			setSubjectGradeActionProcessor = SetSubjectGradeActionProcessor(
				setSubjectGradeUseCase = SetSubjectGradeUseCase(
					quarterRepository = RecordViewModelFakeQuarterRepository(
						quarters = listOf(recordViewModelSampleQuarter())
					),
					paramsValidator = SetSubjectGradeParamsValidator(),
					exceptionHandler = SetSubjectGradeExceptionHandler(
						reportingRepository = RecordViewModelFakeReportingGateway()
					)
				),
				textProvider = RecordViewModelFakeTextProvider
			)
		)
		val stateJob = launch { viewModel.state.collect() }

		try {
			waitUntil { viewModel.action.subscriptionCount.value > 0 }

			viewModel.loadQuartersAction()

			waitUntil { viewModel.state.value is Record.State.Content }
			val content = assertIs<Record.State.Content>(viewModel.state.value)
			assertEquals(1, content.quarters.size)
		} finally {
			stateJob.cancel()
		}
	}

	@Test
	fun updateSubjectAction_whenUseCaseFails_emitsSnackBarEffect() = runBlocking {
		val viewModel = RecordViewModel(
			loadQuartersActionProcessor = LoadQuartersActionProcessor(
				getQuartersUseCase = GetQuartersUseCase(
					quarterRepository = RecordViewModelFakeQuarterRepository(
						quarters = listOf(recordViewModelSampleQuarter())
					),
					exceptionHandler = GetQuartersExceptionHandler(
						networkRepository = RecordViewModelFakeNetworkStatusGateway(),
						reportingRepository = RecordViewModelFakeReportingGateway()
					)
				),
				textProvider = RecordViewModelFakeTextProvider
			),
			setSubjectGradeActionProcessor = SetSubjectGradeActionProcessor(
				setSubjectGradeUseCase = SetSubjectGradeUseCase(
					quarterRepository = RecordViewModelFakeQuarterRepository(
						quarters = listOf(recordViewModelSampleQuarter()),
						setSubjectGradeThrowable = IllegalStateException("set grade failed")
					),
					paramsValidator = SetSubjectGradeParamsValidator(),
					exceptionHandler = SetSubjectGradeExceptionHandler(
						reportingRepository = RecordViewModelFakeReportingGateway()
					)
				),
				textProvider = RecordViewModelFakeTextProvider
			)
		)
		val effects = mutableListOf<Record.Effect>()
		val effectJob = launch { viewModel.effect.collect { effects += it } }
		val stateJob = launch { viewModel.state.collect() }

		try {
			waitUntil { viewModel.action.subscriptionCount.value > 0 }

			viewModel.updateSubjectAction(
				quarterId = "q1",
				subjectId = "s1",
				grade = 3,
				commit = false
			)

			waitUntil { effects.isNotEmpty() }
			val snackBar = assertIs<Record.Effect.ShowSnackBar>(effects.single())
			assertEquals("Default error", snackBar.message)
		} finally {
			effectJob.cancel()
			stateJob.cancel()
		}
	}

	private suspend fun waitUntil(
		timeoutMs: Long = 2_000L,
		condition: () -> Boolean
	) {
		val mark = TimeSource.Monotonic.markNow()

		while (!condition() && mark.elapsedNow() < timeoutMs.milliseconds) {
			delay(20)
		}

		assertTrue(condition(), "Condition not reached within timeout.")
	}
}

private object RecordViewModelFakeTextProvider : RecordTextProvider {
	override fun serviceUnavailable(): String = "Service unavailable"
	override fun networkUnavailable(): String = "Network unavailable"
	override fun timeout(): String = "Timeout"
	override fun defaultError(): String = "Default error"
}

private class RecordViewModelFakeQuarterRepository(
	private val quarters: List<Quarter> = emptyList(),
	private val getQuartersThrowable: Throwable? = null,
	private val setSubjectGradeThrowable: Throwable? = null
) : QuarterRepository {
	override suspend fun getQuartersFlow(): Flow<List<Quarter>> {
		return flow {
			getQuartersThrowable?.let { throw it }
			emit(quarters)
		}
	}

	override suspend fun getQuarters(): List<Quarter> {
		getQuartersThrowable?.let { throw it }
		return quarters
	}

	override suspend fun removeQuarter(remove: QuarterRemove) = Unit

	override suspend fun setSubjectGrade(set: SubjectGradeSet) {
		setSubjectGradeThrowable?.let { throw it }
	}
}

private class RecordViewModelFakeNetworkStatusGateway : NetworkStatusGateway {
	override fun isAvailable(): Boolean = true
}

private class RecordViewModelFakeReportingGateway : ReportingGateway {
	override fun setIdentifier(identifier: String) = Unit
	override fun logException(throwable: Throwable) = Unit
	override fun logMessage(message: String) = Unit
	override fun <T : Any> setCustomKey(key: String, value: T) = Unit
}

private fun recordViewModelSampleQuarter(): Quarter {
	return Quarter(
		id = "q1",
		name = "Q1",
		startDate = 1_700_000_000_000L,
		endDate = 1_700_086_400_000L,
		grade = 3.5,
		gradeSum = 3.5,
		credits = 3,
		creditsSum = 3,
		isCurrent = true,
		isReadOnly = false,
		subjects = listOf(
			Subject(
				id = "s1",
				quarterId = "q1",
				code = "MAT101",
				name = "Mathematics",
				credits = 3,
				grade = 3
			)
		)
	)
}
