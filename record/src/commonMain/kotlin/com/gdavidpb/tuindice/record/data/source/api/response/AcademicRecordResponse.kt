package com.gdavidpb.tuindice.record.data.source.api.response

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AcademicRecordResponse(
	@SerialName("revision") val revision: Long,
	@SerialName("record") val record: AcademicRecord
)
