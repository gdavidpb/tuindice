package com.gdavidpb.tuindice.record.data.mutation

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.base.domain.model.mutation.OutboxMutation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val RECORD_MUTATION_SCOPE = "record"
const val RECORD_MUTATION_STORE_ID = "record"

@Serializable
sealed interface AcademicRecordMutation : OutboxMutation {
	@Serializable
	@SerialName("upsert_attempt_override")
	data class UpsertAttemptOverride(
		val attemptId: String,
		val score: AttemptScore? = null,
		val outcome: AttemptOutcome? = null
	) : AcademicRecordMutation {
		override val entityType: String = "record:upsert_attempt_override"
		override val entityId: String = attemptId
		override val replaceKey: String = "attempt:$attemptId"
	}

	@Serializable
	@SerialName("delete_attempt_override")
	data class DeleteAttemptOverride(
		val attemptId: String
	) : AcademicRecordMutation {
		override val entityType: String = "record:delete_attempt_override"
		override val entityId: String = attemptId
		override val replaceKey: String = "attempt:$attemptId"
	}

	@Serializable
	@SerialName("add_synthetic_term")
	data class AddSyntheticTerm(
		val termId: String,
		val label: String,
		val startAtMillis: Long,
		val endAtMillis: Long,
		val attempts: List<SyntheticAttemptSeed>
	) : AcademicRecordMutation {
		@Serializable
		data class SyntheticAttemptSeed(
			val attemptId: String,
			val subjectCode: String,
			val subjectName: String,
			val credits: Int,
			val gradingMode: AttemptGradingMode,
			val score: AttemptScore? = null,
			val outcome: AttemptOutcome? = null
		)

		override val entityType: String = "record:add_synthetic_term"
		override val entityId: String = termId
		override val replaceKey: String = "term:$termId"
	}

	@Serializable
	@SerialName("delete_synthetic_term")
	data class DeleteSyntheticTerm(
		val termId: String
	) : AcademicRecordMutation {
		override val entityType: String = "record:delete_synthetic_term"
		override val entityId: String = termId
		override val replaceKey: String = "term:$termId"
	}
}
