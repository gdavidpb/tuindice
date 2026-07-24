package com.gdavidpb.tuindice.evaluations.data.repository

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluationsSnapshot
import com.gdavidpb.tuindice.evaluations.data.mutation.EVALUATIONS_MUTATION_SCOPE
import com.gdavidpb.tuindice.evaluations.data.mutation.EvaluationMutation
import com.gdavidpb.tuindice.evaluations.data.source.EvaluationDataSource
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_LOCAL_PENDING_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.FakeDatabaseDataSource
import com.gdavidpb.tuindice.evaluations.testing.FakeEvaluationsApiDataSource
import com.gdavidpb.tuindice.evaluations.testing.FakeIdentifierRepository
import com.gdavidpb.tuindice.evaluations.testing.FakeMutationEnvelopeStore
import com.gdavidpb.tuindice.evaluations.testing.FakeSettingsDataSource
import com.gdavidpb.tuindice.evaluations.testing.createEvaluationsMutationEngine
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationPrecondition
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class EvaluationRepositoryContractTest {
	@Test
	fun updateEvaluations_refreshesLocalCache_whenCooldownIsDisabled() = runTest {
		val databaseDataSource = FakeDatabaseDataSource()
		val evaluationsApiDataSource = FakeEvaluationsApiDataSource()
		val settingsDataSource = FakeSettingsDataSource(onCooldown = false)
		val repository = EvaluationDataSource(
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
	fun updateEvaluations_ignoresCooldown_whenEvaluationsHaveNeverSynced() = runTest {
		val databaseDataSource = FakeDatabaseDataSource(
			initialSnapshot = LocalEvaluationsSnapshot(
				hasSynced = false,
				evaluations = emptyList()
			)
		)
		val evaluationsApiDataSource = FakeEvaluationsApiDataSource()
		val settingsDataSource = FakeSettingsDataSource(onCooldown = true)
		val repository = EvaluationDataSource(
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
	fun updateEvaluations_respectsCooldown_whenEvaluationsHaveSynced() = runTest {
		val databaseDataSource = FakeDatabaseDataSource()
		val evaluationsApiDataSource = FakeEvaluationsApiDataSource()
		val settingsDataSource = FakeSettingsDataSource(onCooldown = true)
		val repository = EvaluationDataSource(
			databaseDataSource = databaseDataSource,
			evaluationsApiDataSource = evaluationsApiDataSource,
			settingsDataSource = settingsDataSource,
			mutationEngine = createEvaluationsMutationEngine(),
			identifierRepository = FakeIdentifierRepository()
		)

		repository.updateEvaluations()

		assertEquals(0, evaluationsApiDataSource.getEvaluationsCalls)
		assertTrue(databaseDataSource.savedSnapshots.isEmpty())
		assertEquals(false, settingsDataSource.cooldownMarked)
	}

	@Test
	fun updateEvaluations_forceRemote_ignoresCooldown_whenSyncedSnapshotIsEmpty() = runTest {
		val databaseDataSource = FakeDatabaseDataSource(
			initialSnapshot = LocalEvaluationsSnapshot(
				hasSynced = true,
				evaluations = emptyList()
			)
		)
		val evaluationsApiDataSource = FakeEvaluationsApiDataSource()
		val settingsDataSource = FakeSettingsDataSource(onCooldown = true)
		val repository = EvaluationDataSource(
			databaseDataSource = databaseDataSource,
			evaluationsApiDataSource = evaluationsApiDataSource,
			settingsDataSource = settingsDataSource,
			mutationEngine = createEvaluationsMutationEngine(),
			identifierRepository = FakeIdentifierRepository()
		)

		repository.updateEvaluations(forceRemote = true)

		assertEquals(1, evaluationsApiDataSource.getEvaluationsCalls)
		assertEquals(1, databaseDataSource.savedSnapshots.size)
		assertTrue(settingsDataSource.cooldownMarked)
	}

	@Test
	fun addEvaluation_enqueues_pending_add_locally_before_remote_ack() = runTest {
		val databaseDataSource = FakeDatabaseDataSource()
		val evaluationsApiDataSource = FakeEvaluationsApiDataSource()
		val pendingMutationStore = FakeMutationEnvelopeStore<String, EvaluationMutation>()
		val repository = EvaluationDataSource(
			databaseDataSource = databaseDataSource,
			evaluationsApiDataSource = evaluationsApiDataSource,
			settingsDataSource = FakeSettingsDataSource(onCooldown = true),
			mutationEngine = createEvaluationsMutationEngine(
				store = pendingMutationStore,
				coroutineScope = backgroundScope
			),
			identifierRepository = FakeIdentifierRepository("mutation-1")
		)

		repository.addEvaluation(
			EvaluationAdd(
				reference = "reference-1",
				attemptId = DEFAULT_EVALUATION_SUBJECT.id,
				subjectCode = DEFAULT_EVALUATION_SUBJECT.code,
				termId = DEFAULT_EVALUATION_SUBJECT.termId,
				type = EvaluationType.QUIZ,
				scheduleMode = EvaluationScheduleMode.DATED,
				date = 1_900_000_000_000L,
				grade = null,
				maxGrade = 100.0
			)
		)

		assertEquals(1, pendingMutationStore.getPendingMutations(EVALUATIONS_MUTATION_SCOPE).size)
		assertTrue(evaluationsApiDataSource.addCalls.isEmpty())
		assertTrue(databaseDataSource.addedEvaluations.isEmpty())
	}

	@Test
	fun updateEvaluation_rewrites_pending_add_locally_before_remote_ack() = runTest {
		val pendingMutationStore: FakeMutationEnvelopeStore<String, EvaluationMutation> = FakeMutationEnvelopeStore(
			initialPendingMutations = listOf(
				MutationEnvelope(
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
					createdAt = currentTimeMillis(),
					updatedAt = currentTimeMillis(),
					lastError = null
				)
			)
		)
		val evaluationsApiDataSource = FakeEvaluationsApiDataSource()
		val repository = EvaluationDataSource(
			databaseDataSource = FakeDatabaseDataSource(),
			evaluationsApiDataSource = evaluationsApiDataSource,
			settingsDataSource = FakeSettingsDataSource(onCooldown = true),
			mutationEngine = createEvaluationsMutationEngine(
				store = pendingMutationStore,
				coroutineScope = backgroundScope
			),
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

		val rewrittenPendingAdd = pendingMutationStore
			.getPendingMutations(EVALUATIONS_MUTATION_SCOPE)
			.single()
		assertTrue(rewrittenPendingAdd.command is EvaluationMutation.Add)
		assertEquals(0, evaluationsApiDataSource.addCalls.size)
		assertEquals(0, evaluationsApiDataSource.updateCalls.size)
		assertEquals(4.0, (rewrittenPendingAdd.command as EvaluationMutation.Add).grade)
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
					createdAt = currentTimeMillis(),
					updatedAt = currentTimeMillis(),
					lastError = null
				)
			)
		)
		val evaluationsApiDataSource = FakeEvaluationsApiDataSource()
		val repository = EvaluationDataSource(
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
	fun removeEvaluation_cancels_failed_add_without_remote_call() = runTest {
		val pendingMutationStore = failedAddStore()
		val evaluationsApiDataSource = FakeEvaluationsApiDataSource()
		val repository = EvaluationDataSource(
			databaseDataSource = FakeDatabaseDataSource(),
			evaluationsApiDataSource = evaluationsApiDataSource,
			settingsDataSource = FakeSettingsDataSource(onCooldown = true),
			mutationEngine = createEvaluationsMutationEngine(pendingMutationStore),
			identifierRepository = FakeIdentifierRepository("mutation-2")
		)

		repository.removeEvaluation(EvaluationRemove(id = "reference-1"))

		// Sin ver el alta fallida se construiría un `Remove` contra un id local que el
		// servidor no conoce, y el alta resucitaría en el siguiente reintento.
		assertEquals(0, evaluationsApiDataSource.removeCalls.size)
		assertTrue(pendingMutationStore.getMutations(EVALUATIONS_MUTATION_SCOPE).isEmpty())
	}

	@Test
	fun updateEvaluation_rewrites_failed_add_instead_of_enqueueing_an_update() = runTest {
		val pendingMutationStore = failedAddStore()
		val evaluationsApiDataSource = FakeEvaluationsApiDataSource()
		val repository = EvaluationDataSource(
			databaseDataSource = FakeDatabaseDataSource(),
			evaluationsApiDataSource = evaluationsApiDataSource,
			settingsDataSource = FakeSettingsDataSource(onCooldown = true),
			mutationEngine = createEvaluationsMutationEngine(
				store = pendingMutationStore,
				coroutineScope = backgroundScope
			),
			identifierRepository = FakeIdentifierRepository("mutation-2")
		)

		repository.updateEvaluation(
			EvaluationUpdate(
				id = "reference-1",
				scheduleMode = null,
				grade = 5.0,
				maxGrade = null,
				date = null,
				type = null
			)
		)

		// Mismo `replaceKey`: el sobre fallido se reemplaza, no se acumula junto a un
		// `Update` que apuntaría a una evaluación inexistente en el remoto.
		val rewritten = pendingMutationStore.getMutations(EVALUATIONS_MUTATION_SCOPE).single()
		val command = rewritten.command
		assertTrue(command is EvaluationMutation.Add)
		assertEquals("reference-1", command.referenceId)
		assertEquals(5.0, command.grade)
		assertEquals(PendingMutationStatus.Pending, rewritten.status)
	}

	private fun failedAddStore(): FakeMutationEnvelopeStore<String, EvaluationMutation> {
		return FakeMutationEnvelopeStore(
			initialPendingMutations = listOf(
				MutationEnvelope(
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
					status = PendingMutationStatus.Failed,
					createdAt = currentTimeMillis(),
					updatedAt = currentTimeMillis(),
					lastError = "terminal"
				)
			)
		)
	}

	@Test
	fun updateEvaluation_enqueues_pending_update_locally_before_remote_ack() = runTest {
		val evaluationsApiDataSource = FakeEvaluationsApiDataSource()
		val pendingMutationStore = FakeMutationEnvelopeStore<String, EvaluationMutation>()
		val repository = EvaluationDataSource(
			databaseDataSource = FakeDatabaseDataSource(),
			evaluationsApiDataSource = evaluationsApiDataSource,
			settingsDataSource = FakeSettingsDataSource(onCooldown = true),
			mutationEngine = createEvaluationsMutationEngine(
				store = pendingMutationStore,
				coroutineScope = backgroundScope
			),
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

		val pendingUpdate = pendingMutationStore
			.getPendingMutations(EVALUATIONS_MUTATION_SCOPE)
			.single()
		assertTrue(pendingUpdate.command is EvaluationMutation.Update)
		assertEquals(MutationPrecondition.Revision(3L), pendingUpdate.precondition)
		assertTrue(evaluationsApiDataSource.updateCalls.isEmpty())
	}

	@Test
	fun removeEvaluation_enqueues_pending_remove_locally_before_remote_ack() = runTest {
		val evaluationsApiDataSource = FakeEvaluationsApiDataSource()
		val pendingMutationStore = FakeMutationEnvelopeStore<String, EvaluationMutation>()
		val repository = EvaluationDataSource(
			databaseDataSource = FakeDatabaseDataSource(),
			evaluationsApiDataSource = evaluationsApiDataSource,
			settingsDataSource = FakeSettingsDataSource(onCooldown = true),
			mutationEngine = createEvaluationsMutationEngine(
				store = pendingMutationStore,
				coroutineScope = backgroundScope
			),
			identifierRepository = FakeIdentifierRepository("mutation-4")
		)

		repository.removeEvaluation(EvaluationRemove(id = "evaluation-1"))

		val pendingRemove = pendingMutationStore
			.getPendingMutations(EVALUATIONS_MUTATION_SCOPE)
			.single()
		assertTrue(pendingRemove.command is EvaluationMutation.Remove)
		assertEquals(MutationPrecondition.Revision(DEFAULT_LOCAL_PENDING_EVALUATION.revision), pendingRemove.precondition)
		assertTrue(evaluationsApiDataSource.removeCalls.isEmpty())
	}

	@Test
	fun removeEvaluation_whenRequestFailsOffline_keepsPendingRemoveQueued() = runTest {
		val pendingMutationStore = FakeMutationEnvelopeStore<String, EvaluationMutation>()
		val repository = EvaluationDataSource(
			databaseDataSource = FakeDatabaseDataSource(),
			evaluationsApiDataSource = FakeEvaluationsApiDataSource(
				removeThrowable = IllegalStateException("Could not connect to the server.")
			),
			settingsDataSource = FakeSettingsDataSource(onCooldown = true),
			mutationEngine = createEvaluationsMutationEngine(
				store = pendingMutationStore,
				coroutineScope = backgroundScope
			),
			identifierRepository = FakeIdentifierRepository("mutation-5")
		)

		repository.removeEvaluation(EvaluationRemove(id = "evaluation-1"))
		advanceUntilIdle()

		val pendingRemove = pendingMutationStore
			.getPendingMutations(EVALUATIONS_MUTATION_SCOPE)
			.single()
		assertTrue(pendingRemove.command is EvaluationMutation.Remove)
		assertEquals("mutation-5", pendingRemove.mutationId)
		assertEquals(PendingMutationStatus.Pending, pendingRemove.status)
	}
}
