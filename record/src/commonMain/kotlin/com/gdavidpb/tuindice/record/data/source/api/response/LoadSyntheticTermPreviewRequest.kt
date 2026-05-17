package com.gdavidpb.tuindice.record.data.source.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoadSyntheticTermPreviewRequest(
	@SerialName("subject_codes") val subjectCodes: List<String>
)
