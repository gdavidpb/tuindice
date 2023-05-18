package com.gdavidpb.tuindice.login.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.login.presentation.route.SignInRoute

fun NavController.navigateToSignIn() {
	navigate(Destination.SignIn.route)
}

fun NavGraphBuilder.signInScreen() {
	composable(Destination.SignIn.route) {
		SignInRoute()
	}
}