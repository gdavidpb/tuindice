package com.gdavidpb.tuindice.summary.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod

/**
 * The last term that is over: its [termKey], the key the share sheet knows it by, its period, its [index] as Registro
 * shows it (the period average, with four decimals there) and the credits of the subjects it [approvedCredits].
 * [isShareable] says whether the share sheet has a card for it, which a closed term has when it has an index.
 */
data class SummaryFinishedTerm(
	val termKey: String,
	val periodCode: AcademicTermPeriod,
	val periodYear: Int,
	val index: Double,
	val approvedCredits: Int,
	val isShareable: Boolean
)
