package com.gdavidpb.tuindice.record.data.repository.mutation

import com.gdavidpb.tuindice.base.domain.model.mutation.OutboxMutation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface RecordMutation : OutboxMutation {
	@Serializable
	@SerialName("add_quarter")
	data class AddQuarter(
		val quarterId: String,
		val startDate: Long,
		val endDate: Long
	) : RecordMutation {
		override val entityType: String = "record:add_quarter"
		override val entityId: String = quarterId
		override val replaceKey: String = "quarter:$quarterId"
	}

	@Serializable
	@SerialName("set_subject_grade")
	data class SetSubjectGrade(
		val quarterId: String,
		val subjectId: String,
		val grade: Int
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
