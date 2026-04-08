package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AttemptSource {
	@SerialName("dst_record")
	DST_RECORD,

	@SerialName("dst_enrollment")
	DST_ENROLLMENT,

	@SerialName("local")
	LOCAL
}
