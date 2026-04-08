package com.gdavidpb.tuindice.record.data.mutation

import com.gdavidpb.tuindice.base.domain.model.mutation.OutboxMutation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface RecordMutation : OutboxMutation {
	@Serializable
	@SerialName("add_quarter")
	data class AddQuarter(
		val quarter: Int,
		val year: Int,
		val subjects: List<SubjectSeed>
	) : RecordMutation {
		@Serializable
		data class SubjectSeed(
			val code: String,
			val grade: Int? = null
		)

		override val entityType: String = "record:add_quarter"
		override val entityId: String = "$year-$quarter"
		override val replaceKey: String = "quarter:$year:$quarter"
	}

	@Serializable
	@SerialName("set_subject_grade")
	data class SetSubjectGrade(
		val quarterId: String,
		val subjectId: String,
		val grade: Int? = null,
		val status: String? = null
	) : RecordMutation {
		override val entityType: String = "record:set_subject_grade"
		override val entityId: String = subjectId
		override val replaceKey: String = "subject:$subjectId"
	}

	@Serializable
	@SerialName("remove_quarter")
	data class RemoveQuarter(
		val quarterId: String
	) : RecordMutation {
		override val entityType: String = "record:remove_quarter"
		override val entityId: String = quarterId
		override val replaceKey: String = "quarter:$quarterId"
	}
}
