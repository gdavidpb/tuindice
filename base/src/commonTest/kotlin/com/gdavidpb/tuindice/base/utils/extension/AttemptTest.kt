package com.gdavidpb.tuindice.base.utils.extension

import kotlinx.coroutines.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class AttemptTest {
	@Test
	fun aStepThatSucceeds_returnsItsValue() {
		assertEquals(7, attempt { 7 }.getOrNull())
	}

	@Test
	fun aStepThatFails_isSkipped_andTheFailureIsKept() {
		val failure = IllegalStateException("step failed")

		assertSame(failure, attempt { throw failure }.exceptionOrNull())
	}

	@Test
	fun aCancellation_isNotSwallowed() {
		assertFailsWith<CancellationException> { attempt { throw CancellationException("cancelled") } }
	}
}
