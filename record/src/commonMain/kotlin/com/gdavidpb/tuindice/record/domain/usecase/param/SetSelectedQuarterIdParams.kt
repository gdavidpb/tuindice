package com.gdavidpb.tuindice.record.domain.usecase.param

import com.gdavidpb.tuindice.record.domain.model.RecordViewMode

data class SetSelectedQuarterIdParams(
	val viewMode: RecordViewMode,
	val quarterId: String
)
