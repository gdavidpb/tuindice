package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcome
import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcomeKind
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.evaluations.di.evaluationsModule
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingEvaluationRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.koin.assertResolves
import com.gdavidpb.tuindice.testkit.koin.withKoinSmokeTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveEvaluationMutationOutcomesUseCaseTest {
	@Test
	fun evaluationsModule_resolvesTheUseCase() = withKoinSmokeTest(
		evaluationsModule,
		module {
			single<EvaluationRepository> { RecordingEvaluationRepository() }
			single<ReportingRepository> { RecordingReportingRepository() }
		}
	) {
		assertResolves(ObserveEvaluationMutationOutcomesUseCase::class)
	}

	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun executeOnBackground_emitsWhatTheRepositoryPublishes() = runTest {
		val outcomes = MutableSharedFlow<MutationOutcome>(extraBufferCapacity = 8)
		val repository = object : EvaluationRepository by RecordingEvaluationRepository() {
			override fun observeMutationOutcomesFlow(): Flow<MutationOutcome> = outcomes
		}
		val useCase = ObserveEvaluationMutationOutcomesUseCase(repository, RecordingReportingRepository())
		val received = mutableListOf<MutationOutcome>()

		backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
			useCase.executeOnBackground(Unit).collect { outcome -> received += outcome }
		}
		outcomes.emit(MutationOutcome("mutation-1", MutationOutcomeKind.CONFIRMED))

		assertEquals(listOf(MutationOutcome("mutation-1", MutationOutcomeKind.CONFIRMED)), received)
	}
}
