package com.gdavidpb.tuindice.record.domain.mapper

import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.usecase.param.RemoveQuarterParams

fun RemoveQuarterParams.toQuarterRemove() = QuarterRemove(
	quarterId = quarterId
)