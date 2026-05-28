package com.gdavidpb.tuindice.enrollmentproof.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import kotlinx.serialization.Serializable

@Serializable
sealed class EnrollmentProofDestination : Destination() {
	@Serializable
	data object EnrollmentProofDialog : EnrollmentProofDestination()
}