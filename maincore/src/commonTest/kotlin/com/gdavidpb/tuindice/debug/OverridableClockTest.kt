package com.gdavidpb.tuindice.debug

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class OverridableClockTest {
	private val delegateNow = Instant.parse("2030-01-01T00:00:00Z")
	private val delegate = object : Clock {
		override fun now(): Instant = delegateNow
	}

	@Test
	fun withoutAnOverride_readsTheDelegate() {
		assertEquals(delegateNow, OverridableClock(delegate).now())
	}

	@Test
	fun withAnOverride_answersItAndKeepsAnsweringIt() {
		val clock = OverridableClock(delegate)
		val fixed = Instant.parse("2026-10-15T12:00:00Z")

		clock.fixedNow = fixed

		assertEquals(fixed, clock.now())
		assertEquals(fixed, clock.now())
	}

	@Test
	fun clearingTheOverride_goesBackToTheDelegate() {
		val clock = OverridableClock(delegate)

		clock.fixedNow = Instant.parse("2026-10-15T12:00:00Z")
		clock.fixedNow = null

		assertEquals(delegateNow, clock.now())
	}

	@Test
	fun theDefaultDelegateIsTheSystemClock() {
		val before = Clock.System.now()
		val read = OverridableClock().now()

		assertTrue(read >= before)
	}
}
