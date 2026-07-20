package com.gdavidpb.tuindice.base.presentation.navigation

import androidx.compose.runtime.staticCompositionLocalOf

val LocalNavEntryScope = staticCompositionLocalOf<NavEntryScope> {
	error("NavEntryScope is not provided; entries must be decorated by the app host")
}
