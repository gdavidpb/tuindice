package com.gdavidpb.tuindice.evaluations.data.repository

import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.evaluations.data.repository.mutation.EVALUATIONS_MUTATION_SCOPE
import com.gdavidpb.tuindice.evaluations.data.repository.mutation.EvaluationMutation
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATIONS_ANCHOR_REVISION
import com.gdavidpb.tuindice.evaluations.testing.FakeDatabaseDataSource
import com.gdavidpb.tuindice.evaluations.testing.FakeEvaluationsApiDataSource
import com.gdavidpb.tuindice.evaluations.testing.FakeIdentifierRepository
import com.gdavidpb.tuindice.evaluations.testing.FakeMutationEnvelopeStore
import com.gdavidpb.tuindice.evaluations.testing.FakeSettingsDataSource
import com.gdavidpb.tuindice.evaluations.testing.createEvaluationsMutationEngine
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationPrecondition
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvaluationRepositoryContractTest {
	@Test
	fun updateEvaluations_refreshesLocalCache_whenCooldownIsDisabled() = runTest {
		val databaseDataSource = FakeDatabaseDataSource()
		val evaluationsApiDataSource = FakeEvaluationsApiDataSource()
		val settingsDataSource = FakeSettingsDataSource(onCooldown = false)
		val repository = EvaluationDataRepository(
			databaseDataSource = databaseDataSource,
			evaluationsApiDataSource = evaluationsApiDataSource,
			settingsDataSource = settingsDataSource,
			mutationEngine = createEvaluationsMutationEngine(),
			identifierRepository = FakeIdentifierRepository()
		)

		repository.updateEvaluations()

		assertEquals(1, evaluationsApiDataSource.getEvaluationsCalls)
		assertEquals(1, databaseDataSource.savedSnapshots.size)
		assertTrue(settingsDataSource.cooldownMarked)
	}

	@Test
	fun addEvaluation_sends_reference_id_with_anchor_revision_and_confirms_real_id() = runTest {
		val databaseDataSource = FakeDatabaseDataSource()
		val evaluationsApiDataSource = FakeEvaluationsApiDataSource()
		val repository = EvaluationDataRepository(
			databaseDataSource = databaseDataSource,
			evaluationsApiDataSource = evaluationsApiDataSource,
			settingsDataSource = FakeSettingsDataSource(onCooldown = true),
			mutationEngine = createEvaluationsMutationEngine(),
			identifierRepository = FakeIdentifierRepository("mutation-1")
		)

		repository.addEvaluation(
			EvaluationAdd(
				reference = "reference-1",
				subjectId = DEFAULT_EVALUATION_SUBJECT.id,
				subjectCode = DEFAULT_EVALUATION_SUBJECT.code,
				quarterId = DEFAULT_EVALUATION_SUBJECT.quarterId,
				type = EvaluationType.QUIZ,
				scheduleMode = EvaluationScheduleMode.DATED,
				date = 1_900_000_000_000L,
				grade = null,
				maxGrade = 100.0
			)
		)

		assertEquals("reference-1", evaluationsApiDataSource.addCalls.single().add.referenceId)
		assertEquals(DEFAULT_EVALUATIONS_ANCHOR_REVISION, evaluationsApiDataSource.addCalls.single().expectedRevision)
		assertEquals("real-reference-1", databaseDataSource.addedEvaluations.single().second.id)
		assertEquals("reference-1", databaseDataSource.addedEvaluations.single().second.referenceId)
	}

	@Test
	fun updateEvaluation_rewrites_pending_add_instead_of_sending_remote_update() = runTest {
		val pendingMutationStore: FakeMutationEnvelopeStore<String, EvaluationMutation> = FakeMutationEnvelopeStore(
			initialPendingMutations = listOf(
				MutationEnvelope(
					mutationId = "mutation-1",
					scopeKey = EVALUATIONS_MUTATION_SCOPE,
					command = EvaluationMutation.Add(
						referenceId = "reference-1",
						subjectId = DEFAULT_EVALUATION_SUBJECT.id,
						subjectCode = DEFAULT_EVALUATION_SUBJECT.code,
						quarterId = DEFAULT_EVALUATION_SUBJECT.quarterId,
						scheduleMode = EvaluationScheduleMode.DATED,
						grade = null,
						maxGrade = 100.0,
						date = 1_900_000_000_000L,
						type = EvaluationType.QUIZ.ordinal
					),
					precondition = MutationPrecondition.Revision(DEFAULT_EVALUATIONS_ANCHOR_REVISION),
					status = PendingMutationStatus.Pending,
					createdAt = currentTimeMillis(),
					updatedAt = currentTimeMillis(),
					lastError = null
				)
			)
		)
		val evaluationsApiDataSource = FakeEvaluationsApiDataSource()
		val repository = EvaluationDataRepository(
			databaseDataSource = FakeDatabaseDataSource(),
			evaluationsApiDataSource = evaluationsApiDataSource,
			settingsDataSource = FakeSettingsDataSource(onCooldown = true),
			mutationEngine = createEvaluationsMutationEngine(pendingMutationStore),
			identifierRepository = FakeIdentifierRepository("mutation-2")
		)

		repository.updateEvaluation(
			EvaluationUpdate(
				id = "reference-1",
				scheduleMode = null,
				grade = 4.0,
				maxGrade = null,
				date = null,
				type = null
			)
		)

		assertEquals(1, evaluationsApiDataSource.addCalls.size)
		assertEquals(0, evaluationsApiDataSource.updateCalls.size)
		assertEquals(4.0, evaluationsApiDataSource.addCalls.single().add.grade)
	}

	@Test
	fun removeEvaluation_cancels_pending_add_without_remote_call() = runTest {
		val pendingMutationStore: FakeMutationEnvelopeStore<String, EvaluationMutation> = FakeMutationEnvelopeStore(
			initialPendingMutations = listOf(
				MutationEnvelope(
					mutationId = "mutation-1",
					scopeKey = EVALUATIONS_MUTATION_SCOPE,
					command = EvaluationMutation.Add(
						referenceId = "reference-1",
						subjectId = DEFAULT_EVALUATION_SUBJECT.id,
						subjectCode = DEFAULT_EVALUATION_SUBJECT.code,
						quarterId = DEFAULT_EVALUATION_SUBJECT.quarterId,
						scheduleMode = EvaluationScheduleMode.DATED,
						grade = null,
						maxGrade = 100.0,
						date = 1_900_000_000_000L,
						type = EvaluationType.QUIZ.ordinal
					),
					precondition = MutationPrecondition.Revision(DEFAULT_EVALUATIONS_ANCHOR_REVISION),
					status = PendingMutationStatus.Pending,
					createdAt = currentTimeMillis(),
					updatedAt = currentTimeMillis(),
					lastError = null
				)
			)
		)
		val evaluationsApiDataSource = FakeEvaluationsApiDataSource()
		val repository = EvaluationDataRepository(
			databaseDataSource = FakeDatabaseDataSource(),
			evaluationsApiDataSource = evaluationsApiDataSource,
			settingsDataSource = FakeSettingsDataSource(onCooldown = true),
			mutationEngine = createEvaluationsMutationEngine(pendingMutationStore),
			identifierRepository = FakeIdentifierRepository("mutation-2")
		)

		repository.removeEvaluation(EvaluationRemove(id = "reference-1"))

		assertEquals(0, evaluationsApiDataSource.removeCalls.size)
		assertTrue(pendingMutationStore.getPendingMutations(EVALUATIONS_MUTATION_SCOPE).isEmpty())
	}

	@Test
	fun updateEvaluation_sends_confirmed_revision_for_remote_patch() = runTest {
		val evaluationsApiDataSource = FakeEvaluationsApiDataSource()
		val repository = EvaluationDataRepository(
			databaseDataSource = FakeDatabaseDataSource(),
			evaluationsApiDataSource = evaluationsApiDataSource,
			settingsDataSource = FakeSettingsDataSource(onCooldown = true),
			mutationEngine = createEvaluationsMutationEngine(),
			identifierRepository = FakeIdentifierRepository("mutation-3")
		)

		repository.updateEvaluation(
			EvaluationUpdate(
				id = "evaluation-1",
				scheduleMode = null,
				grade = 5.0,
				maxGrade = null,
				date = null,
				type = null
			)
		)

		assertEquals(3L, evaluationsApiDataSource.updateCalls.single().expectedRevision)
	}

	@Test
	fun removeEvaluation_sends_anchor_revision_for_remote_delete() = runTest {
		val evaluationsApiDataSource = FakeEvaluationsApiDataSource()
		val repository = EvaluationDataRepository(
			databaseDataSource = FakeDatabaseDataSource(),
			evaluationsApiDataSource = evaluationsApiDataSource,
			settingsDataSource = FakeSettingsDataSource(onCooldown = true),
			mutationEngine = createEvaluationsMutationEngine(),
			identifierRepository = FakeIdentifierRepository("mutation-4")
		)

		repository.removeEvaluation(EvaluationRemove(id = "evaluation-1"))

		assertEquals(DEFAULT_EVALUATIONS_ANCHOR_REVISION, evaluationsApiDataSource.removeCalls.single().expectedRevision)
	}
}
