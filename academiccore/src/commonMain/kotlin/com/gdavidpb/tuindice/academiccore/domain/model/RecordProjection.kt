package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RecordProjection(
	val terms: List<TermProjection> = emptyList()
)
