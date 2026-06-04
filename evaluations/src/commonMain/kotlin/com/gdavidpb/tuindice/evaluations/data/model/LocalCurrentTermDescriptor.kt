package com.gdavidpb.tuindice.evaluations.data.model

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod

data class LocalCurrentTermDescriptor(
	val id: String,
	val periodYear: Int,
	val periodCode: AcademicTermPeriod,
	val periodLabel: String
)
