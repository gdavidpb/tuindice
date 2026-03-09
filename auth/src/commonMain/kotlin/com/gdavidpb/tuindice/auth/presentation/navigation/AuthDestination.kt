package com.gdavidpb.tuindice.auth.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import kotlinx.serialization.Serializable

@Serializable
sealed class AuthDestination : Destination() {
	@Serializable
	data object NavGraph : AuthDestination()

	@Serializable
	data object SignIn : AuthDestination()

	@Serializable
	data object SignOutDialog : AuthDestination()

	@Serializable
	data object UpdatePasswordDialog : AuthDestination()
}
