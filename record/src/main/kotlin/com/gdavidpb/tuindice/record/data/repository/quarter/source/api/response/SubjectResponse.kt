package com.gdavidpb.tuindice.record.data.repository.quarter.source.api.response

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
	@SerialName("status") val status: Int,
	@SerialName("no_effect_by") val noEffectBy: String? = null
)