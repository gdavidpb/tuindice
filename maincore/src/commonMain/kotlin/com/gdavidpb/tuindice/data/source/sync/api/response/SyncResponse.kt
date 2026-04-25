package com.gdavidpb.tuindice.data.source.sync.api.response

import com.gdavidpb.tuindice.record.data.source.api.response.AcademicRecordResponse
import com.gdavidpb.tuindice.summary.data.model.UserResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyncResponse(
	@SerialName("record") val record: AcademicRecordResponse,
	@SerialName("user") val user: UserResponse
)
