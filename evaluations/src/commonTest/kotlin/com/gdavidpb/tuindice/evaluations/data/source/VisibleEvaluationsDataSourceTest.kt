@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.evaluations.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.domain.model.ObservedSyncedSnapshot
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluationsSnapshot
import com.gdavidpb.tuindice.evaluations.data.mutation.EVALUATIONS_MUTATION_SCOPE
import com.gdavidpb.tuindice.evaluations.data.mutation.EvaluationMutation
import com.gdavidpb.tuindice.evaluations.data.mutation.EvaluationMutationAck
import com.gdavidpb.tuindice.evaluations.data.repository.DatabaseDataRepository
import com.gdavidpb.tuindice.evaluations.domain.mapper.listedUnder
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_COMPLETED_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_LOCAL_COMPLETED_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_LOCAL_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_LOCAL_PENDING_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_PENDING_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.FakeDatabaseDataSource
import com.gdavidpb.tuindice.evaluations.testing.FakeEvaluationsApiDataSource
import com.gdavidpb.tuindice.evaluations.testing.FakeIdentifierRepository
import com.gdavidpb.tuindice.evaluations.testing.FakeMutationEnvelopeStore
import com.gdavidpb.tuindice.evaluations.testing.FakeSettingsDataSource
import com.gdavidpb.tuindice.evaluations.testing.QueueMergingDatabaseDataSource
import com.gdavidpb.tuindice.evaluations.testing.RecordingEvaluationRepository
import com.gdavidpb.tuindice.evaluations.testing.createEvaluationsMutationEngine
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationPrecondition
import com.gdavidpb.tuindice.persistence.domain.mutation.StoreBackedMutationEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class VisibleEvaluationsDataSourceTest {
	@Test
	fun observeVisibleEvaluationsFlow_deliversWhatTheTabObserves_withSyncedFlag() = runTest {
		val tabRepository = RecordingEvaluationRepository(
			initialEvaluations = listOf(DEFAULT_PENDING_EVALUATION, DEFAULT_COMPLETED_EVALUATION),
			hasSyncedEvaluationsFlow = kotlinx.coroutines.flow.flowOf(true)
		)
		val source = VisibleEvaluationsDataSource(evaluationRepository = tabRepository)

		val visible = source.observeVisibleEvaluationsFlow().first()

		assertEquals(tabRepository.observeEvaluationsSnapshotFlow().first(), visible)
		assertEquals(
			ObservedSyncedSnapshot(
				value = listOf(DEFAULT_PENDING_EVALUATION, DEFAULT_COMPLETED_EVALUATION),
				hasSynced = true
			),
			visible
		)
	}

	@Test
	fun observeVisibleEvaluationsFlow_tellsNotDownloadedYetApartFromNone() = runTest {
		val notDownloaded = VisibleEvaluationsDataSource(
			RecordingEvaluationRepository(
				initialEvaluations = emptyList(),
				hasSyncedEvaluationsFlow = kotlinx.coroutines.flow.flowOf(false)
			)
		)
		val downloadedAndEmpty = VisibleEvaluationsDataSource(
			RecordingEvaluationRepository(
				initialEvaluations = emptyList(),
				hasSyncedEvaluationsFlow = kotlinx.coroutines.flow.flowOf(true)
			)
		)

		assertEquals(
			ObservedSyncedSnapshot(value = emptyList(), hasSynced = false),
			notDownloaded.observeVisibleEvaluationsFlow().first()
		)
		assertEquals(
			ObservedSyncedSnapshot(value = emptyList(), hasSynced = true),
			downloadedAndEmpty.observeVisibleEvaluationsFlow().first()
		)
	}

	@Test
	fun observeVisibleEvaluationsFlow_includesACreationPendingInTheMutationQueue() = runTest {
		val queue = FakeMutationEnvelopeStore<String, EvaluationMutation>()
		val mutationEngine = createEvaluationsMutationEngine(store = queue)
		val repository = realRepository(
			database = QueueMergingDatabaseDataSource(
				confirmed = FakeDatabaseDataSource(
					initialSnapshot = LocalEvaluationsSnapshot(
						hasSynced = true,
						evaluations = listOf(DEFAULT_LOCAL_COMPLETED_EVALUATION)
					)
				),
				mutationEngine = mutationEngine
			),
			mutationEngine = mutationEngine
		)
		// Only in the queue: the creation has not been confirmed, so no confirmed row has it.
		queue.savePendingMutation(
			pendingCreation(referenceId = "pending-creation", attemptId = DEFAULT_LOCAL_EVALUATION_SUBJECT.id)
		)
		val source = VisibleEvaluationsDataSource(evaluationRepository = repository)

		val visible = source.observeVisibleEvaluationsFlow().first()

		assertEquals(
			setOf(DEFAULT_LOCAL_COMPLETED_EVALUATION.id, "pending-creation"),
			visible.value.map { evaluation -> evaluation.id }.toSet()
		)
		assertTrue(visible.hasSynced)
	}

	@Test
	fun observeVisibleEvaluationsFlow_leavesOutTheEvaluationsOfAnAttemptThatIsNotAvailable() = runTest {
		val queue = FakeMutationEnvelopeStore<String, EvaluationMutation>()
		val mutationEngine = createEvaluationsMutationEngine(store = queue)
		val repository = realRepository(
			database = QueueMergingDatabaseDataSource(
				confirmed = FakeDatabaseDataSource(
					initialSnapshot = LocalEvaluationsSnapshot(
						hasSynced = true,
						evaluations = listOf(
							DEFAULT_LOCAL_PENDING_EVALUATION,
							DEFAULT_LOCAL_COMPLETED_EVALUATION.copy(
								id = "orphan-confirmed",
								referenceId = "orphan-confirmed",
								attemptId = "attempt-that-is-gone"
							)
						)
					)
				),
				mutationEngine = mutationEngine
			),
			mutationEngine = mutationEngine
		)
		queue.savePendingMutation(
			pendingCreation(referenceId = "orphan-pending", attemptId = "attempt-that-is-gone")
		)
		val source = VisibleEvaluationsDataSource(evaluationRepository = repository)

		val visible = source.observeVisibleEvaluationsFlow().first()

		// The tab would not list them, so the contract does not deliver them either.
		assertEquals(listOf(DEFAULT_LOCAL_PENDING_EVALUATION.id), visible.value.map { evaluation -> evaluation.id })
		assertEquals(
			listOf("orphan-confirmed", "orphan-pending", DEFAULT_LOCAL_PENDING_EVALUATION.id).sorted(),
			repository.getEvaluationsSnapshot().value.map { evaluation -> evaluation.id }.sorted()
		)
	}

	@Test
	fun observeVisibleEvaluationsFlow_deliversWhatTheTabListsAfterItsOwnFilter() = runTest {
		val tabRepository = RecordingEvaluationRepository(
			initialEvaluations = listOf(
				DEFAULT_PENDING_EVALUATION,
				DEFAULT_COMPLETED_EVALUATION.copy(attemptId = "attempt-that-is-gone")
			),
			availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT)
		)
		val source = VisibleEvaluationsDataSource(evaluationRepository = tabRepository)

		val visible = source.observeVisibleEvaluationsFlow().first()

		assertEquals(
			tabRepository.observeEvaluationsSnapshotFlow().first().value
				.listedUnder(tabRepository.getAvailableAttempts()),
			visible.value
		)
	}

	private fun realRepository(
		database: DatabaseDataRepository,
		mutationEngine: StoreBackedMutationEngine<String, EvaluationMutation, EvaluationMutationAck>
	) = EvaluationDataSource(
		databaseDataSource = database,
		evaluationsApiDataSource = FakeEvaluationsApiDataSource(),
		settingsDataSource = FakeSettingsDataSource(onCooldown = true),
		mutationEngine = mutationEngine,
		identifierRepository = FakeIdentifierRepository(),
		clock = Clock.System
	)

	private fun pendingCreation(
		referenceId: String,
		attemptId: String
	): MutationEnvelope<String, EvaluationMutation> = MutationEnvelope(
		mutationId = "mutation-$referenceId",
		scopeKey = EVALUATIONS_MUTATION_SCOPE,
		command = EvaluationMutation.Add(
			referenceId = referenceId,
			attemptId = attemptId,
			subjectCode = DEFAULT_LOCAL_EVALUATION_SUBJECT.code,
			termId = DEFAULT_LOCAL_EVALUATION_SUBJECT.termId,
			scheduleMode = EvaluationScheduleMode.DATED,
			grade = null,
			maxGrade = 20.0,
			date = DEFAULT_PENDING_EVALUATION.date,
			type = 0
		),
		precondition = MutationPrecondition.None,
		status = PendingMutationStatus.Pending,
		createdAt = 1L,
		updatedAt = 1L,
		lastError = null
	)

	@Test
	fun observeVisibleEvaluationsFlow_emitsAgain_whenTheEvaluationsChange() = runTest {
		val evaluations = MutableStateFlow(listOf(DEFAULT_PENDING_EVALUATION))
		val source = VisibleEvaluationsDataSource(
			RecordingEvaluationRepository(evaluationsFlow = evaluations)
		)
		val emitted = mutableListOf<List<String>>()

		backgroundScope.launch {
			source.observeVisibleEvaluationsFlow().collect { snapshot ->
				emitted += snapshot.value.map { evaluation -> evaluation.id }
			}
		}
		runCurrent()
		evaluations.value = listOf(DEFAULT_PENDING_EVALUATION, DEFAULT_COMPLETED_EVALUATION)
		runCurrent()

		assertEquals(
			listOf(
				listOf(DEFAULT_PENDING_EVALUATION.id),
				listOf(DEFAULT_PENDING_EVALUATION.id, DEFAULT_COMPLETED_EVALUATION.id)
			),
			emitted
		)
	}
}
