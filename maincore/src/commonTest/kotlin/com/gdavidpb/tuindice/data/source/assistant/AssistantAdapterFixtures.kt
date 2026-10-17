package com.gdavidpb.tuindice.data.source.assistant

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationState
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcome
import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcomeKind
import com.gdavidpb.tuindice.base.domain.repository.IdentifierRepository
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationDisplayContext
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationTermDescriptor
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationsRefreshResult
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.pensum.domain.model.ObservedPensum
import com.gdavidpb.tuindice.pensum.domain.model.PensumGraph
import com.gdavidpb.tuindice.pensum.domain.model.PensumNodeType
import com.gdavidpb.tuindice.pensum.domain.model.PensumObservation
import com.gdavidpb.tuindice.pensum.domain.model.PensumSelection
import com.gdavidpb.tuindice.pensum.domain.repository.PensumRepository
import com.gdavidpb.tuindice.persistence.data.room.daos.PendingMutationDao
import com.gdavidpb.tuindice.persistence.data.room.entity.PendingMutationEntity
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermCreationCommand
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermUpdateCommand
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

// The answer a fake gives a mutation it receives: the outcome that settles it, or null when it never settles.
internal typealias OutcomeOf = (String) -> MutationOutcome?

internal fun storedEvaluation(id: String, grade: Double?) = Evaluation(
	id = id,
	attemptId = "attempt-1",
	subjectCode = "MAT101",
	termId = "term-1",
	scheduleMode = EvaluationScheduleMode.DATED,
	grade = grade,
	maxGrade = 5.0,
	date = 1_700_000_000_000L,
	type = EvaluationType.QUIZ,
	state = EvaluationState.COMPLETED
)

internal fun settledAs(kind: MutationOutcomeKind, detailCode: String? = null): OutcomeOf {
	return { mutationId -> MutationOutcome(mutationId, kind, detailCode) }
}

// Records what the evaluations use cases asked the repository to do, and whether an outcome subscriber was there
// when it did. The outcomes are hot, as the real engine's are: a mutation that settles with nobody subscribed is lost.
internal class FakeEvaluationRepository(
	private val outcomeOf: OutcomeOf = settledAs(MutationOutcomeKind.CONFIRMED),
	var failure: Throwable? = null
) : EvaluationRepository {
	private val outcomes = MutableSharedFlow<MutationOutcome>(extraBufferCapacity = 64)

	val mutations = mutableListOf<String>()
	val subscribedAtMutation = mutableListOf<Boolean>()
	val adds = mutableListOf<EvaluationAdd>()
	val updates = mutableListOf<EvaluationUpdate>()
	val removes = mutableListOf<EvaluationRemove>()

	// The evaluations the app holds, by id: e1, with a grade, unless a test says otherwise.
	val stored = mutableMapOf("e1" to storedEvaluation(id = "e1", grade = 4.0))

	private suspend fun settle(mutationId: String?) {
		val id = requireNotNull(mutationId) { "every mutation carries its id" }

		mutations += id
		subscribedAtMutation += outcomes.subscriptionCount.value > 0

		failure?.let { throw it }

		outcomeOf(id)?.let { outcome -> outcomes.emit(outcome) }
	}

	override suspend fun observeEvaluationsFlow(): Flow<List<Evaluation>> = flowOf(emptyList())

	override suspend fun observeHasSyncedEvaluationsFlow(): Flow<Boolean> = flowOf(true)

	override suspend fun updateEvaluations(): EvaluationsRefreshResult = TODO("not used by the assistant")

	override suspend fun drainPendingMutations() = Unit

	override suspend fun getEvaluation(eid: String): Evaluation? = stored[eid]

	override suspend fun addEvaluation(add: EvaluationAdd) {
		adds += add
		settle(add.mutationId)
	}

	override suspend fun updateEvaluation(update: EvaluationUpdate) {
		updates += update
		settle(update.mutationId)
	}

	override suspend fun removeEvaluation(remove: EvaluationRemove) {
		removes += remove
		settle(remove.mutationId)
	}

	override suspend fun getAvailableAttempts(): List<EditableAttemptDescriptor> = emptyList()

	override suspend fun getCurrentTerm(): EvaluationTermDescriptor? = null

	override fun observeDisplayContextFlow(): Flow<EvaluationDisplayContext> = emptyFlow()

	override fun observeMutationOutcomesFlow(): Flow<MutationOutcome> = outcomes
}

