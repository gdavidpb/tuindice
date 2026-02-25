package com.gdavidpb.tuindice.base.utils.extension

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExceptionExtensionsTest {
	@Test
	fun `isTimeout returns true for timeout-like class name`() {
		assertTrue(ConnectTimeoutException().isTimeout())
	}

	@Test
	fun `isConnection returns true when cause chain has connection error`() {
		val throwable = RuntimeException("outer", UnknownHostException())

		assertTrue(throwable.isConnection())
	}

	@Test
	fun `isConnection returns true for offline message`() {
		val throwable = RuntimeException("The Internet connection appears to be offline.")

		assertTrue(throwable.isConnection())
	}

	@Test
	fun `isConnection returns false for non-network error`() {
		assertFalse(IllegalStateException("random failure").isConnection())
	}
}

private class ConnectTimeoutException : RuntimeException()

private class UnknownHostException : RuntimeException()
