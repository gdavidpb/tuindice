package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.domain.repository.NetworkStatusGateway
import com.gdavidpb.tuindice.base.domain.repository.ReportingGateway
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.persistence.utils.MAX_SUBJECT_GRADE
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.SubjectGradeSet
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import com.gdavidpb.tuindice.record.domain.usecase.error.GetQuartersUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.error.SubjectUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.GetQuartersExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.SetSubjectGradeExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.param.SetSubjectGradeParams
import com.gdavidpb.tuindice.record.domain.usecase.validator.SetSubjectGradeParamsValidator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class RecordUseCasesTest {
	@Test
	fun getQuarters_returnsRepositoryFlowValues() = runBlocking {
		val quarterRepository = DomainFakeQuarterRepository(
			quarters = listOf(sampleQuarter())
		)
		val useCase = GetQuartersUseCase(
			quarterRepository = quarterRepository,
			exceptionHandler = GetQuartersExceptionHandler(
				networkRepository = FakeNetworkStatusGateway(),
				reportingRepository = FakeReportingGateway()
			)
		)

		val states = useCase.execute(Unit).toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<List<Quarter>, GetQuartersUseCaseError>>(states[0])
		val success = assertIs<UseCaseState.Data<List<Quarter>, GetQuartersUseCaseError>>(states[1])
		assertEquals(1, success.value.size)
		assertEquals("q1", success.value.single().id)
	}

	@Test
	fun getQuarters_whenTimeout_emitsMappedTimeoutError() = runBlocking {
		val quarterRepository = DomainFakeQuarterRepository(
			shouldTimeout = true
		)
		val useCase = GetQuartersUseCase(
			quarterRepository = quarterRepository,
			exceptionHandler = GetQuartersExceptionHandler(
				networkRepository = FakeNetworkStatusGateway(),
				reportingRepository = FakeReportingGateway()
			)
		)

		val states = useCase.execute(Unit).toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<List<Quarter>, GetQuartersUseCaseError>>(states[0])
		val failure = assertIs<UseCaseState.Error<List<Quarter>, GetQuartersUseCaseError>>(states[1])
		assertEquals(GetQuartersUseCaseError.Timeout, failure.error)
	}

	@Test
	fun setSubjectGrade_withValidRange_mapsParamsAndCallsRepository() = runBlocking {
		val quarterRepository = DomainFakeQuarterRepository(quarters = listOf(sampleQuarter()))
		val useCase = SetSubjectGradeUseCase(
			quarterRepository = quarterRepository,
			paramsValidator = SetSubjectGradeParamsValidator(),
			exceptionHandler = SetSubjectGradeExceptionHandler(
				reportingRepository = FakeReportingGateway()
			)
		)

		val states = useCase.execute(
			SetSubjectGradeParams(
				quarterId = "q1",
				subjectId = "s1",
				grade = 4,
				commit = true
			)
		).toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<Unit, SubjectUseCaseError>>(states[0])
		assertIs<UseCaseState.Data<Unit, SubjectUseCaseError>>(states[1])
		assertEquals(
			SubjectGradeSet(
				id = "s1",
				quarterId = "q1",
				grade = 4,
				commit = true
			),
			quarterRepository.lastSetSubjectGrade
		)
	}

	@Test
	fun setSubjectGrade_whenOutOfRange_emitsOutOfRangeErrorAndDoesNotCallRepository() = runBlocking {
		val quarterRepository = DomainFakeQuarterRepository(quarters = listOf(sampleQuarter()))
		val useCase = SetSubjectGradeUseCase(
			quarterRepository = quarterRepository,
			paramsValidator = SetSubjectGradeParamsValidator(),
			exceptionHandler = SetSubjectGradeExceptionHandler(
				reportingRepository = FakeReportingGateway()
			)
		)

		val states = useCase.execute(
			SetSubjectGradeParams(
				quarterId = "q1",
				subjectId = "s1",
				grade = MAX_SUBJECT_GRADE + 1,
				commit = false
			)
		).toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<Unit, SubjectUseCaseError>>(states[0])
		val failure = assertIs<UseCaseState.Error<Unit, SubjectUseCaseError>>(states[1])
		assertEquals(SubjectUseCaseError.OutOfRangeGrade, failure.error)
		assertNull(quarterRepository.lastSetSubjectGrade)
	}
}

private class DomainFakeQuarterRepository(
	private val quarters: List<Quarter> = emptyList(),
	private val shouldTimeout: Boolean = false
) : QuarterRepository {
	var lastSetSubjectGrade: SubjectGradeSet? = null

	override suspend fun getQuartersFlow(): Flow<List<Quarter>> {
		return flow {
			if (shouldTimeout) {
				withTimeout(1) {
					delay(5)
				}
			}

			emit(quarters)
		}
	}

	override suspend fun getQuarters(): List<Quarter> = quarters

	override suspend fun removeQuarter(remove: QuarterRemove) = Unit

	override suspend fun setSubjectGrade(set: SubjectGradeSet) {
		lastSetSubjectGrade = set
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
