package com.gdavidpb.tuindice.record.domain.model

import com.gdavidpb.tuindice.base.domain.model.Quarter

data class Quarters(
	val quarters: List<Quarter>,
	val lastUpdate: Long
)
