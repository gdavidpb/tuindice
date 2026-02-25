package com.gdavidpb.tuindice.record.presentation.action

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
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.resource.RecordTextProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class RecordActionProcessorsTest {
	@Test
	fun loadQuartersActionProcessor_whenQuartersExist_setsContentState() = runBlocking {
		val processor = LoadQuartersActionProcessor(
			getQuartersUseCase = GetQuartersUseCase(
				quarterRepository = FakeQuarterRepository(
					quarters = listOf(sampleQuarter())
				),
				exceptionHandler = GetQuartersExceptionHandler(
					networkRepository = FakeNetworkStatusGateway(),
					reportingRepository = FakeReportingGateway()
				)
			),
			textProvider = FakeRecordTextProvider
		)
		val effects = mutableListOf<Record.Effect>()

		val mutations = processor.process(
			action = Record.Action.LoadQuarters,
			sideEffect = effects::add
		).toList()
		val finalState = applyMutations(Record.State.Failed, mutations)

		val content = assertIs<Record.State.Content>(finalState)
		assertEquals(1, content.quarters.size)
		assertTrue(effects.isEmpty())
	}

	@Test
	fun loadQuartersActionProcessor_whenQuartersEmpty_setsEmptyState() = runBlocking {
		val processor = LoadQuartersActionProcessor(
			getQuartersUseCase = GetQuartersUseCase(
				quarterRepository = FakeQuarterRepository(quarters = emptyList()),
				exceptionHandler = GetQuartersExceptionHandler(
					networkRepository = FakeNetworkStatusGateway(),
					reportingRepository = FakeReportingGateway()
				)
			),
			textProvider = FakeRecordTextProvider
		)
		val effects = mutableListOf<Record.Effect>()

		val mutations = processor.process(
			action = Record.Action.LoadQuarters,
			sideEffect = effects::add
		).toList()
		val finalState = applyMutations(Record.State.Loading, mutations)

		assertEquals(Record.State.Empty, finalState)
		assertTrue(effects.isEmpty())
	}

	@Test
	fun loadQuartersActionProcessor_whenTimeout_showsSnackBarAndFailedState() = runBlocking {
		val processor = LoadQuartersActionProcessor(
			getQuartersUseCase = GetQuartersUseCase(
				quarterRepository = FakeQuarterRepository(
					getQuartersShouldTimeout = true
				),
				exceptionHandler = GetQuartersExceptionHandler(
					networkRepository = FakeNetworkStatusGateway(),
					reportingRepository = FakeReportingGateway()
				)
			),
			textProvider = FakeRecordTextProvider
		)
		val effects = mutableListOf<Record.Effect>()

		val mutations = processor.process(
			action = Record.Action.LoadQuarters,
			sideEffect = effects::add
		).toList()
		val finalState = applyMutations(Record.State.Loading, mutations)

		assertEquals(Record.State.Failed, finalState)
		val snackBar = assertIs<Record.Effect.ShowSnackBar>(effects.single())
		assertEquals("Timeout", snackBar.message)
	}

	@Test
	fun loadQuartersActionProcessor_whenUnexpectedError_showsDefaultErrorAndFailedState() = runBlocking {
		val processor = LoadQuartersActionProcessor(
			getQuartersUseCase = GetQuartersUseCase(
				quarterRepository = FakeQuarterRepository(
					getQuartersThrowable = IllegalStateException("unexpected")
				),
				exceptionHandler = GetQuartersExceptionHandler(
					networkRepository = FakeNetworkStatusGateway(),
					reportingRepository = FakeReportingGateway()
				)
			),
			textProvider = FakeRecordTextProvider
		)
		val effects = mutableListOf<Record.Effect>()

		val mutations = processor.process(
			action = Record.Action.LoadQuarters,
			sideEffect = effects::add
		).toList()
		val finalState = applyMutations(Record.State.Loading, mutations)

		assertEquals(Record.State.Failed, finalState)
		val snackBar = assertIs<Record.Effect.ShowSnackBar>(effects.single())
		assertEquals("Default error", snackBar.message)
	}

	@Test
	fun setSubjectGradeActionProcessor_whenUseCaseSucceeds_keepsState() = runBlocking {
		val initialState = Record.State.Content(quarters = listOf(sampleQuarter()))
		val processor = SetSubjectGradeActionProcessor(
			setSubjectGradeUseCase = SetSubjectGradeUseCase(
				quarterRepository = FakeQuarterRepository(quarters = initialState.quarters),
				paramsValidator = SetSubjectGradeParamsValidator(),
				exceptionHandler = SetSubjectGradeExceptionHandler(
					reportingRepository = FakeReportingGateway()
				)
			),
			textProvider = FakeRecordTextProvider
		)
		val effects = mutableListOf<Record.Effect>()

		val mutations = processor.process(
			action = Record.Action.SetSubjectGrade(
				quarterId = "q1",
				subjectId = "s1",
				grade = 3,
				commit = true
			),
			sideEffect = effects::add
		).toList()
		val finalState = applyMutations(initialState, mutations)

		assertEquals(initialState, finalState)
		assertTrue(effects.isEmpty())
	}

	@Test
	fun setSubjectGradeActionProcessor_whenUseCaseFails_showsDefaultErrorAndKeepsState() = runBlocking {
		val initialState = Record.State.Content(quarters = listOf(sampleQuarter()))
		val processor = SetSubjectGradeActionProcessor(
			setSubjectGradeUseCase = SetSubjectGradeUseCase(
				quarterRepository = FakeQuarterRepository(
					quarters = initialState.quarters,
					setSubjectGradeThrowable = IllegalStateException("set subject grade failed")
				),
				paramsValidator = SetSubjectGradeParamsValidator(),
				exceptionHandler = SetSubjectGradeExceptionHandler(
					reportingRepository = FakeReportingGateway()
				)
			),
			textProvider = FakeRecordTextProvider
		)
		val effects = mutableListOf<Record.Effect>()

		val mutations = processor.process(
			action = Record.Action.SetSubjectGrade(
				quarterId = "q1",
				subjectId = "s1",
				grade = 3,
				commit = false
			),
			sideEffect = effects::add
		).toList()
		val finalState = applyMutations(initialState, mutations)

		assertEquals(initialState, finalState)
		val snackBar = assertIs<Record.Effect.ShowSnackBar>(effects.single())
		assertEquals("Default error", snackBar.message)
	}

	private fun applyMutations(
		initialState: Record.State,
		mutations: List<(Record.State) -> Record.State>
	): Record.State {
		return mutations.fold(initialState) { state, mutation -> mutation(state) }
	}
}

private object FakeRecordTextProvider : RecordTextProvider {
	override fun serviceUnavailable(): String = "Service unavailable"
	override fun networkUnavailable(): String = "Network unavailable"
	override fun timeout(): String = "Timeout"
	override fun defaultError(): String = "Default error"
}

private class FakeQuarterRepository(
	private val quarters: List<Quarter> = emptyList(),
	private val getQuartersThrowable: Throwable? = null,
	private val getQuartersShouldTimeout: Boolean = false,
	private val setSubjectGradeThrowable: Throwable? = null
) : QuarterRepository {
	override suspend fun getQuartersFlow(): Flow<List<Quarter>> {
		return flow {
			if (getQuartersShouldTimeout) {
				withTimeout(1) {
					delay(5)
				}
			}

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

private class FakeNetworkStatusGateway : NetworkStatusGateway {
	override fun isAvailable(): Boolean = true
}

private class FakeReportingGateway : ReportingGateway {
	override fun setIdentifier(identifier: String) = Unit

	override fun logException(throwable: Throwable) = Unit

	override fun logMessage(message: String) = Unit

	override fun <T : Any> setCustomKey(key: String, value: T) = Unit
}

private fun sampleQuarter(): Quarter {
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
