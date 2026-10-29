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

		source.markServerDisabled(60, source.sessionMark())

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

		source.markServerDisabled(10, source.sessionMark())
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

		source.markServerDisabled(30, source.sessionMark())
		runCurrent()

		assertFalse(seen.last().isVisible)
	}

	@Test
	fun clearSessionMemoryLiftsTheWindow() = runTest {
		val source = AssistantAvailabilityDataSource(FakeAssistantConfigRepository(), VirtualClock(testScheduler))
		source.markServerDisabled(60, source.sessionMark())

		source.clearSessionMemory()

		assertFalse(source.currentAvailability().serverDisabled)
	}

	@Test
	fun aMissingRetryAfterHoldsTheAssistantFifteenMinutes() = runTest {
		val source = AssistantAvailabilityDataSource(FakeAssistantConfigRepository(), VirtualClock(testScheduler))

		source.markServerDisabled(null, source.sessionMark())

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

		source.reportServerMode(AssistantMode.GUIDED, source.sessionMark())
		runCurrent()
		assertEquals(AssistantMode.GUIDED, seen.last().mode)
		assertEquals(AssistantMode.GUIDED, source.currentAvailability().mode)

		config.setMode(AssistantMode.OFF)
		runCurrent()
		assertEquals(AssistantMode.OFF, seen.last().mode)

		source.reportServerMode(AssistantMode.ON, source.sessionMark())
		runCurrent()
		assertEquals(AssistantMode.ON, seen.last().mode)

		source.clearSessionMemory()
		runCurrent()
		assertEquals(AssistantMode.OFF, seen.last().mode)
		assertEquals(AssistantMode.OFF, source.currentAvailability().mode)
	}

	// A switch-off the server answered for a session that has ended does not hold the one that follows.
	@Test
	fun aSwitchOffAnsweredForAnEndedSessionDoesNotHoldTheNextOne() = runTest {
		val source = AssistantAvailabilityDataSource(FakeAssistantConfigRepository(), VirtualClock(testScheduler))
		val mark = source.sessionMark()

		source.clearSessionMemory()
		source.markServerDisabled(60, mark)

		assertFalse(source.currentAvailability().serverDisabled)
	}

	// A mode the server reported for a session that has ended is not the mode of the student who follows.
	@Test
	fun aServerModeReportedForAnEndedSessionDoesNotReachTheNextOne() = runTest {
		val config = FakeAssistantConfigRepository(AssistantMode.ON)
		val source = AssistantAvailabilityDataSource(config, VirtualClock(testScheduler))
		val seen = mutableListOf<AssistantAvailability>()
		backgroundScope.launch { source.observeAvailability().collect { seen += it } }
		runCurrent()
		val mark = source.sessionMark()

		source.clearSessionMemory()
		source.reportServerMode(AssistantMode.GUIDED, mark)
		runCurrent()

		assertEquals(AssistantMode.ON, seen.last().mode)
		assertEquals(AssistantMode.ON, source.currentAvailability().mode)
	}

	// The chat sheet subscribes again right after the server's mode was applied: the configuration has not changed, so the
	// server's word stays. A change of the configuration is a new word on the mode, and the availability follows it.
	@Test
	fun aNewSubscriptionKeepsTheModeTheServerReported() = runTest {
		val config = FakeAssistantConfigRepository(AssistantMode.OFF)
		val source = AssistantAvailabilityDataSource(config, VirtualClock(testScheduler))
		val firstSheet = backgroundScope.launch { source.observeAvailability().collect {} }
		runCurrent()

		source.reportServerMode(AssistantMode.GUIDED, source.sessionMark())
		firstSheet.cancel()

		val seen = mutableListOf<AssistantAvailability>()
		backgroundScope.launch { source.observeAvailability().collect { seen += it } }
		runCurrent()

		// While it subscribes, the sheet sees only the server's word: nothing the configuration holds reaches it.
		assertEquals(listOf(AssistantMode.GUIDED), seen.map { it.mode })
		assertEquals(AssistantMode.GUIDED, seen.last().mode)
		assertEquals(AssistantMode.GUIDED, source.currentAvailability().mode)

		config.setMode(AssistantMode.ON)
		runCurrent()

		assertEquals(AssistantMode.ON, seen.last().mode)
	}
}
