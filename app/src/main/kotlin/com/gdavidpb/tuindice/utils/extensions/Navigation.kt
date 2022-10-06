package com.gdavidpb.tuindice.utils.extensions

import androidx.navigation.NavController

fun NavController.popStackToRoot() {
    while (previousBackStackEntry != null) popBackStack()
}