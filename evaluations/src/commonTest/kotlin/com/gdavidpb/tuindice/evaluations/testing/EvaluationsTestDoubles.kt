package com.gdavidpb.tuindice.evaluations.testing

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.domain.repository.IdentifierRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluation
import com.gdavidpb.tuindice.evaluations.data.model.LocalSubject
import com.gdavidpb.tuindice.evaluations.data.model.RemoteEvaluation
import com.gdavidpb.tuindice.evaluations.data.repository.DatabaseDataSource
import com.gdavidpb.tuindice.evaluations.data.repository.EvaluationsApiDataSource
import com.gdavidpb.tuindice.evaluations.data.repository.SettingsDataSource
import com.gdavidpb.tuindice.evaluations.domain.mapper.toEvaluation
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.presentation.resource.EvaluationFilterLabelsProvider
import com.gdavidpb.tuindice.evaluations.presentation.resource.EvaluationTextProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

private const val PAST_EVALUATION_DATE = 1_700_000_000_000L
private const val FUTURE_EVALUATION_DATE = 1_900_000_000_000L

val DEFAULT_EVALUATION_SUBJECT = Subject(
	id = "subject-1",
	quarterId = "quarter-1",
	code = "INF-101",
	name = "Programacion",
	credits = 6,
	grade = 70
)

val SECOND_EVALUATION_SUBJECT = Subject(
	id = "subject-2",
	quarterId = "quarter-1",
	code = "MAT-101",
	name = "Calculo",
	credits = 6,
	grade = 65
)

val DEFAULT_LOCAL_EVALUATION_SUBJECT = LocalSubject(
	id = DEFAULT_EVALUATION_SUBJECT.id,
	quarterId = DEFAULT_EVALUATION_SUBJECT.quarterId,
	code = DEFAULT_EVALUATION_SUBJECT.code,
	name = DEFAULT_EVALUATION_SUBJECT.name,
	credits = DEFAULT_EVALUATION_SUBJECT.credits,
	grade = DEFAULT_EVALUATION_SUBJECT.grade
)

val SECOND_LOCAL_EVALUATION_SUBJECT = LocalSubject(
	id = SECOND_EVALUATION_SUBJECT.id,
	quarterId = SECOND_EVALUATION_SUBJECT.quarterId,
	code = SECOND_EVALUATION_SUBJECT.code,
	name = SECOND_EVALUATION_SUBJECT.name,
	credits = SECOND_EVALUATION_SUBJECT.credits,
	grade = SECOND_EVALUATION_SUBJECT.grade
)

val DEFAULT_PENDING_EVALUATION = Evaluation(
	id = "evaluation-1",
	subjectId = DEFAULT_EVALUATION_SUBJECT.id,
	subjectCode = DEFAULT_EVALUATION_SUBJECT.code,
	quarterId = DEFAULT_EVALUATION_SUBJECT.quarterId,
	grade = null,
	maxGrade = 100.0,
	date = FUTURE_EVALUATION_DATE,
	type = EvaluationType.QUIZ,
	state = EvaluationState.PENDING
)

val DEFAULT_COMPLETED_EVALUATION = Evaluation(
	id = "evaluation-2",
	subjectId = SECOND_EVALUATION_SUBJECT.id,
	subjectCode = SECOND_EVALUATION_SUBJECT.code,
	quarterId = SECOND_EVALUATION_SUBJECT.quarterId,
	grade = 82.0,
	maxGrade = 100.0,
	date = PAST_EVALUATION_DATE,
	type = EvaluationType.TEST,
	state = EvaluationState.COMPLETED
)

val DEFAULT_LOCAL_PENDING_EVALUATION = LocalEvaluation(
	id = DEFAULT_PENDING_EVALUATION.id,
	subjectId = DEFAULT_PENDING_EVALUATION.subjectId,
	subjectCode = DEFAULT_PENDING_EVALUATION.subjectCode,
	quarterId = DEFAULT_PENDING_EVALUATION.quarterId,
	grade = DEFAULT_PENDING_EVALUATION.grade,
	maxGrade = DEFAULT_PENDING_EVALUATION.maxGrade,
	date = DEFAULT_PENDING_EVALUATION.date,
	type = DEFAULT_PENDING_EVALUATION.type.ordinal,
	isDone = false
)

