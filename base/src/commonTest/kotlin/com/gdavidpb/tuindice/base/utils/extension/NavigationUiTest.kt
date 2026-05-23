package com.gdavidpb.tuindice.base.utils.extension

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class NavigationUiTest {
	@Test
	fun when_collectValueWithLifecycleStartsResumedAndActive_then_collectsInitialAndUpdatedValue() = runTuIndiceUiTest {
		val lifecycleOwner = TestLifecycleOwner()
		val value = mutableStateOf(TestValue(label = "record"))
		val collectedValues = mutableListOf<TestValue>()

		setTuIndiceTestContent {
			CollectValueWithLifecycle(
				value = value.value,
				lifecycle = lifecycleOwner.lifecycle
			) { collectedValue ->
				collectedValues += collectedValue
			}
		}

		runOnIdle {
			lifecycleOwner.handleEvent(Lifecycle.Event.ON_CREATE)
			lifecycleOwner.handleEvent(Lifecycle.Event.ON_START)
			lifecycleOwner.handleEvent(Lifecycle.Event.ON_RESUME)
		}

		waitUntil(timeoutMillis = 2_000) {
			collectedValues == listOf(TestValue(label = "record"))
		}

		runOnIdle {
			value.value = TestValue(label = "summary")
		}

		waitUntil(timeoutMillis = 2_000) {
			collectedValues == listOf(
				TestValue(label = "record"),
				TestValue(label = "summary")
			)
		}
	}

	@Test
	fun when_collectValueWithLifecycleRequiresActiveResumedEntry_then_defersAndReplaysLatestValue() = runTuIndiceUiTest {
		val lifecycleOwner = TestLifecycleOwner()
		val isActive = mutableStateOf(false)
		val value = mutableStateOf(TestValue(label = "record"))
		val collectedValues = mutableListOf<TestValue>()

		setTuIndiceTestContent {
			CollectValueWithLifecycle(
				value = value.value,
				lifecycle = lifecycleOwner.lifecycle,
				isActive = isActive.value
			) { collectedValue ->
				collectedValues += collectedValue
			}
		}

		runOnIdle {
			lifecycleOwner.handleEvent(Lifecycle.Event.ON_CREATE)
			lifecycleOwner.handleEvent(Lifecycle.Event.ON_START)
			value.value = TestValue(label = "summary")
			isActive.value = true
		}
		waitForIdle()

		assertTrue(collectedValues.isEmpty())

		runOnIdle {
			lifecycleOwner.handleEvent(Lifecycle.Event.ON_RESUME)
		}

		waitUntil(timeoutMillis = 2_000) {
			collectedValues == listOf(TestValue(label = "summary"))
		}

		runOnIdle {
			value.value = TestValue(label = "about")
		}

		waitUntil(timeoutMillis = 2_000) {
			collectedValues == listOf(
				TestValue(label = "summary"),
				TestValue(label = "about")
			)
		}

		runOnIdle {
			isActive.value = false
		}
		waitForIdle()

		runOnIdle {
			value.value = TestValue(label = "ignored")
		}
		waitForIdle()

		assertEquals(
			expected = listOf(
				TestValue(label = "summary"),
				TestValue(label = "about")
			),
			actual = collectedValues
		)

		runOnIdle {
			isActive.value = true
		}

		waitUntil(timeoutMillis = 2_000) {
			collectedValues == listOf(
				TestValue(label = "summary"),
				TestValue(label = "about"),
				TestValue(label = "ignored")
			)
		}
	}
}

private data class TestValue(
	val label: String
)

private class TestLifecycleOwner : LifecycleOwner {
	private val registry = LifecycleRegistry(this)

	override val lifecycle: Lifecycle
		get() = registry

	fun handleEvent(event: Lifecycle.Event) {
		registry.handleLifecycleEvent(event)
	}
}
