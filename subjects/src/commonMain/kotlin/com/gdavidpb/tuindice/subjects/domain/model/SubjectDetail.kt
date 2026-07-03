package com.gdavidpb.tuindice.subjects.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.GradingMode

data class SubjectDetail(
	val id: String,
	val name: String,
	val credits: Int,
	val gradingMode: GradingMode,
	val generatedAt: Long,
	val expiresAt: Long,
	val careerSegment: SubjectStatsSegment? = null,
	val globalSegment: SubjectStatsSegment? = null
)
