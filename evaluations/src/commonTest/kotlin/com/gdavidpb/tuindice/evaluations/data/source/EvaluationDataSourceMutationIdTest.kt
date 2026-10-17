package com.gdavidpb.tuindice.evaluations.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcome
import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcomeKind
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.evaluations.data.mutation.EVALUATIONS_MUTATION_SCOPE
import com.gdavidpb.tuindice.evaluations.data.mutation.EvaluationMutation
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock

class EvaluationDataSourceMutationIdTest {
	@Test
	fun addEvaluation_withAnExternalMutationId_usesItForTheRowTheRequestAndTheOutcome() = runTest {
		val store = FakeMutationEnvelopeStore<String, EvaluationMutation>()
		val api = FakeEvaluationsApiDataSource()
		val repository = dataSource(store = store, api = api, generatedId = "generated-id")
		val outcomes = collectOutcomes(repository)

		repository.addEvaluation(quizAdd(mutationId = "external-add"))

		assertEquals("external-add", store.getPendingMutations(EVALUATIONS_MUTATION_SCOPE).single().mutationId)

		runCurrent()

		assertEquals("external-add", api.addCalls.single().mutationId)
		assertEquals(listOf(MutationOutcome("external-add", MutationOutcomeKind.CONFIRMED)), outcomes)
	}

	@Test
	fun addEvaluation_withoutAnExternalMutationId_generatesOne() = runTest {
		val store = FakeMutationEnvelopeStore<String, EvaluationMutation>()
		val api = FakeEvaluationsApiDataSource()
		val repository = dataSource(store = store, api = api, generatedId = "generated-id")

		repository.addEvaluation(quizAdd(mutationId = null))

		assertEquals("generated-id", store.getPendingMutations(EVALUATIONS_MUTATION_SCOPE).single().mutationId)

		runCurrent()

		assertEquals("generated-id", api.addCalls.single().mutationId)
	}

	@Test
	fun updateEvaluation_withAnExternalMutationId_usesItForTheRowTheRequestAndTheOutcome() = runTest {
		val store = FakeMutationEnvelopeStore<String, EvaluationMutation>()
		val api = FakeEvaluationsApiDataSource()
		val repository = dataSource(store = store, api = api, generatedId = "generated-id")
		val outcomes = collectOutcomes(repository)

		repository.updateEvaluation(
			EvaluationUpdate(
				id = DEFAULT_LOCAL_PENDING_EVALUATION.id,
				scheduleMode = null,
				grade = 4.0,
				maxGrade = null,
				date = null,
				type = null,
				mutationId = "external-update"
			)
		)

		assertEquals("external-update", store.getPendingMutations(EVALUATIONS_MUTATION_SCOPE).single().mutationId)

		runCurrent()

		assertEquals("external-update", api.updateCalls.single().mutationId)
		assertEquals(listOf(MutationOutcome("external-update", MutationOutcomeKind.CONFIRMED)), outcomes)
	}

	@Test
	fun removeEvaluation_withAnExternalMutationId_usesItForTheRowTheRequestAndTheOutcome() = runTest {
		val store = FakeMutationEnvelopeStore<String, EvaluationMutation>()
		val api = FakeEvaluationsApiDataSource()
		val repository = dataSource(store = store, api = api, generatedId = "generated-id")
		val outcomes = collectOutcomes(repository)

		repository.removeEvaluation(
			EvaluationRemove(id = DEFAULT_LOCAL_PENDING_EVALUATION.id, mutationId = "external-remove")
		)

		assertEquals("external-remove", store.getPendingMutations(EVALUATIONS_MUTATION_SCOPE).single().mutationId)

		runCurrent()

		assertEquals("external-remove", api.removeCalls.single().mutationId)
		assertEquals(listOf(MutationOutcome("external-remove", MutationOutcomeKind.CONFIRMED)), outcomes)
	}

