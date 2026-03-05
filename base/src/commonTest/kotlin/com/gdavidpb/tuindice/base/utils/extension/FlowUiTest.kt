package com.gdavidpb.tuindice.base.utils.extension

import androidx.compose.ui.test.ExperimentalTestApi
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class FlowUiTest {
	@Test
	fun when_collectEffectWithLifecycleReceivesStateFlow_then_collectsEmittedValues() = runTuIndiceUiTest {
		val flow = MutableStateFlow(1)
		val collectedValues = mutableListOf<Int>()

		setTuIndiceTestContent {
			CollectEffectWithLifecycle(flow = flow) { value ->
				collectedValues.add(value)
			}
		}

		waitUntil(timeoutMillis = 2_000) {
			collectedValues.contains(1)
		}

		runOnIdle {
			flow.value = 2
		}

		waitUntil(timeoutMillis = 2_000) {
			collectedValues.contains(2)
		}

		assertTrue(collectedValues.size >= 2)
	}

	@Test
	fun when_collectEffectWithLifecycleUsesStringFlow_then_collectsUpdatedString() = runTuIndiceUiTest {
		val flow = MutableStateFlow("inicio")
		val collectedValues = mutableListOf<String>()

		setTuIndiceTestContent {
			CollectEffectWithLifecycle(flow = flow) { value ->
				collectedValues.add(value)
			}
		}

		waitUntil(timeoutMillis = 2_000) {
			collectedValues.contains("inicio")
		}

		runOnIdle {
			flow.value = "actualizado"
		}

		waitUntil(timeoutMillis = 2_000) {
			collectedValues.contains("actualizado")
		}

		assertTrue(collectedValues.size >= 2)
	}
}
