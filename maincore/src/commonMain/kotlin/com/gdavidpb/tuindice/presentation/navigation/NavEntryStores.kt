package com.gdavidpb.tuindice.presentation.navigation

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

/**
 * ViewModel stores for navigation entries whose lifetime is decided by the
 * navigator: an entry parked in a tab stack keeps its store, and clearing
 * happens only on real removal from the owning stack (pop or root swap),
 * never on leaving the displayed back stack.
 */
class NavEntryStores {

	private val stores = mutableMapOf<String, NavEntryStoreOwner>()
	private val removalListeners = mutableListOf<(String) -> Unit>()

	fun viewModelStoreOwner(storeKey: String): ViewModelStoreOwner =
		stores.getOrPut(storeKey) { NavEntryStoreOwner() }

	fun onRemoved(storeKey: String) {
		stores.remove(storeKey)?.viewModelStore?.clear()
		removalListeners.toList().forEach { listener -> listener(storeKey) }
	}

	fun addRemovalListener(listener: (String) -> Unit): () -> Unit {
		removalListeners.add(listener)
		return { removalListeners.remove(listener) }
	}

	fun clearAll() {
		val storeKeys = stores.keys.toList()
		stores.values.forEach { owner -> owner.viewModelStore.clear() }
		stores.clear()
		storeKeys.forEach { storeKey ->
			removalListeners.toList().forEach { listener -> listener(storeKey) }
		}
	}
}

private class NavEntryStoreOwner : ViewModelStoreOwner {
	override val viewModelStore = ViewModelStore()
}
