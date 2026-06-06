package com.gdavidpb.tuindice.evaluations.data.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.evaluations.data.model.LocalCurrentTermDescriptor
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationTermDescriptor
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicTermEntity

fun AcademicTermEntity.toLocalCurrentTermDescriptor() = LocalCurrentTermDescriptor(
	id = id,
	periodYear = periodYear,
	periodCode = AcademicTermPeriod.valueOf(periodCode),
	periodLabel = periodLabel
)

fun LocalCurrentTermDescriptor.toEvaluationTermDescriptor() = EvaluationTermDescriptor(
	id = id,
	periodYear = periodYear,
	periodCode = periodCode,
	periodLabel = periodLabel
)
