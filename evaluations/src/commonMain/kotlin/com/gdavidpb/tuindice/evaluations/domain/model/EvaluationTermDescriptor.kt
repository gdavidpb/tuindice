package com.gdavidpb.tuindice.evaluations.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import kotlinx.datetime.LocalDate

data class EvaluationTermDescriptor(
	val id: String,
	val periodYear: Int,
	val periodCode: AcademicTermPeriod,
	val periodLabel: String,
	val classesStartDate: LocalDate? = null
)