// The record: a term the tests can find, a revision the tests can move, and the record of what was asked of it.
internal class FakeRecordRepository(
	var record: AcademicRecord? = null,
	var revision: Long? = null,
	private val outcomeOf: OutcomeOf = settledAs(MutationOutcomeKind.CONFIRMED),
	var failure: Throwable? = null,
	var revisionFailure: Throwable? = null
) : AcademicRecordRepository {
	private val outcomes = MutableSharedFlow<MutationOutcome>(extraBufferCapacity = 64)

	val mutations = mutableListOf<String>()
	val subscribedAtMutation = mutableListOf<Boolean>()
	val addedTerms = mutableListOf<SyntheticTermCreationCommand>()
	val updatedTerms = mutableListOf<SyntheticTermUpdateCommand>()
	val deletedTermIds = mutableListOf<String>()
	val projections = mutableListOf<ProjectionCall>()

	private suspend fun settle(mutationId: String?) {
		val id = requireNotNull(mutationId) { "every mutation carries its id" }

		mutations += id
		subscribedAtMutation += outcomes.subscriptionCount.value > 0

		failure?.let { throw it }

		val outcome = outcomeOf(id) ?: return

		// The revision moves when a write lands, before its outcome reaches the subscriber.
		if (outcome.kind == MutationOutcomeKind.CONFIRMED) {
			revision = revision?.plus(1)
		}

		outcomes.emit(outcome)
	}

	override suspend fun observeAcademicRecordFlow(): Flow<AcademicRecord> = TODO("not used by the assistant")

	override suspend fun observeHasSyncedRecordFlow(): Flow<Boolean> = TODO("not used by the assistant")

	override suspend fun getAcademicRecord(): AcademicRecord? = record

	override suspend fun updateAcademicRecord() = Unit

	override suspend fun drainPendingMutations() = Unit

	override suspend fun upsertAttemptOverride(
		attemptId: String,
		score: AttemptScore?,
		outcome: AttemptOutcome?,
		mutationId: String?
	) {
		projections += ProjectionCall(attemptId, score, outcome, mutationId, deleted = false)
		settle(mutationId)
	}

	override suspend fun deleteAttemptOverride(attemptId: String, mutationId: String?) {
		projections += ProjectionCall(attemptId, score = null, outcome = null, mutationId = mutationId, deleted = true)
		settle(mutationId)
	}

	override suspend fun addSyntheticTerm(command: SyntheticTermCreationCommand) {
		addedTerms += command
		settle(command.mutationId)
	}

	override suspend fun updateSyntheticTerm(command: SyntheticTermUpdateCommand) {
		updatedTerms += command
		settle(command.mutationId)
	}

	override suspend fun deleteSyntheticTerm(termId: String, mutationId: String?) {
		deletedTermIds += termId
		settle(mutationId)
	}

	override suspend fun getRecordRevision(): Long? {
		revisionFailure?.let { throw it }

		return revision
	}

	override fun observeMutationOutcomesFlow(): Flow<MutationOutcome> = outcomes
}

internal data class ProjectionCall(
	val attemptId: String,
	val score: AttemptScore?,
	val outcome: AttemptOutcome?,
	val mutationId: String?,
	val deleted: Boolean
)

internal class FakePensumRepository(
	private val observation: Flow<PensumObservation>
) : PensumRepository {
	override fun observePensumFlow(): Flow<PensumObservation> = observation

	override suspend fun hasSelectedPensumResponse(): Boolean = TODO("not used by the assistant")

	override suspend fun refreshPensum(forceRemote: Boolean) = Unit

	override suspend fun selectSelection(year: Int, modalityId: String) = Unit
}

internal class FakeIdentifierRepository : IdentifierRepository {
	override fun generateRandomIdentifier(): String = "generated-reference"
}

