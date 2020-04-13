package com.gdavidpb.tuindice.utils.extensions

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.gdavidpb.tuindice.MainNavigationDirections
import com.gdavidpb.tuindice.ui.activities.LoginActivity

fun Fragment.navigateToLogin() {
    val direction = MainNavigationDirections.actionToLogin()

    findNavController().navigate(direction)

    val activity = requireActivity()

    if (activity !is LoginActivity) activity.finish()
}

fun FragmentActivity.navigateToLogin(navController: NavController) {
    val direction = MainNavigationDirections.actionToLogin()

    navController.navigate(direction)

    finish()
}