val DEFAULT_LOCAL_COMPLETED_EVALUATION = LocalEvaluation(
	id = DEFAULT_COMPLETED_EVALUATION.id,
	subjectId = DEFAULT_COMPLETED_EVALUATION.subjectId,
	subjectCode = DEFAULT_COMPLETED_EVALUATION.subjectCode,
	quarterId = DEFAULT_COMPLETED_EVALUATION.quarterId,
	grade = DEFAULT_COMPLETED_EVALUATION.grade,
	maxGrade = DEFAULT_COMPLETED_EVALUATION.maxGrade,
	date = DEFAULT_COMPLETED_EVALUATION.date,
	type = DEFAULT_COMPLETED_EVALUATION.type.ordinal,
	isDone = true
)

val DEFAULT_REMOTE_PENDING_EVALUATION = RemoteEvaluation(
	id = DEFAULT_PENDING_EVALUATION.id,
	subjectId = DEFAULT_PENDING_EVALUATION.subjectId,
	subjectCode = DEFAULT_PENDING_EVALUATION.subjectCode,
	quarterId = DEFAULT_PENDING_EVALUATION.quarterId,
	grade = DEFAULT_PENDING_EVALUATION.grade,
	maxGrade = DEFAULT_PENDING_EVALUATION.maxGrade,
	date = DEFAULT_PENDING_EVALUATION.date,
	type = DEFAULT_PENDING_EVALUATION.type.ordinal,
	isDone = false
)

val DEFAULT_REMOTE_COMPLETED_EVALUATION = RemoteEvaluation(
	id = DEFAULT_COMPLETED_EVALUATION.id,
	subjectId = DEFAULT_COMPLETED_EVALUATION.subjectId,
	subjectCode = DEFAULT_COMPLETED_EVALUATION.subjectCode,
	quarterId = DEFAULT_COMPLETED_EVALUATION.quarterId,
	grade = DEFAULT_COMPLETED_EVALUATION.grade,
	maxGrade = DEFAULT_COMPLETED_EVALUATION.maxGrade,
	date = DEFAULT_COMPLETED_EVALUATION.date,
	type = DEFAULT_COMPLETED_EVALUATION.type.ordinal,
	isDone = true
)

class RecordingEvaluationRepository(
	initialEvaluations: List<Evaluation> = listOf(
		DEFAULT_PENDING_EVALUATION,
		DEFAULT_COMPLETED_EVALUATION
	),
	private val availableSubjects: List<Subject> = listOf(
		DEFAULT_EVALUATION_SUBJECT,
		SECOND_EVALUATION_SUBJECT
	)
) : EvaluationRepository {
	private val evaluationsState = MutableStateFlow(initialEvaluations)

	val addCalls = mutableListOf<EvaluationAdd>()
	val updateCalls = mutableListOf<EvaluationUpdate>()
	val removeCalls = mutableListOf<EvaluationRemove>()

	override suspend fun getEvaluationsFlow(): Flow<List<Evaluation>> = evaluationsState

	override suspend fun getEvaluation(eid: String): Evaluation? {
		return evaluationsState.value.firstOrNull { evaluation -> evaluation.id == eid }
	}

	override suspend fun addEvaluation(add: EvaluationAdd) {
		addCalls += add
		evaluationsState.value = evaluationsState.value + add.toEvaluation()
	}

	override suspend fun updateEvaluation(update: EvaluationUpdate) {
		updateCalls += update
		evaluationsState.value = evaluationsState.value.map { evaluation ->
			if (evaluation.id == update.id)
				evaluation.copy(
					grade = update.grade,
					maxGrade = update.maxGrade ?: evaluation.maxGrade,
					date = update.date ?: evaluation.date,
					type = update.type ?: evaluation.type
				)
			else
				evaluation
		}
	}

	override suspend fun removeEvaluation(remove: EvaluationRemove) {
		removeCalls += remove
		evaluationsState.value = evaluationsState.value.filterNot { evaluation ->
			evaluation.id == remove.id
		}
	}

	override suspend fun getAvailableSubjects(): List<Subject> = availableSubjects
}

