package com.gdavidpb.tuindice.subjects.domain.model

import com.gdavidpb.tuindice.base.domain.model.GradingMode

data class SubjectDetail(
	val id: String,
	val name: String? = null,
	val credits: Int? = null,
	val gradingMode: GradingMode? = null,
	val generatedAt: Long,
	val expiresAt: Long,
	val careerSegment: SubjectStatsSegment? = null,
	val globalSegment: SubjectStatsSegment? = null
)
