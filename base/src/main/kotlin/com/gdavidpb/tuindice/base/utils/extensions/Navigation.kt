package com.gdavidpb.tuindice.base.utils.extensions

import androidx.navigation.NavController

fun NavController.popStackToRoot() {
    while (previousBackStackEntry != null) popBackStack()
}