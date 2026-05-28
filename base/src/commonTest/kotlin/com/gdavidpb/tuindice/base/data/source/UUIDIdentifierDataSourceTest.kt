package com.gdavidpb.tuindice.base.data.source

import kotlin.test.Test
import kotlin.test.assertTrue

class UUIDIdentifierDataSourceTest {
	@Test
	fun generateRandomIdentifier_returnsLowercaseHex32() {
		val identifier = UUIDIdentifierDataSource().generateRandomIdentifier()

		assertTrue(LOWERCASE_HEX_32.matches(identifier))
	}

	private companion object {
		val LOWERCASE_HEX_32 = Regex("^[0-9a-f]{32}$")
	}
}
