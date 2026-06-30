package com.gdavidpb.tuindice.base.presentation.statemachine

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InitialContentRefreshGateTest {
	private enum class TestEvent {
		Cached,
		RefreshStarted,
		RefreshSucceeded,
	}

	@Test
	fun cachedContent_suppressesInitialRefreshStartedAndError() {
		val gate = createGate()

		assertFalse(gate.shouldProcess(TestEvent.Cached))
		assertFalse(gate.shouldProcess(TestEvent.RefreshStarted))
		assertFalse(gate.shouldProcessError())
	}

	@Test
	fun missingContent_processesRefreshStartedAndError() {
		val gate = createGate()

		assertTrue(gate.shouldProcess(TestEvent.RefreshStarted))
		assertTrue(gate.shouldProcessError())
	}

	@Test
	fun cachedContent_keepsRefreshSucceededProcessable() {
		val gate = createGate()

		assertFalse(gate.shouldProcess(TestEvent.Cached))
		assertTrue(gate.shouldProcess(TestEvent.RefreshSucceeded))
	}

	private fun createGate(): InitialContentRefreshGate<TestEvent> {
		return InitialContentRefreshGate(
			isCached = { event -> event == TestEvent.Cached },
			isRefreshStarted = { event -> event == TestEvent.RefreshStarted }
		)
	}
}
