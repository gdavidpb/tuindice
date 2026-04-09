package com.gdavidpb.tuindice.record.domain.usecase.param

import com.gdavidpb.tuindice.record.domain.model.RecordViewMode

data class SetSelectedTermParams(
	val viewMode: RecordViewMode,
	val termId: String
)
