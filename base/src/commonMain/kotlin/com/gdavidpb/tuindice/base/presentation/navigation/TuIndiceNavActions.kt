package com.gdavidpb.tuindice.base.presentation.navigation

import androidx.compose.runtime.Stable

/**
 * Navigation surface exposed to feature entry providers; the full navigator
 * stays owned by the app host.
 */
@Stable
interface TuIndiceNavActions {

	fun push(key: Destination)

	fun pop(): Boolean

	fun popWithResult(result: NavResult): Boolean
}
