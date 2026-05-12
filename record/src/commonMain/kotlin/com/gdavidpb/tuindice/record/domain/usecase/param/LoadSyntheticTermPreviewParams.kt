package com.gdavidpb.tuindice.record.domain.usecase.param

data class LoadSyntheticTermPreviewParams(
	val termKey: String,
	val subjectCodes: List<String>
)
