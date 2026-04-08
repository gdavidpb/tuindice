package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TermProjection(
	val id: String,
	val label: String,
	@SerialName("start_at") val startAtMillis: Long,
	@SerialName("end_at") val endAtMillis: Long,
	val order: Int,
	@SerialName("current") val current: Boolean = false,
	val closed: Boolean,
	val editable: Boolean,
	val synthetic: Boolean = false,
	val grade: Double,
	@SerialName("grade_sum") val gradeSum: Double,
	val credits: Int,
	@SerialName("credits_sum") val creditsSum: Int,
	val attempts: List<AttemptProjection> = emptyList()
)
