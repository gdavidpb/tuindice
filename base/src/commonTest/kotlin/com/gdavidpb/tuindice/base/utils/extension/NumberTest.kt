package com.gdavidpb.tuindice.base.utils.extension

import kotlin.test.Test
import kotlin.test.assertEquals

class NumberTest {
	@Test
	fun truncateScaledDivision_avoidsBinaryFloatingPrecisionDriftAtFourDecimals() {
		assertEquals(
			4.52,
			truncateScaledDivision(
				numerator = 339L,
				denominator = 75L,
				decimals = 4
			)
		)
	}

	@Test
	fun formatGrade_keepsFourDecimalsVisible() {
		assertEquals("4.5200", (339.0 / 75.0).formatGrade(decimals = 4))
	}
}
