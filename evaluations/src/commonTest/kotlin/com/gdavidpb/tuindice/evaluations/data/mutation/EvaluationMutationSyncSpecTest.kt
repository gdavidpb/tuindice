package com.gdavidpb.tuindice.evaluations.data.mutation

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_LOCAL_PENDING_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.FakeDatabaseDataSource
import com.gdavidpb.tuindice.evaluations.testing.FakeEvaluationsApiDataSource
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationFailureResolution
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationPrecondition
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

class EvaluationMutationSyncSpecTest {
	@Test
	fun deletePendingBeforeConfirm_keepsOptimisticStateVisibleUntilLocalConfirmation() = runTest {
		val evaluationsApiDataSource = FakeEvaluationsApiDataSource()
		val syncSpec = EvaluationMutationSyncSpec(
			databaseDataSource = FakeDatabaseDataSource(),
			evaluationsApiDataSource = evaluationsApiDataSource,
			refreshRemoteSnapshot = evaluationsApiDataSource::getEvaluations
		)

		val shouldDeleteBeforeConfirm = syncSpec.deletePendingBeforeConfirm(
			mutation = MutationEnvelope(
				mutationId = "mutation-0",
				scopeKey = EVALUATIONS_MUTATION_SCOPE,
				command = EvaluationMutation.Remove(
					evaluationId = DEFAULT_LOCAL_PENDING_EVALUATION.id
				),
				precondition = MutationPrecondition.Revision(DEFAULT_LOCAL_PENDING_EVALUATION.revision),
				status = PendingMutationStatus.Pending,
				createdAt = 1L,
				updatedAt = 1L,
				lastError = null
			)
		)

		assertFalse(shouldDeleteBeforeConfirm)
	}

	@Test
	fun resolveAddFailure_whenRequestFailsOffline_defersMutation_withoutSnapshotRefresh() = runTest {
		val offlineError = IllegalStateException("Could not connect to the server.")
		val evaluationsApiDataSource = FakeEvaluationsApiDataSource(
			getEvaluationsThrowable = offlineError
		)
		val syncSpec = EvaluationMutationSyncSpec(
			databaseDataSource = FakeDatabaseDataSource(),
			evaluationsApiDataSource = evaluationsApiDataSource,
			refreshRemoteSnapshot = evaluationsApiDataSource::getEvaluations
		)

		val resolution = syncSpec.resolveFailure(
			mutation = MutationEnvelope(
				mutationId = "mutation-1",
				scopeKey = EVALUATIONS_MUTATION_SCOPE,
				command = EvaluationMutation.Add(
					referenceId = "reference-1",
					attemptId = DEFAULT_EVALUATION_SUBJECT.id,
					subjectCode = DEFAULT_EVALUATION_SUBJECT.code,
					termId = DEFAULT_EVALUATION_SUBJECT.termId,
					scheduleMode = EvaluationScheduleMode.DATED,
					grade = null,
					maxGrade = 100.0,
					date = 1_900_000_000_000L,
					type = EvaluationType.QUIZ.ordinal
				),
				precondition = MutationPrecondition.None,
				status = PendingMutationStatus.Pending,
				createdAt = 1L,
				updatedAt = 1L,
				lastError = null
			),
			throwable = offlineError
		)

		assertIs<MutationFailureResolution.Defer<String, EvaluationMutation>>(resolution)
		assertEquals(0, evaluationsApiDataSource.getEvaluationsCalls)
	}

	@Test
	fun resolveRemoveFailure_whenRequestFailsOffline_defersMutation_withoutSnapshotRefresh() = runTest {
		val offlineError = IllegalStateException("Could not connect to the server.")
		val evaluationsApiDataSource = FakeEvaluationsApiDataSource(
			getEvaluationsThrowable = offlineError
		)
		val syncSpec = EvaluationMutationSyncSpec(
			databaseDataSource = FakeDatabaseDataSource(),
			evaluationsApiDataSource = evaluationsApiDataSource,
			refreshRemoteSnapshot = evaluationsApiDataSource::getEvaluations
		)

		val resolution = syncSpec.resolveFailure(
			mutation = MutationEnvelope(
				mutationId = "mutation-2",
				scopeKey = EVALUATIONS_MUTATION_SCOPE,
				command = EvaluationMutation.Remove(
					evaluationId = DEFAULT_LOCAL_PENDING_EVALUATION.id
				),
				precondition = MutationPrecondition.Revision(DEFAULT_LOCAL_PENDING_EVALUATION.revision),
				status = PendingMutationStatus.Pending,
				createdAt = 1L,
				updatedAt = 1L,
				lastError = null
			),
			throwable = offlineError
		)

		assertIs<MutationFailureResolution.Defer<String, EvaluationMutation>>(resolution)
		assertEquals(0, evaluationsApiDataSource.getEvaluationsCalls)
	}
}
