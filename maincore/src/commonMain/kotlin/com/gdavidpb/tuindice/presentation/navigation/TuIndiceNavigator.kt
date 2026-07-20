package com.gdavidpb.tuindice.presentation.navigation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.gdavidpb.tuindice.auth.presentation.navigation.AuthDestination
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.presentation.navigation.DialogDestination
import com.gdavidpb.tuindice.base.presentation.navigation.NavResult
import com.gdavidpb.tuindice.base.presentation.navigation.NavResultStore
import com.gdavidpb.tuindice.base.presentation.navigation.TuIndiceNavActions
import com.gdavidpb.tuindice.presentation.mapper.toTabRootDestination

/**
 * Owner of the app's navigation state: one auth stack plus one stack per
 * bottom-bar tab. [backStack] flattens the start tab's stack under the
 * current tab's so NavDisplay predictive pops cross tabs into the start tab;
 * parked tabs keep their stacks and retained state untouched.
 */
@Stable
class TuIndiceNavigator(
	val startTab: MainSection,
	private val rootModeState: MutableState<TuIndiceRootMode>,
	private val currentTabState: MutableState<MainSection>,
	private val stacks: TuIndiceNavStacks,
	val entryStores: NavEntryStores,
	val resultStore: NavResultStore
) : TuIndiceNavActions {

	private val authStack: NavBackStack<NavKey>
		get() = stacks.authStack

	private val tabStacks: Map<MainSection, NavBackStack<NavKey>>
		get() = stacks.tabStacks

	val rootMode: TuIndiceRootMode
		get() = rootModeState.value

	val currentTab: MainSection
		get() = currentTabState.value

	val backStack: List<NavKey> by derivedStateOf {
		when (rootModeState.value) {
			TuIndiceRootMode.AUTH -> authStack.toList()
			TuIndiceRootMode.SIGNED_IN -> {
				val currentTab = currentTabState.value

				if (currentTab == startTab) {
					tabStacks.getValue(startTab).toList()
				} else {
					tabStacks.getValue(startTab).toList() + tabStacks.getValue(currentTab).toList()
				}
			}
		}
	}

	val currentKey: Destination
		get() = backStack.last() as Destination

	val currentStoreKey: String
		get() = storeKeyOf(backStack.last())

	val showsBackButton: Boolean by derivedStateOf {
		activeStack().size > 1 && activeStack().last() !is DialogDestination
	}

	override fun push(key: Destination) {
		val stack = activeStack()

		if (stack.last() == key) return

		stack.add(key)
	}

	override fun pop(): Boolean {
		val stack = activeStack()

		return when {
			stack.size > 1 -> {
				val removedStoreKey = storeKeyOf(stack.last())

				stack.removeAt(stack.lastIndex)
				entryStores.onRemoved(removedStoreKey)
				resultStore.consume(removedStoreKey)
				true
			}

			rootModeState.value == TuIndiceRootMode.SIGNED_IN && currentTabState.value != startTab -> {
				currentTabState.value = startTab
				true
			}

			else -> false
		}
	}

	override fun popWithResult(result: NavResult): Boolean {
		val stack = activeStack()

		if (stack.size > 1) {
			resultStore.publish(
				receiverStoreKey = storeKeyOf(stack[stack.size - 2]),
				result = result
			)
		}

		return pop()
	}

	fun switchTab(section: MainSection) {
		if (rootModeState.value != TuIndiceRootMode.SIGNED_IN) return

		currentTabState.value = section
	}

	fun replaceAllForSignIn() {
		Snapshot.withMutableSnapshot {
			tabStacks.forEach { (section, stack) ->
				stack.clear()
				stack.add(section.toTabRootDestination())
			}

			authStack.clear()
			authStack.add(AuthDestination.SignIn)

			currentTabState.value = startTab
			rootModeState.value = TuIndiceRootMode.AUTH
		}

		entryStores.clearAll()
		resultStore.clear()
	}

	fun replaceAllForSignedIn(section: MainSection) {
		Snapshot.withMutableSnapshot {
			tabStacks.forEach { (tabSection, stack) ->
				stack.clear()
				stack.add(tabSection.toTabRootDestination())
			}

			authStack.clear()
			authStack.add(AuthDestination.SignIn)

			currentTabState.value = section
			rootModeState.value = TuIndiceRootMode.SIGNED_IN
		}

		entryStores.clearAll()
		resultStore.clear()
	}

	internal fun storeKeyOf(key: NavKey): String = when (rootModeState.value) {
		TuIndiceRootMode.AUTH -> "auth:$key"
		TuIndiceRootMode.SIGNED_IN -> {
			val currentTab = currentTabState.value
			val ownerTab = when {
				tabStacks.getValue(currentTab).contains(key) -> currentTab
				tabStacks.getValue(startTab).contains(key) -> startTab
				else -> currentTab
			}

			"${ownerTab.name}:$key"
		}
	}

	private fun activeStack(): NavBackStack<NavKey> = when (rootModeState.value) {
		TuIndiceRootMode.AUTH -> authStack
		TuIndiceRootMode.SIGNED_IN -> tabStacks.getValue(currentTabState.value)
	}
}
