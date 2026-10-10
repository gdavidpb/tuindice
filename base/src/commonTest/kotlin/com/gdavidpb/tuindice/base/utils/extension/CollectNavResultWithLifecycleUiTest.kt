package com.gdavidpb.tuindice.base.utils.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.gdavidpb.tuindice.base.presentation.navigation.LocalNavEntryScope
import com.gdavidpb.tuindice.base.presentation.navigation.NavEntryScope
import com.gdavidpb.tuindice.base.presentation.navigation.NavResult
import com.gdavidpb.tuindice.base.presentation.navigation.NavResultStore
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class CollectNavResultWithLifecycleUiTest {
	@Test
	fun when_resultIsPublishedForTheCurrentEntry_then_deliversItOnceAndConsumesIt() = runTuIndiceUiTest {
		val resultStore = NavResultStore()
		val entryScope = entryScope(resultStore = resultStore, isCurrent = true)
		val delivered = mutableListOf<TermPickedNavResult>()

		setTuIndiceTestContent {
			TermPickedCollector(entryScope = entryScope) { result -> delivered += result }
		}
		waitForIdle()
		assertTrue(delivered.isEmpty())

		runOnIdle {
			resultStore.publish(ENTRY_STORE_KEY, TermPickedNavResult(termId = "2024-sep"))
		}

		waitUntil(timeoutMillis = DELIVERY_TIMEOUT_MILLIS) {
			delivered == listOf(TermPickedNavResult(termId = "2024-sep"))
		}
		waitForIdle()

		// The slot is emptied on delivery, so the same result is never replayed.
		assertNull(resultStore.peek(ENTRY_STORE_KEY))
		assertEquals(listOf(TermPickedNavResult(termId = "2024-sep")), delivered)
	}

	@Test
	fun when_entryIsNotCurrent_then_holdsTheResultUntilTheEntryIsBackOnTop() = runTuIndiceUiTest {
		val resultStore = NavResultStore()
		val isCurrent = mutableStateOf(false)
		val entryScope = NavEntryScope(
			storeKey = ENTRY_STORE_KEY,
			isCurrent = isCurrent,
			resultStore = resultStore
		)
		val delivered = mutableListOf<TermPickedNavResult>()

		setTuIndiceTestContent {
			TermPickedCollector(entryScope = entryScope) { result -> delivered += result }
		}

		runOnIdle {
			resultStore.publish(ENTRY_STORE_KEY, TermPickedNavResult(termId = "2025-ene"))
		}
		waitForIdle()

		// The dialog is still on top: the result waits in the store.
		assertTrue(delivered.isEmpty())
		assertEquals(TermPickedNavResult(termId = "2025-ene"), resultStore.peek(ENTRY_STORE_KEY))

		runOnIdle {
			isCurrent.value = true
		}

		waitUntil(timeoutMillis = DELIVERY_TIMEOUT_MILLIS) {
			delivered == listOf(TermPickedNavResult(termId = "2025-ene"))
		}
		assertNull(resultStore.peek(ENTRY_STORE_KEY))
	}

	@Test
	fun when_resultIsNotForThisCollector_then_leavesItPending() = runTuIndiceUiTest {
		val resultStore = NavResultStore()
		val entryScope = entryScope(resultStore = resultStore, isCurrent = true)
		val delivered = mutableListOf<TermPickedNavResult>()

		setTuIndiceTestContent {
			TermPickedCollector(entryScope = entryScope) { result -> delivered += result }
		}

		runOnIdle {
			// Another result type on this entry, and the expected type addressed to another entry.
			resultStore.publish(ENTRY_STORE_KEY, DialogDismissedNavResult(reason = "cancelado"))
			resultStore.publish(OTHER_STORE_KEY, TermPickedNavResult(termId = "2023-abr"))
		}
		waitForIdle()

		assertTrue(delivered.isEmpty())
		assertEquals(DialogDismissedNavResult(reason = "cancelado"), resultStore.peek(ENTRY_STORE_KEY))
		assertEquals(TermPickedNavResult(termId = "2023-abr"), resultStore.peek(OTHER_STORE_KEY))

		runOnIdle {
			resultStore.publish(ENTRY_STORE_KEY, TermPickedNavResult(termId = "2024-sep"))
		}

		// The collector is still alive after skipping a foreign result.
		waitUntil(timeoutMillis = DELIVERY_TIMEOUT_MILLIS) {
			delivered == listOf(TermPickedNavResult(termId = "2024-sep"))
		}
		assertEquals(TermPickedNavResult(termId = "2023-abr"), resultStore.peek(OTHER_STORE_KEY))
	}

	@Test
	fun when_lifecycleIsBelowTheMinActiveState_then_defersDeliveryUntilItIsReached() = runTuIndiceUiTest {
		val resultStore = NavResultStore()
		val entryScope = entryScope(resultStore = resultStore, isCurrent = true)
		val lifecycleOwner = NavResultTestLifecycleOwner()
		val delivered = mutableListOf<TermPickedNavResult>()

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
				TermPickedCollector(entryScope = entryScope) { result -> delivered += result }
			}
		}

		runOnIdle {
			lifecycleOwner.handleEvent(Lifecycle.Event.ON_CREATE)
			lifecycleOwner.handleEvent(Lifecycle.Event.ON_START)
			resultStore.publish(ENTRY_STORE_KEY, TermPickedNavResult(termId = "2024-sep"))
		}
		waitForIdle()

		// Started but not resumed: the default minimum state has not been reached.
		assertTrue(delivered.isEmpty())
		assertEquals(TermPickedNavResult(termId = "2024-sep"), resultStore.peek(ENTRY_STORE_KEY))

		runOnIdle {
			lifecycleOwner.handleEvent(Lifecycle.Event.ON_RESUME)
		}

		waitUntil(timeoutMillis = DELIVERY_TIMEOUT_MILLIS) {
			delivered == listOf(TermPickedNavResult(termId = "2024-sep"))
		}
		assertNull(resultStore.peek(ENTRY_STORE_KEY))
	}

	@Test
	fun when_minActiveStateIsStarted_then_deliversWithoutWaitingForResume() = runTuIndiceUiTest {
		val resultStore = NavResultStore()
		val entryScope = entryScope(resultStore = resultStore, isCurrent = true)
		val lifecycleOwner = NavResultTestLifecycleOwner()
		val delivered = mutableListOf<TermPickedNavResult>()

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
				TermPickedCollector(
					entryScope = entryScope,
					minActiveState = Lifecycle.State.STARTED,
					awaitFrame = true
				) { result -> delivered += result }
			}
		}

		runOnIdle {
			lifecycleOwner.handleEvent(Lifecycle.Event.ON_CREATE)
			lifecycleOwner.handleEvent(Lifecycle.Event.ON_START)
			resultStore.publish(ENTRY_STORE_KEY, TermPickedNavResult(termId = "2024-sep"))
		}

		waitUntil(timeoutMillis = DELIVERY_TIMEOUT_MILLIS) {
			delivered == listOf(TermPickedNavResult(termId = "2024-sep"))
		}
		assertNull(resultStore.peek(ENTRY_STORE_KEY))
	}

	private fun entryScope(
		resultStore: NavResultStore,
		isCurrent: Boolean
	): NavEntryScope {
		return NavEntryScope(
			storeKey = ENTRY_STORE_KEY,
			isCurrent = mutableStateOf(isCurrent),
			resultStore = resultStore
		)
	}

	private companion object {
		const val ENTRY_STORE_KEY = "record_entry"
		const val OTHER_STORE_KEY = "summary_entry"
		const val DELIVERY_TIMEOUT_MILLIS = 2_000L
	}
}

@Composable
private fun TermPickedCollector(
	entryScope: NavEntryScope,
	minActiveState: Lifecycle.State = Lifecycle.State.RESUMED,
	awaitFrame: Boolean = false,
	onResult: (TermPickedNavResult) -> Unit
) {
	CompositionLocalProvider(LocalNavEntryScope provides entryScope) {
		CollectNavResultWithLifecycle<TermPickedNavResult>(
			minActiveState = minActiveState,
			awaitFrame = awaitFrame
		) { result ->
			onResult(result)
		}
	}
}

private data class TermPickedNavResult(
	val termId: String
) : NavResult

private data class DialogDismissedNavResult(
	val reason: String
) : NavResult

private class NavResultTestLifecycleOwner : LifecycleOwner {
	private val registry = LifecycleRegistry(this)

	override val lifecycle: Lifecycle
		get() = registry

	fun handleEvent(event: Lifecycle.Event) {
		registry.handleLifecycleEvent(event)
	}
}
