package com.gdavidpb.tuindice.summary.domain.model

/** The credits the student has approved out of the credits of the selected pensum, as the summary reads them. */
data class SummaryCreditsProgress(
	val approvedCredits: Int,
	val totalCredits: Int
)
