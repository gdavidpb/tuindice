package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef
import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.domain.repository.NetworkStatusGateway
import com.gdavidpb.tuindice.base.domain.repository.ReportingGateway
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationSubjectFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.domain.model.GetEvaluations
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationFilterLabelsProvider
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationsUseCaseError
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.GetEvaluationsExceptionHandler
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.SubjectGradeSet
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import com.gdavidpb.tuindice.record.domain.usecase.GetQuartersUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetSubjectGradeUseCase
import com.gdavidpb.tuindice.record.domain.usecase.error.GetQuartersUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.error.SubjectUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.GetQuartersExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.SetSubjectGradeExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.param.SetSubjectGradeParams
import com.gdavidpb.tuindice.record.domain.usecase.validator.SetSubjectGradeParamsValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AcademicFixtureRegressionTest {
	@Test
	fun recordAndEvaluations_fixture_remainsConsistentAfterGradeUpdate() = runBlocking {
		val quarterRepository = AcademicFixtureQuarterRepository(
			quarters = fixtureQuarters().toMutableList()
		)
		val evaluationRepository = AcademicFixtureEvaluationRepository(
			evaluations = fixtureEvaluations(),
			subjectsProvider = quarterRepository::subjectsSnapshot
		)
		quarterRepository.onGetQuarters = evaluationRepository::hydrateSubjectsFromQuarters

		val getEvaluationsUseCase = GetEvaluationsUseCase(
			quarterRepository = quarterRepository,
			evaluationRepository = evaluationRepository,
			filterLabelsProvider = AcademicFixtureLabelsProvider(),
			exceptionHandler = GetEvaluationsExceptionHandler(AcademicFixtureReportingGateway())
		)
		val setSubjectGradeUseCase = SetSubjectGradeUseCase(
			quarterRepository = quarterRepository,
			paramsValidator = SetSubjectGradeParamsValidator(),
			exceptionHandler = SetSubjectGradeExceptionHandler(AcademicFixtureReportingGateway())
		)
		val getQuartersUseCase = GetQuartersUseCase(
			quarterRepository = quarterRepository,
			exceptionHandler = GetQuartersExceptionHandler(
				networkRepository = AcademicFixtureNetworkGateway(),
				reportingRepository = AcademicFixtureReportingGateway()
			)
		)

		val initialStates = getEvaluationsUseCase.execute(
			params = flowOf(listOf(EvaluationSubjectFilter("MAT101")))
		).toList()
		assertEquals(2, initialStates.size)
		val initialData = assertIs<UseCaseState.Data<GetEvaluations, EvaluationsUseCaseError>>(
			initialStates.last()
		)
		assertEquals(1, quarterRepository.getQuartersCalls)
		assertEquals(listOf("e-mat-pending", "e-mat-completed"), initialData.value.filteredEvaluations.map { it.id })
		assertEquals(7, initialData.value.availableFilters.size)

		val setStates = setSubjectGradeUseCase.execute(
			SetSubjectGradeParams(
				quarterId = "q-2026-1",
				subjectId = "s-mat-q1",
				grade = 5,
				commit = true
			)
		).toList()
		assertEquals(2, setStates.size)
		assertIs<UseCaseState.Data<Unit, SubjectUseCaseError>>(setStates.last())

		val quartersStates = getQuartersUseCase.execute(Unit).toList()
		assertEquals(2, quartersStates.size)
		val quartersData = assertIs<UseCaseState.Data<List<Quarter>, GetQuartersUseCaseError>>(
			quartersStates.last()
		)
		val firstQuarter = quartersData.value.first { quarter -> quarter.id == "q-2026-1" }
		assertEquals(5, firstQuarter.subjects.first { subject -> subject.id == "s-mat-q1" }.grade)
		assertEquals(4.5, firstQuarter.grade)
		assertEquals(9.0, firstQuarter.gradeSum)

		val afterUpdateStates = getEvaluationsUseCase.execute(flowOf(emptyList())).toList()
		val afterUpdateData = assertIs<UseCaseState.Data<GetEvaluations, EvaluationsUseCaseError>>(
			afterUpdateStates.last()
		)
		assertEquals(1, quarterRepository.getQuartersCalls)
		assertEquals(
			listOf("e-mat-pending", "e-fis-overdue", "e-mat-completed", "e-qui-continuous"),
			afterUpdateData.value.originalEvaluations.map { it.id }
		)
		assertTrue(afterUpdateData.value.filteredEvaluations.isNotEmpty())
	}
}

