package com.gdavidpb.tuindice.enrollmentproof.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.navigation.Destination2
import kotlinx.serialization.Serializable

@Serializable
sealed class EnrollmentProofFetchDestination : Destination2() {
	@Serializable
	data object EnrollmentProofFetchDialog : EnrollmentProofFetchDestination()
}