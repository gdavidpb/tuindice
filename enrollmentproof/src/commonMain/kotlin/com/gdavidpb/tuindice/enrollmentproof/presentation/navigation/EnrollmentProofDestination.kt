package com.gdavidpb.tuindice.enrollmentproof.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.presentation.navigation.DialogDestination
import kotlinx.serialization.Serializable

@Serializable
sealed class EnrollmentProofDestination : Destination() {
	@Serializable
	data object EnrollmentProofDialog : EnrollmentProofDestination(), DialogDestination
}