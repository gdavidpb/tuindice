package com.gdavidpb.tuindice.subjects.domain.model

sealed interface SubjectDetailLoad {
	data object LoadingRemote : SubjectDetailLoad

	data class Data(
		val result: SubjectDetailResult
	) : SubjectDetailLoad
}
