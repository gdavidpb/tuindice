package com.gdavidpb.tuindice.record.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod

data class SyntheticTermPeriodOption(
	val periodYear: Int,
	val periodCode: AcademicTermPeriod
) {
	val termKey: String = "$periodYear-${periodCode.name}"
	val termOrder: Int = periodYear * 10 + periodCode.sequence
	val label: String = "${periodCode.shortLabel} $periodYear"
}
