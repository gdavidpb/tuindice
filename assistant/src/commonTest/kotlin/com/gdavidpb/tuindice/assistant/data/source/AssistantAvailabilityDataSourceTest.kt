package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.testing.FakeAssistantConfigRepository
import com.gdavidpb.tuindice.assistant.testing.VirtualClock
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantAvailability
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class AssistantAvailabilityDataSourceTest {

	@Test
	fun serverDisabledHoldsTheAssistantOffUntilTheRetryAfter() = runTest {
		val source = AssistantAvailabilityDataSource(FakeAssistantConfigRepository(), VirtualClock(testScheduler))

		source.markServerDisabled(60)

		assertTrue(source.currentAvailability().serverDisabled)

		advanceTimeBy(59.seconds)

		assertTrue(source.currentAvailability().serverDisabled)

		advanceTimeBy(1.seconds)

		assertFalse(source.currentAvailability().serverDisabled)
	}

	@Test
	fun observedAvailabilityFollowsTheConfiguredModeAndTheWindow() = runTest {
		val config = FakeAssistantConfigRepository(AssistantMode.ON)
		val source = AssistantAvailabilityDataSource(config, VirtualClock(testScheduler))
		val seen = mutableListOf<AssistantAvailability>()
		backgroundScope.launch { source.observeAvailability().collect { seen += it } }
		runCurrent()

		assertEquals(AssistantAvailability(mode = AssistantMode.ON, serverDisabled = false), seen.last())

		source.markServerDisabled(10)
		runCurrent()

		assertEquals(AssistantAvailability(mode = AssistantMode.ON, serverDisabled = true), seen.last())

		advanceTimeBy(10.seconds)
		runCurrent()

		assertEquals(AssistantAvailability(mode = AssistantMode.ON, serverDisabled = false), seen.last())

		config.setMode(AssistantMode.GUIDED)
		runCurrent()

		assertEquals(AssistantMode.GUIDED, seen.last().mode)
		assertEquals(AssistantMode.GUIDED, source.currentAvailability().mode)
	}

	@Test
	fun aServerSwitchOffReadsAsOffWhateverTheMode() = runTest {
		val config = FakeAssistantConfigRepository(AssistantMode.ON)
		val source = AssistantAvailabilityDataSource(config, VirtualClock(testScheduler))
		val seen = mutableListOf<AssistantAvailability>()
		backgroundScope.launch { source.observeAvailability().collect { seen += it } }
		runCurrent()

		source.markServerDisabled(30)
		runCurrent()

		assertFalse(seen.last().isVisible)
	}

	@Test
	fun clearSessionMemoryLiftsTheWindow() = runTest {
		val source = AssistantAvailabilityDataSource(FakeAssistantConfigRepository(), VirtualClock(testScheduler))
		source.markServerDisabled(60)

		source.clearSessionMemory()

		assertFalse(source.currentAvailability().serverDisabled)
	}

	@Test
	fun aMissingRetryAfterHoldsTheAssistantFifteenMinutes() = runTest {
		val source = AssistantAvailabilityDataSource(FakeAssistantConfigRepository(), VirtualClock(testScheduler))

		source.markServerDisabled(null)

		advanceTimeBy(899.seconds)

		assertTrue(source.currentAvailability().serverDisabled)

		advanceTimeBy(1.seconds)

		assertFalse(source.currentAvailability().serverDisabled)
	}

	@Test
	fun theLastWordOnTheModeIsTheServersOrTheConfigurations_andTheSessionEndingBringsBackTheConfiguration() = runTest {
		val config = FakeAssistantConfigRepository(AssistantMode.ON)
		val source = AssistantAvailabilityDataSource(config, VirtualClock(testScheduler))
		val seen = mutableListOf<AssistantAvailability>()
		backgroundScope.launch { source.observeAvailability().collect { seen += it } }
		runCurrent()

		source.reportServerMode(AssistantMode.GUIDED)
		runCurrent()
		assertEquals(AssistantMode.GUIDED, seen.last().mode)
		assertEquals(AssistantMode.GUIDED, source.currentAvailability().mode)

		config.setMode(AssistantMode.OFF)
		runCurrent()
		assertEquals(AssistantMode.OFF, seen.last().mode)

		source.reportServerMode(AssistantMode.ON)
		runCurrent()
		assertEquals(AssistantMode.ON, seen.last().mode)

		source.clearSessionMemory()
		runCurrent()
		assertEquals(AssistantMode.OFF, seen.last().mode)
		assertEquals(AssistantMode.OFF, source.currentAvailability().mode)
	}
}