	@Test
	fun removeEvaluation_ofAPendingAdd_cancelsTheAddAndSettlesTheRemovalLocally() = runTest {
		val store = FakeMutationEnvelopeStore(
			initialPendingMutations = listOf(pendingAdd(mutationId = "add-1", referenceId = "reference-1"))
		)
		val api = FakeEvaluationsApiDataSource()
		val repository = dataSource(store = store, api = api, generatedId = "generated-id")
		val outcomes = collectOutcomes(repository)

		repository.removeEvaluation(EvaluationRemove(id = "reference-1", mutationId = "remove-1"))

		runCurrent()

		assertTrue(api.removeCalls.isEmpty())
		assertTrue(store.getMutations(EVALUATIONS_MUTATION_SCOPE).isEmpty())
		assertEquals(
			listOf(
				MutationOutcome("add-1", MutationOutcomeKind.CANCELLED),
				MutationOutcome("remove-1", MutationOutcomeKind.CONFIRMED)
			),
			outcomes
		)
	}

	@Test
	fun updateEvaluation_rewritingAPendingAdd_takesTheCallersIdAndSupersedesTheOldAdd() = runTest {
		val store = FakeMutationEnvelopeStore(
			initialPendingMutations = listOf(pendingAdd(mutationId = "add-1", referenceId = "reference-1"))
		)
		val api = FakeEvaluationsApiDataSource()
		val repository = dataSource(store = store, api = api, generatedId = "generated-id")
		val outcomes = collectOutcomes(repository)

		repository.updateEvaluation(
			EvaluationUpdate(
				id = "reference-1",
				scheduleMode = null,
				grade = 4.0,
				maxGrade = null,
				date = null,
				type = null,
				mutationId = "update-1"
			)
		)

		assertEquals("update-1", store.getPendingMutations(EVALUATIONS_MUTATION_SCOPE).single().mutationId)
		assertEquals(listOf(MutationOutcome("add-1", MutationOutcomeKind.SUPERSEDED)), outcomes)

		runCurrent()

		assertEquals("update-1", api.addCalls.single().mutationId)
		assertEquals(
			listOf(
				MutationOutcome("add-1", MutationOutcomeKind.SUPERSEDED),
				MutationOutcome("update-1", MutationOutcomeKind.CONFIRMED)
			),
			outcomes
		)
	}

	private fun TestScope.dataSource(
		store: FakeMutationEnvelopeStore<String, EvaluationMutation>,
		api: FakeEvaluationsApiDataSource,
		generatedId: String
	) = EvaluationDataSource(
		databaseDataSource = FakeDatabaseDataSource(),
		evaluationsApiDataSource = api,
		settingsDataSource = FakeSettingsDataSource(onCooldown = true),
		mutationEngine = createEvaluationsMutationEngine(store = store, coroutineScope = backgroundScope),
		identifierRepository = FakeIdentifierRepository(generatedId),
		clock = Clock.System
	)

	// Collects what the repository publishes from the moment the test starts, as the outcomes are settled.
	private fun TestScope.collectOutcomes(repository: EvaluationDataSource): MutableList<MutationOutcome> {
		val collected = mutableListOf<MutationOutcome>()

		backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
			repository.observeMutationOutcomesFlow().collect { outcome -> collected += outcome }
		}

		return collected
	}

	private fun quizAdd(mutationId: String?) = EvaluationAdd(
		reference = "reference-1",
		attemptId = DEFAULT_EVALUATION_SUBJECT.id,
		subjectCode = DEFAULT_EVALUATION_SUBJECT.code,
		termId = DEFAULT_EVALUATION_SUBJECT.termId,
		type = EvaluationType.QUIZ,
		scheduleMode = EvaluationScheduleMode.DATED,
		date = 1_900_000_000_000L,
		grade = null,
		maxGrade = 100.0,
		mutationId = mutationId
	)

	private fun pendingAdd(
		mutationId: String,
		referenceId: String
	): MutationEnvelope<String, EvaluationMutation> = MutationEnvelope(
		mutationId = mutationId,
		scopeKey = EVALUATIONS_MUTATION_SCOPE,
		command = EvaluationMutation.Add(
			referenceId = referenceId,
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
}
