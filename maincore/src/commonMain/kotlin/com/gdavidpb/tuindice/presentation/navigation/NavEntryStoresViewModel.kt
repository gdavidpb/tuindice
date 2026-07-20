package com.gdavidpb.tuindice.presentation.navigation

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.base.presentation.navigation.NavResultStore

/**
 * Host-scoped owner of navigation retention state: survives configuration
 * changes and is cleared only when the host is finished for real.
 */
class NavEntryStoresViewModel : ViewModel() {

	val entryStores = NavEntryStores()
	val resultStore = NavResultStore()

	override fun onCleared() {
		entryStores.clearAll()
		resultStore.clear()
	}
}
