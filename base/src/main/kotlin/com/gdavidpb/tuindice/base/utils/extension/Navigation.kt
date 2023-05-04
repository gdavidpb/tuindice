package com.gdavidpb.tuindice.base.utils.extension

import androidx.fragment.app.DialogFragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController

// TODO check
fun NavController.popStackToRoot() {
	while (previousBackStackEntry != null) popBackStack()
}

fun DialogFragment.navigate(directions: NavDirections, navOptions: NavOptions? = null) {
	requireParentFragment().findNavController().navigate(directions, navOptions)
}