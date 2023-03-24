package com.gdavidpb.tuindice.base.domain.model.subject

import com.gdavidpb.tuindice.base.domain.model.resolution.ResolutionOperation

data class SubjectUpdateResolution(
	val id: String,
	val quarterId: String,
	val code: String,
	val name: String,
	val credits: Int,
	val grade: Int,
	val status: Int,
	val noEffectBy: String? = null
) : ResolutionOperation
