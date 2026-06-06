package com.gdavidpb.tuindice.evaluations.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod

data class EvaluationTermDescriptor(
	val id: String,
	val periodYear: Int,
	val periodCode: AcademicTermPeriod,
	val periodLabel: String
)
