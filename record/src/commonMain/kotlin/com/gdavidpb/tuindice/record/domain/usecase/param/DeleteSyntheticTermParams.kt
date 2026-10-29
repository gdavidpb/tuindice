package com.gdavidpb.tuindice.record.domain.usecase.param

data class DeleteSyntheticTermParams(
	val termId: String,
	val mutationId: String? = null
)