private class AcademicFixtureQuarterRepository(
	private val quarters: MutableList<Quarter>
) : QuarterRepository {
	var onGetQuarters: (() -> Unit)? = null
	var getQuartersCalls: Int = 0

	override suspend fun getQuartersFlow(): Flow<List<Quarter>> = flowOf(quarters.toList())

	override suspend fun getQuarters(): List<Quarter> {
		getQuartersCalls++
		onGetQuarters?.invoke()
		return quarters.toList()
	}

	override suspend fun removeQuarter(remove: QuarterRemove) {
		quarters.removeAll { quarter -> quarter.id == remove.id }
	}

	override suspend fun setSubjectGrade(set: SubjectGradeSet) {
		val quarterIndex = quarters.indexOfFirst { quarter -> quarter.id == set.quarterId }
		if (quarterIndex < 0) return
		val quarter = quarters[quarterIndex]
		val updatedSubjects = quarter.subjects.map { subject ->
			if (subject.id == set.id) subject.copy(grade = set.grade) else subject
		}
		val updatedGradeSum = updatedSubjects.sumOf { subject -> subject.grade.toDouble() }
		val updatedGrade = if (updatedSubjects.isEmpty()) 0.0 else updatedGradeSum / updatedSubjects.size

		quarters[quarterIndex] = quarter.copy(
			subjects = updatedSubjects,
			grade = updatedGrade,
			gradeSum = updatedGradeSum
		)
	}

	fun subjectsSnapshot(): List<Subject> {
		return quarters.flatMap { quarter -> quarter.subjects }
	}
}

private class AcademicFixtureEvaluationRepository(
	private val evaluations: List<Evaluation>,
	private val subjectsProvider: () -> List<Subject>
) : EvaluationRepository {
	private var availableSubjects: List<Subject> = emptyList()

	override suspend fun getEvaluationsFlow(): Flow<List<Evaluation>> = flowOf(evaluations)

	override suspend fun getEvaluation(eid: String): Evaluation? {
		return evaluations.firstOrNull { evaluation -> evaluation.id == eid }
	}

	override suspend fun addEvaluation(add: EvaluationAdd) = Unit

	override suspend fun updateEvaluation(update: EvaluationUpdate) = Unit

	override suspend fun removeEvaluation(remove: EvaluationRemove) = Unit

	override suspend fun getAvailableSubjects(): List<Subject> = availableSubjects

	fun hydrateSubjectsFromQuarters() {
		availableSubjects = subjectsProvider()
	}
}

private class AcademicFixtureLabelsProvider : EvaluationFilterLabelsProvider {
	override fun pending(): String = "Pending"

	override fun completed(): String = "Completed"

	override fun noGrade(): String = "No grade"

	override fun date(date: Long?): String = date?.toString() ?: "No date"
}

private class AcademicFixtureNetworkGateway : NetworkStatusGateway {
	override fun isAvailable(): Boolean = true
}

private class AcademicFixtureReportingGateway : ReportingGateway {
	override fun setIdentifier(identifier: String) = Unit

	override fun logException(throwable: Throwable) = Unit

	override fun logMessage(message: String) = Unit

	override fun <T : Any> setCustomKey(key: String, value: T) = Unit
}

private fun fixtureQuarters(): List<Quarter> {
	return listOf(
		Quarter(
			id = "q-2026-1",
			name = "2026-1",
			startDate = 1_704_067_200_000L,
			endDate = 1_712_774_400_000L,
			grade = 3.5,
			gradeSum = 7.0,
			credits = 6,
			creditsSum = 6,
			isCurrent = true,
			isReadOnly = false,
			subjects = listOf(
				Subject(
					id = "s-mat-q1",
					quarterId = "q-2026-1",
					code = "MAT101",
					name = "Calculo",
					credits = 3,
					grade = 3
				),
				Subject(
					id = "s-fis-q1",
					quarterId = "q-2026-1",
					code = "FIS101",
					name = "Fisica",
					credits = 3,
					grade = 4
				)
			)
		),
		Quarter(
			id = "q-2026-2",
			name = "2026-2",
			startDate = 1_712_860_800_000L,
			endDate = 1_721_568_000_000L,
			grade = 5.0,
			gradeSum = 5.0,
			credits = 3,
			creditsSum = 3,
			isCurrent = false,
			isReadOnly = false,
			subjects = listOf(
				Subject(
					id = "s-qui-q2",
					quarterId = "q-2026-2",
					code = "QUI201",
					name = "Quimica",
					credits = 3,
					grade = 5
				)
			)
		)
	)
}

private fun fixtureEvaluations(): List<Evaluation> {
	return listOf(
		Evaluation(
			id = "e-mat-pending",
			subjectId = "s-mat-q1",
			subjectCode = "MAT101",
			quarterId = "q-2026-1",
			grade = null,
			maxGrade = 20.0,
			date = null,
			type = EvaluationType.TEST,
			state = EvaluationState.PENDING
		),
		Evaluation(
			id = "e-fis-overdue",
			subjectId = "s-fis-q1",
			subjectCode = "FIS101",
			quarterId = "q-2026-1",
			grade = null,
			maxGrade = 20.0,
			date = null,
			type = EvaluationType.LABORATORY,
			state = EvaluationState.OVERDUE
		),
		Evaluation(
			id = "e-mat-completed",
			subjectId = "s-mat-q1",
			subjectCode = "MAT101",
			quarterId = "q-2026-1",
			grade = 16.0,
			maxGrade = 20.0,
			date = null,
			type = EvaluationType.QUIZ,
			state = EvaluationState.COMPLETED
		),
		Evaluation(
			id = "e-qui-continuous",
			subjectId = "s-qui-q2",
			subjectCode = "QUI201",
			quarterId = "q-2026-2",
			grade = null,
			maxGrade = 20.0,
			date = null,
			type = EvaluationType.PROJECT,
			state = EvaluationState.CONTINUOUS
		)
	)
}