class FakeDatabaseDataSource(
	initialEvaluations: List<LocalEvaluation> = listOf(
		DEFAULT_LOCAL_PENDING_EVALUATION,
		DEFAULT_LOCAL_COMPLETED_EVALUATION
	),
	private val availableSubjects: List<LocalSubject> = listOf(
		DEFAULT_LOCAL_EVALUATION_SUBJECT,
		SECOND_LOCAL_EVALUATION_SUBJECT
	)
) : DatabaseDataSource {
	private val evaluationsState = MutableStateFlow(initialEvaluations)

	val savedEvaluations = mutableListOf<List<LocalEvaluation>>()
	val addedEvaluations = mutableListOf<LocalEvaluation>()
	val updatedEvaluations = mutableListOf<LocalEvaluation>()
	val removedEvaluationIds = mutableListOf<String>()

	override fun getEvaluationsFlow(): Flow<List<LocalEvaluation>> = evaluationsState

	override suspend fun getEvaluation(eid: String): LocalEvaluation? {
		return evaluationsState.value.firstOrNull { evaluation -> evaluation.id == eid }
	}

	override suspend fun getAvailableSubjects(): List<LocalSubject> = availableSubjects

	override suspend fun addEvaluation(evaluation: LocalEvaluation): LocalEvaluation {
		addedEvaluations += evaluation
		evaluationsState.value = evaluationsState.value + evaluation
		return evaluation
	}

	override suspend fun updateEvaluation(evaluation: LocalEvaluation): LocalEvaluation {
		updatedEvaluations += evaluation
		evaluationsState.value = evaluationsState.value.map { current ->
			if (current.id == evaluation.id) evaluation else current
		}
		return evaluation
	}

	override suspend fun removeEvaluation(eid: String) {
		removedEvaluationIds += eid
		evaluationsState.value = evaluationsState.value.filterNot { evaluation ->
			evaluation.id == eid
		}
	}

	override suspend fun saveEvaluations(evaluations: List<LocalEvaluation>) {
		savedEvaluations += evaluations
		evaluationsState.value = evaluations
	}
}

class FakeEvaluationsApiDataSource(
	private val evaluations: List<RemoteEvaluation> = listOf(
		DEFAULT_REMOTE_PENDING_EVALUATION,
		DEFAULT_REMOTE_COMPLETED_EVALUATION
	)
) : EvaluationsApiDataSource {
	var getEvaluationsCalls = 0
	val addedEvaluations = mutableListOf<RemoteEvaluation>()
	val updatedEvaluations = mutableListOf<RemoteEvaluation>()
	val removedEvaluationIds = mutableListOf<String>()

	override suspend fun getEvaluations(): List<RemoteEvaluation> {
		getEvaluationsCalls++
		return evaluations
	}

	override suspend fun getEvaluation(eid: String): RemoteEvaluation? {
		return evaluations.firstOrNull { evaluation -> evaluation.id == eid }
	}

	override suspend fun addEvaluation(evaluation: RemoteEvaluation): RemoteEvaluation {
		addedEvaluations += evaluation
		return evaluation
	}

	override suspend fun updateEvaluation(evaluation: RemoteEvaluation): RemoteEvaluation {
		updatedEvaluations += evaluation
		return evaluation
	}

	override suspend fun removeEvaluation(eid: String) {
		removedEvaluationIds += eid
	}
}

class FakeSettingsDataSource(
	private val onCooldown: Boolean
) : SettingsDataSource {
	var cooldownMarked = false

	override suspend fun isGetEvaluationsOnCooldown(): Boolean = onCooldown

	override suspend fun setGetEvaluationsOnCooldown() {
		cooldownMarked = true
	}
}

class FakeEvaluationTextProvider : EvaluationTextProvider {
	override fun defaultError(): String = "Error"

	override fun serviceUnavailable(): String = "Servicio no disponible"

	override fun networkUnavailable(): String = "Sin conexion"

	override fun timeout(): String = "Tiempo agotado"

	override fun evaluationAdded(): String = "Evaluacion agregada"

	override fun evaluationUpdated(): String = "Evaluacion actualizada"

	override fun evaluationRemoved(): String = "Evaluacion eliminada"

	override fun evaluationGradeUpdated(): String = "Nota actualizada"

	override fun evaluationSubjectMissed(): String = "Falta asignatura"

	override fun evaluationTypeMissed(): String = "Falta tipo"

	override fun evaluationMaxGradeMissed(): String = "Falta nota maxima"
}

class FakeEvaluationFilterLabelsProvider : EvaluationFilterLabelsProvider {
	override fun pending(): String = "Pendientes"

	override fun completed(): String = "Completadas"

	override fun noGrade(): String = "Sin nota"

	override fun date(date: Long?): String = "Fecha ${date ?: "continua"}"
}

class FakeIdentifierRepository(
	private val identifier: String = "generated-evaluation-id"
) : IdentifierRepository {
	override fun generateRandomIdentifier(): String = identifier
}

class RecordingReportingRepository : ReportingRepository {
	val exceptions = mutableListOf<Throwable>()

	override fun setIdentifier(identifier: String) = Unit

	override fun logException(throwable: Throwable) {
		exceptions += throwable
	}

	override fun logMessage(message: String) = Unit

	override fun <T : Any> setCustomKey(key: String, value: T) = Unit
}

class FakeEvaluationFilter(
	private val label: String,
	private val predicate: (Evaluation) -> Boolean
) : EvaluationFilter {
	override fun getLabel(): String = label

	override fun match(evaluation: Evaluation): Boolean = predicate(evaluation)
}
