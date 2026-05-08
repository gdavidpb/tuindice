package com.gdavidpb.tuindice.subjects.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchSubjectsResponse(
	@SerialName("query") val query: String,
	@SerialName("results") val results: List<SubjectSearchResultResponse> = emptyList()
)
