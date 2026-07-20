package com.gdavidpb.tuindice.base.presentation.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State

/**
 * Per-entry context provided by the app host's entry decorators;
 * [isCurrent] is true only while the entry is the top of the back stack.
 */
@Stable
class NavEntryScope(
	val storeKey: String,
	val isCurrent: State<Boolean>,
	val resultStore: NavResultStore
)
