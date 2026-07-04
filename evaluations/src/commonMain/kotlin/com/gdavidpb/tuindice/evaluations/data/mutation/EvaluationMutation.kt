package com.gdavidpb.tuindice.evaluations.data.mutation

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.domain.model.mutation.OutboxMutation
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

const val EVALUATIONS_MUTATION_SCOPE = "evaluations"
const val EVALUATIONS_MUTATION_STORE_ID = "evaluations"

@Serializable
@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("mutation_type")
sealed interface EvaluationMutation : OutboxMutation {
	@Serializable
	@SerialName("add_evaluation")
	data class Add(
		val referenceId: String,
		val attemptId: String,
		val subjectCode: String,
		val termId: String,
		val scheduleMode: EvaluationScheduleMode,
		val grade: Double?,
		val maxGrade: Double,
		val date: Long?,
		val type: Int
	) : EvaluationMutation {
		override val entityType: String = "evaluations:add"
		override val entityId: String = referenceId
		override val replaceKey: String = "evaluation:$referenceId"
	}

	@Serializable
	@SerialName("update_evaluation")
	data class Update(
		val evaluationId: String,
		val scheduleMode: EvaluationScheduleMode?,
		val grade: Double?,
		val maxGrade: Double?,
		val date: Long?,
		val type: Int?
	) : EvaluationMutation {
		override val entityType: String = "evaluations:update"
		override val entityId: String = evaluationId
		override val replaceKey: String = "evaluation:$evaluationId"
	}

	@Serializable
	@SerialName("remove_evaluation")
	data class Remove(
		val evaluationId: String
	) : EvaluationMutation {
		override val entityType: String = "evaluations:remove"
		override val entityId: String = evaluationId
		override val replaceKey: String = "evaluation:$evaluationId"
	}
}
