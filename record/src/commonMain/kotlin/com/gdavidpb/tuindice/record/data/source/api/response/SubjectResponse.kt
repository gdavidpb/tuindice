package com.gdavidpb.tuindice.record.data.source.api.response

import com.gdavidpb.tuindice.base.domain.model.subject.GradingMode
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubjectResponse(
	@SerialName("id") val id: String,
	@SerialName("qid") val quarterId: String,
	@SerialName("code") val code: String,
	@SerialName("name") val name: String,
	@SerialName("credits") val credits: Int,
	@SerialName("grade") val grade: Int,
	@SerialName("grading_mode") val gradingMode: GradingMode = GradingMode.NUMERIC,
	@SerialName("status") val status: SubjectStatus? = null,
	@SerialName("simulation_status") val simulationStatus: SubjectStatus? = null,
	@SerialName("revision") val revision: Long
)
