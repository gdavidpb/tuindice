package com.gdavidpb.tuindice.persistence.domain.model

/** The credits the student has approved out of the credits of the selected pensum. */
data class PensumCreditsProgress(
	val approvedCredits: Int,
	val totalCredits: Int
)
