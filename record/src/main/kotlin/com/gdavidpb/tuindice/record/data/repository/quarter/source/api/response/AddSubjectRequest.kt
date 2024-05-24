package com.gdavidpb.tuindice.record.data.repository.quarter.source.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddSubjectRequest(
	@SerialName("reference") val reference: String,
	@SerialName("code") val code: String,
	@SerialName("grade") val grade: Int
)