// The outbox, as the client state reads it: the rows of one store and scope are the ones it returns.
internal class FakePendingMutationDao(
	private val mutations: List<PendingMutationEntity> = emptyList()
) : PendingMutationDao() {
	override suspend fun upsertEntity(entity: PendingMutationEntity) = TODO("not used by the assistant")

	override suspend fun upsertEntities(entities: List<PendingMutationEntity>) = TODO("not used by the assistant")

	override fun observePendingMutations(storeId: String, scopeKey: String): Flow<List<PendingMutationEntity>> {
		TODO("not used by the assistant")
	}

	override suspend fun getPendingMutations(storeId: String, scopeKey: String): List<PendingMutationEntity> {
		TODO("not used by the assistant")
	}

	override fun observeMutations(storeId: String, scopeKey: String): Flow<List<PendingMutationEntity>> {
		TODO("not used by the assistant")
	}

	override suspend fun getMutations(storeId: String, scopeKey: String): List<PendingMutationEntity> {
		return mutations.filter { mutation -> mutation.storeId == storeId && mutation.scopeKey == scopeKey }
	}

	override suspend fun getPendingMutation(
		storeId: String,
		scopeKey: String,
		mutationId: String
	): PendingMutationEntity? {
		TODO("not used by the assistant")
	}

	override suspend fun getPendingMutation(mutationId: String): PendingMutationEntity? {
		TODO("not used by the assistant")
	}

	override suspend fun deletePendingMutation(storeId: String, scopeKey: String, mutationId: String): Int {
		TODO("not used by the assistant")
	}

	override suspend fun deletePendingMutationsByReplaceKey(storeId: String, replaceKey: String): Int {
		TODO("not used by the assistant")
	}

	override suspend fun requeueMutations(
		storeId: String,
		scopeKey: String,
		requeueFrom: String,
		retryableBefore: Long,
		updatedAt: Long
	): Int {
		TODO("not used by the assistant")
	}

	override suspend fun deleteAll(): Int = TODO("not used by the assistant")
}

internal fun pendingMutation(mutationId: String, storeId: String, scopeKey: String, status: String) =
	PendingMutationEntity(
		mutationId = mutationId,
		storeId = storeId,
		scopeKey = scopeKey,
		entityType = "entity",
		entityId = "entity-$mutationId",
		replaceKey = "replace-$mutationId",
		payload = "{}",
		preconditionType = "none",
		expectedRevision = 0L,
		status = status,
		createdAt = 0L,
		updatedAt = 0L,
		lastError = null
	)

// The pensum with its courses, as the catalog reads it: (code, name, credits) for each course.
internal fun pensumWith(vararg courses: Triple<String, String, Int>): PensumObservation {
	val nodes = courses.map { (code, name, credits) ->
		PensumGraph.Node(
			id = "node-$code",
			nodeType = PensumNodeType.COURSE,
			displayCode = code,
			subjectCode = code,
			name = name,
			credits = credits,
			category = "mandatory",
			termId = "term-1",
			x = 0.0,
			y = 0.0,
			width = 0.0,
			height = 0.0,
			fulfillmentRules = emptyList()
		)
	}

	return PensumObservation.Content(
		pensum = ObservedPensum(
			careerName = "Ingeniería",
			selection = PensumSelection(
				pensumId = "pensum-1",
				year = 2020,
				modalityId = "regular",
				modalityName = "Regular",
				inferred = false
			),
			availablePensums = emptyList(),
			availableModalities = emptyList(),
			pensum = PensumGraph(
				id = "pensum-1",
				year = 2020,
				modalityId = "regular",
				modalityName = "Regular",
				totalCredits = 0,
				canvas = PensumGraph.Canvas(width = 0.0, height = 0.0),
				terms = emptyList(),
				nodes = nodes,
				edges = emptyList()
			),
			pensums = emptyList(),
			approvedCredits = 0,
			nodeStatuses = emptyMap(),
			nodeFulfillments = emptyMap()
		)
	)
}

// A synthetic term of the record: its own id (not its key), its period, and its attempts.
internal fun syntheticTerm(
	id: String = "term-1",
	periodYear: Int = 2099,
	periodCode: AcademicTermPeriod = AcademicTermPeriod.APR_JUL,
	attempts: List<AcademicAttempt> = emptyList()
) = AcademicTerm(
	id = id,
	periodYear = periodYear,
	periodCode = periodCode,
	kind = TermKind.SYNTHETIC,
	attempts = attempts
)

internal fun attempt(id: String, subjectCode: String, subjectName: String, credits: Int) = AcademicAttempt(
	id = id,
	subjectCode = subjectCode,
	subjectName = subjectName,
	credits = credits
)

internal fun recordWith(vararg terms: AcademicTerm) = AcademicRecord(id = "record-1", terms = terms.toList())
