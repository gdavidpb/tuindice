package com.gdavidpb.tuindice.summary.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod

/**
 * The last term that is over: its period, its [index] as Registro shows it (the period average, with four decimals
 * there) and the credits of the subjects it [approvedCredits].
 */
data class SummaryFinishedTerm(
	val periodCode: AcademicTermPeriod,
	val periodYear: Int,
	val index: Double,
	val approvedCredits: Int
)
