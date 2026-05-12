package com.gdavidpb.tuindice.record.domain.usecase.param

import com.gdavidpb.tuindice.record.domain.model.SyntheticTermPeriodOption
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject

data class CreateSyntheticTermParams(
	val period: SyntheticTermPeriodOption,
	val subjects: List<SyntheticTermSubject>
)
