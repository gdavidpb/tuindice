package com.gdavidpb.tuindice.evaluations.data.mutation

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.exception.ServiceRetryWindowException
import com.gdavidpb.tuindice.base.domain.exception.SessionRecoveryAttestationException
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_LOCAL_PENDING_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.FakeDatabaseDataSource
import com.gdavidpb.tuindice.evaluations.testing.FakeEvaluationsApiDataSource
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationFailureResolution
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationPrecondition
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import com.gdavidpb.tuindice.testkit.ktor.serverResponseException
import io.ktor.http.HttpStatusCode
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

	// A 503 (or the wait the service asked for) is the service being away, not a verdict on the
	// change: the row stays Pending and goes out again with the next drain.
	@Test
	fun resolveFailure_whenTheServiceIsUnavailable_defersEveryKindOfMutation_withoutSnapshotRefresh() = runTest {
		val unavailableErrors = listOf(426, 429, 502, 503, 504).map(::responseWithStatus) +
			ServiceRetryWindowException(retryAfterMillis = 30_000L) +
			SessionRecoveryAttestationException(responseWithStatus(403))

		unavailableErrors.forEach { unavailable ->
			evaluationMutations().forEach { command ->
				val evaluationsApiDataSource = FakeEvaluationsApiDataSource(
					getEvaluationsThrowable = IllegalStateException("must not be read")
				)
				val syncSpec = EvaluationMutationSyncSpec(
					databaseDataSource = FakeDatabaseDataSource(),
					evaluationsApiDataSource = evaluationsApiDataSource,
					refreshRemoteSnapshot = evaluationsApiDataSource::getEvaluations
				)

				val resolution = syncSpec.resolveFailure(
					mutation = evaluationEnvelope(command),
					throwable = unavailable
				)

				assertIs<MutationFailureResolution.Defer<String, EvaluationMutation>>(resolution)
				assertEquals(0, evaluationsApiDataSource.getEvaluationsCalls)
			}
		}
	}

	// These say something about the request itself (or the server cannot handle it): a verdict, not an outage.
	@Test
	fun resolveFailure_whenTheServerRejectsTheRequest_stillFailsTerminally() = runTest {
		listOf(400, 401, 403, 423, 500).map(::responseWithStatus).forEach { rejection ->
			evaluationMutations().forEach { command ->
				val evaluationsApiDataSource = FakeEvaluationsApiDataSource()
				val syncSpec = EvaluationMutationSyncSpec(
					databaseDataSource = FakeDatabaseDataSource(),
					evaluationsApiDataSource = evaluationsApiDataSource,
					refreshRemoteSnapshot = evaluationsApiDataSource::getEvaluations
				)

				val resolution = syncSpec.resolveFailure(
					mutation = evaluationEnvelope(command),
					throwable = rejection
				)

				assertIs<MutationFailureResolution.Fail<String, EvaluationMutation>>(resolution)
			}
		}
	}

	// A response from the server is decided by its status code. Ktor puts the body in the exception message,
	// so a 400 or a 500 whose body talks about a timeout must stay a verdict, and a 408 is not an outage.
	@Test
	fun resolveFailure_aResponseIsDecidedByItsCode_neverByWhatItsBodySays() = runTest {
		val verdicts = listOf(
			clientRequestException(HttpStatusCode.BadRequest, message = "date: time out of range"),
			serverResponseException(HttpStatusCode.InternalServerError, message = "MongoTimeoutException: no primary"),
			clientRequestException(HttpStatusCode.RequestTimeout)
		)

		verdicts.forEach { verdict ->
			evaluationMutations().forEach { command ->
				val evaluationsApiDataSource = FakeEvaluationsApiDataSource()
				val syncSpec = EvaluationMutationSyncSpec(
					databaseDataSource = FakeDatabaseDataSource(),
					evaluationsApiDataSource = evaluationsApiDataSource,
					refreshRemoteSnapshot = evaluationsApiDataSource::getEvaluations
				)

				val resolution = syncSpec.resolveFailure(
					mutation = evaluationEnvelope(command),
					throwable = verdict
				)

				assertIs<MutationFailureResolution.Fail<String, EvaluationMutation>>(resolution)
			}
		}
	}

	@Test
	fun resolveFailure_aGatewayTimeout_defersEvenWhenItsBodyIsEmpty() = runTest {
		val gatewayTimeout = serverResponseException(HttpStatusCode.GatewayTimeout)

		evaluationMutations().forEach { command ->
			val evaluationsApiDataSource = FakeEvaluationsApiDataSource()
			val syncSpec = EvaluationMutationSyncSpec(
				databaseDataSource = FakeDatabaseDataSource(),
				evaluationsApiDataSource = evaluationsApiDataSource,
				refreshRemoteSnapshot = evaluationsApiDataSource::getEvaluations
			)

			val resolution = syncSpec.resolveFailure(
				mutation = evaluationEnvelope(command),
				throwable = gatewayTimeout
			)

			assertIs<MutationFailureResolution.Defer<String, EvaluationMutation>>(resolution)
		}
	}

	// The refresh that resolves a 409, 412 or 404 can fail too. When it fails because the service is away the row
	// goes out again with the next drain; parking it as FailedTerminal would lose a change nobody can see.
	@Test
	fun resolveFailure_whenTheRefreshThatResolvesItFailsTransiently_defersEveryKindOfMutation() = runTest {
		listOf(409, 412, 404).forEach { status ->
			evaluationMutations().forEach { command ->
				val database = FakeDatabaseDataSource()
				val evaluationsApiDataSource = FakeEvaluationsApiDataSource(
					getEvaluationsThrowable = responseWithStatus(503)
				)
				val syncSpec = EvaluationMutationSyncSpec(
					databaseDataSource = database,
					evaluationsApiDataSource = evaluationsApiDataSource,
					refreshRemoteSnapshot = evaluationsApiDataSource::getEvaluations
				)

				val resolution = syncSpec.resolveFailure(
					mutation = evaluationEnvelope(command),
					throwable = responseWithStatus(status)
				)

				assertIs<MutationFailureResolution.Defer<String, EvaluationMutation>>(resolution)
				assertEquals(1, evaluationsApiDataSource.getEvaluationsCalls)
				assertEquals(emptyList(), database.removedEvaluations)
			}
		}
	}

	@Test
	fun resolveFailure_whenTheRefreshThatResolvesItFailsForGood_failsEveryKindOfMutation() = runTest {
		listOf(409, 412, 404).forEach { status ->
			evaluationMutations().forEach { command ->
				val evaluationsApiDataSource = FakeEvaluationsApiDataSource(
					getEvaluationsThrowable = responseWithStatus(500)
				)
				val syncSpec = EvaluationMutationSyncSpec(
					databaseDataSource = FakeDatabaseDataSource(),
					evaluationsApiDataSource = evaluationsApiDataSource,
					refreshRemoteSnapshot = evaluationsApiDataSource::getEvaluations
				)

				val resolution = syncSpec.resolveFailure(
					mutation = evaluationEnvelope(command),
					throwable = responseWithStatus(status)
				)

				assertIs<MutationFailureResolution.Fail<String, EvaluationMutation>>(resolution)
			}
		}
	}

	@Test
	fun resolveFailure_whenTheRefreshThatResolvesItFailsOffline_defers() = runTest {
		val evaluationsApiDataSource = FakeEvaluationsApiDataSource(
			getEvaluationsThrowable = IllegalStateException("Could not connect to the server.")
		)
		val syncSpec = EvaluationMutationSyncSpec(
			databaseDataSource = FakeDatabaseDataSource(),
			evaluationsApiDataSource = evaluationsApiDataSource,
			refreshRemoteSnapshot = evaluationsApiDataSource::getEvaluations
		)

		val resolution = syncSpec.resolveFailure(
			mutation = evaluationEnvelope(evaluationMutations().first()),
			throwable = responseWithStatus(409)
		)

		assertIs<MutationFailureResolution.Defer<String, EvaluationMutation>>(resolution)
	}

	// A 404 on an edit or a delete discards the local copy only once the refresh has worked.
	@Test
	fun resolveFailure_aNotFoundOnEditOrDelete_discardsTheLocalCopyOnlyAfterTheRefreshWorked() = runTest {
		evaluationMutations().drop(1).forEach { command ->
			val database = FakeDatabaseDataSource()
			val evaluationsApiDataSource = FakeEvaluationsApiDataSource()
			val syncSpec = EvaluationMutationSyncSpec(
				databaseDataSource = database,
				evaluationsApiDataSource = evaluationsApiDataSource,
				refreshRemoteSnapshot = evaluationsApiDataSource::getEvaluations
			)

			val resolution = syncSpec.resolveFailure(
				mutation = evaluationEnvelope(command),
				throwable = responseWithStatus(404)
			)

			assertIs<MutationFailureResolution.Drop<String, EvaluationMutation>>(resolution)
			assertEquals(listOf(DEFAULT_LOCAL_PENDING_EVALUATION.id), database.removedEvaluations)
		}
	}
}

private fun evaluationMutations(): List<EvaluationMutation> = listOf(
	EvaluationMutation.Add(
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
	EvaluationMutation.Update(
		evaluationId = DEFAULT_LOCAL_PENDING_EVALUATION.id,
		scheduleMode = null,
		grade = 80.0,
		maxGrade = null,
		date = null,
		type = null
	),
	EvaluationMutation.Remove(evaluationId = DEFAULT_LOCAL_PENDING_EVALUATION.id)
)

private fun evaluationEnvelope(command: EvaluationMutation) = MutationEnvelope(
	mutationId = "mutation-x",
	scopeKey = EVALUATIONS_MUTATION_SCOPE,
	command = command,
	precondition = MutationPrecondition.Revision(DEFAULT_LOCAL_PENDING_EVALUATION.revision),
	status = PendingMutationStatus.Pending,
	createdAt = 1L,
	updatedAt = 1L,
	lastError = null
)

// A response with the given status whose message never says "timeout": the verdict must come from the code.
private fun responseWithStatus(code: Int): Throwable {
	val status = HttpStatusCode.fromValue(code)

	return if (code >= 500) {
		serverResponseException(status, message = "status $code")
	} else {
		clientRequestException(status, message = "status $code")
	}
}
