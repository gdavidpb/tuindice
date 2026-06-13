package com.gdavidpb.tuindice.record.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermCreationSnapshot
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermPeriodOption
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.domain.usecase.param.ObserveSyntheticTermCreationParams
import com.gdavidpb.tuindice.record.testing.ControllableSyntheticTermCreationRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class ObserveSyntheticTermCreationUseCaseTest {
	@Test
	fun execute_emitsSnapshots_asRepositoryPublishesThem() = runTest {
		val repository = ControllableSyntheticTermCreationRepository()
		val useCase = createUseCase(repository)
		val emptySnapshot = creationSnapshot()
		val searchSnapshot = creationSnapshot(
			searchResults = listOf(syntheticSubject("MA1112"))
		)

		useCase.execute(createParams()).test {
			assertIs<UseCaseState.Loading>(awaitItem())

			repository.snapshotFlow.value = emptySnapshot
			assertEquals(
				emptySnapshot,
				assertIs<UseCaseState.Data<SyntheticTermCreationSnapshot>>(awaitItem()).value
			)

			repository.snapshotFlow.value = searchSnapshot
			assertEquals(
				searchSnapshot,
				assertIs<UseCaseState.Data<SyntheticTermCreationSnapshot>>(awaitItem()).value
			)

			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun execute_forwardsEveryParamsFlow_toRepository() = runTest {
		val repository = ControllableSyntheticTermCreationRepository()
		val useCase = createUseCase(repository)
		val params = createParams()

		useCase.execute(params).test {
			assertIs<UseCaseState.Loading>(awaitItem())

			cancelAndIgnoreRemainingEvents()
		}

		assertEquals(1, repository.observeSnapshotCalls.size)
		val call = repository.observeSnapshotCalls.single()
		assertSame(params.queryFlow, call.queryFlow)
		assertSame(params.selectedSubjectsFlow, call.selectedSubjectsFlow)
		assertSame(params.selectedPeriodKeyFlow, call.selectedPeriodKeyFlow)
		assertSame(params.editingTermIdFlow, call.editingTermIdFlow)
		assertSame(params.editingTermKeyFlow, call.editingTermKeyFlow)
	}

	private fun createUseCase(
		repository: ControllableSyntheticTermCreationRepository
	): ObserveSyntheticTermCreationUseCase {
		return ObserveSyntheticTermCreationUseCase(
			repository = repository,
			reportingRepository = RecordingReportingRepository()
		)
	}

	private fun createParams(): ObserveSyntheticTermCreationParams {
		return ObserveSyntheticTermCreationParams(
			queryFlow = MutableStateFlow(""),
			selectedSubjectsFlow = MutableStateFlow(emptyList()),
			selectedPeriodKeyFlow = MutableStateFlow(null),
			editingTermIdFlow = MutableStateFlow(null),
			editingTermKeyFlow = MutableStateFlow(null)
		)
	}

	private fun creationSnapshot(
		searchResults: List<SyntheticTermSubject> = emptyList()
	): SyntheticTermCreationSnapshot {
		val periodOption = SyntheticTermPeriodOption(
			periodYear = 9999,
			periodCode = AcademicTermPeriod.JAN_MAR
		)

		return SyntheticTermCreationSnapshot(
			periodOptions = listOf(periodOption),
			selectedPeriod = periodOption,
			selectedSubjects = emptyList(),
			suggestedSubjects = emptyList(),
			searchResults = searchResults
		)
	}

	private fun syntheticSubject(subjectCode: String): SyntheticTermSubject {
		return SyntheticTermSubject(
			subjectCode = subjectCode,
			name = subjectCode,
			credits = 4
		)
	}
}
