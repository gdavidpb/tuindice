package com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.database.mapper

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.mapper.toAddEvaluationRequest
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.api.mapper.toUpdateEvaluationRequest
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.utils.extension.computeEvaluationState
import com.gdavidpb.tuindice.persistence.data.room.entity.EvaluationEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.TransactionEntity
import com.gdavidpb.tuindice.persistence.data.room.mapper.toSubject
import com.gdavidpb.tuindice.persistence.data.room.otm.EvaluationWithSubject
import com.gdavidpb.tuindice.transactions.domain.model.TransactionAction
import com.gdavidpb.tuindice.transactions.domain.model.TransactionType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Evaluation.toEvaluationEntity(uid: String) = EvaluationEntity(
	id = evaluationId,
	subjectId = subjectId,
	quarterId = quarterId,
	accountId = uid,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type
)

fun EvaluationAdd.toEvaluationEntity(uid: String) = EvaluationEntity(
	id = reference,
	subjectId = subjectId,
	quarterId = quarterId,
	accountId = uid,
	grade = grade,
	maxGrade = maxGrade,
	date = date,
	type = type
)

fun EvaluationAdd.toTransactionEntity(uid: String) = TransactionEntity(
	id = "${TransactionAction.ADD}:$reference",
	timestamp = System.currentTimeMillis(),
	accountId = uid,
	reference = reference,
	type = TransactionType.EVALUATION.ordinal,
	action = TransactionAction.ADD.ordinal,
	data = Json.encodeToString(toAddEvaluationRequest())
)

fun EvaluationUpdate.toTransactionEntity(uid: String) = TransactionEntity(
	id = "${TransactionAction.UPDATE}:$evaluationId",
	timestamp = System.currentTimeMillis(),
	accountId = uid,
	reference = evaluationId,
	type = TransactionType.EVALUATION.ordinal,
	action = TransactionAction.UPDATE.ordinal,
	data = Json.encodeToString(toUpdateEvaluationRequest())
)

fun EvaluationRemove.toTransactionEntity(uid: String) = TransactionEntity(
	id = "${TransactionAction.DELETE}:$evaluationId",
	timestamp = System.currentTimeMillis(),
	accountId = uid,
	reference = evaluationId,
	type = TransactionType.EVALUATION.ordinal,
	action = TransactionAction.DELETE.ordinal,
	data = null
)

fun EvaluationWithSubject.toEvaluation() = Evaluation(
	evaluationId = evaluation.id,
	subjectId = evaluation.subjectId,
	quarterId = evaluation.quarterId,
	grade = evaluation.grade,
	maxGrade = evaluation.maxGrade,
	date = evaluation.date,
	type = evaluation.type,
	state = computeEvaluationState(
		grade = evaluation.grade,
		date = evaluation.date
	),
	subject = subject.toSubject(isEditable = true)
)