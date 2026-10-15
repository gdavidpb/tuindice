package com.gdavidpb.tuindice.base.presentation.model

import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TopBarActionBusTest {
	@Test
	fun dispatch_reachesEveryActionOfAListeningScreen() = runTest {
		val bus = TopBarActionBus<String>()
		val received = mutableListOf<String>()

		val listener = launch(UnconfinedTestDispatcher(testScheduler)) {
			bus.actions.collect { action -> received += action }
		}

		bus.dispatch("first")
		bus.dispatch("second")
		runCurrent()
		listener.cancel()

		assertEquals(listOf("first", "second"), received)
	}

	@Test
	fun dispatch_isNotKeptForAScreenThatListensLater() = runTest {
		val bus = TopBarActionBus<String>()
		val received = mutableListOf<String>()

		bus.dispatch("missed")

		val listener = launch(UnconfinedTestDispatcher(testScheduler)) {
			bus.actions.collect { action -> received += action }
		}

		bus.dispatch("seen")
		runCurrent()
		listener.cancel()

		assertEquals(listOf("seen"), received)
	}
}
