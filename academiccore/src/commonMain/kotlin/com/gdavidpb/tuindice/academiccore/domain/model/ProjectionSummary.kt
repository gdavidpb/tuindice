package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectionSummary(
	val grade: Double = 0.0,
	@SerialName("enrolled_subjects") val enrolledSubjects: Int = 0,
	@SerialName("enrolled_credits") val enrolledCredits: Int = 0,
	@SerialName("approved_subjects") val approvedSubjects: Int = 0,
	@SerialName("approved_credits") val approvedCredits: Int = 0,
	@SerialName("approved_relation") val approvedRelation: Double = 0.0,
	@SerialName("retired_subjects") val retiredSubjects: Int = 0,
	@SerialName("retired_credits") val retiredCredits: Int = 0,
	@SerialName("retired_relation") val retiredRelation: Double = 0.0,
	@SerialName("failed_subjects") val failedSubjects: Int = 0,
	@SerialName("failed_credits") val failedCredits: Int = 0,
	@SerialName("failed_relation") val failedRelation: Double = 0.0,
	@SerialName("without_effect_attempts") val withoutEffectAttempts: Int = 0
)
