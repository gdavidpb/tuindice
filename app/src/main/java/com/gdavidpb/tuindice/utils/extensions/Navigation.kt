package com.gdavidpb.tuindice.utils.extensions

import androidx.navigation.NavController
import androidx.navigation.NavOptions

fun NavController.popStackToRoot() {
    while (previousBackStackEntry != null) popBackStack()
}

fun NavController.navOptionsClean(): NavOptions? = currentDestination?.id
        ?.let { destinationId ->
            NavOptions.Builder()
                    .setPopUpTo(destinationId, true)
                    .build()
        }