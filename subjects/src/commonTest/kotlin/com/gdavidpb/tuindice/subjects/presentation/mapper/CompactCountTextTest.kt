package com.gdavidpb.tuindice.subjects.presentation.mapper

import kotlin.test.Test
import kotlin.test.assertEquals

class CompactCountTextTest {
	@Test
	fun toCompactCountText_keepsPlainTextBelowOneThousand() {
		assertEquals("999", 999.toCompactCountText())
	}

	@Test
	fun toCompactCountText_formatsThousandsWithOneDecimal() {
		assertEquals("1.5k", 1534.toCompactCountText())
		assertEquals("10.8k", 10789.toCompactCountText())
	}

	@Test
	fun toCompactCountText_keepsSingleDecimalForExactThousands() {
		assertEquals("1k", 1000.toCompactCountText())
	}
}
