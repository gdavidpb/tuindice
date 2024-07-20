package com.gdavidpb.tuindice.login.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.navigation.Destination2
import kotlinx.serialization.Serializable

@Serializable
sealed class LoginDestination : Destination2() {
	@Serializable
	data object NavGraph : LoginDestination()

	@Serializable
	data object SignIn : LoginDestination()

	@Serializable
	data object SignOutDialog : LoginDestination()

	@Serializable
	data object UpdatePasswordDialog : LoginDestination()
}