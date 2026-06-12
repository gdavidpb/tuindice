package com.gdavidpb.tuindice.base.ui.style

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CourseCodeColorGeneratorTest {
	@Test
	fun when_codeHasTwoLettersAndFourDigits_then_returnsNonBlackColorPair() {
		val subjectColors = CourseCodeColorGenerator.fromCode("FS1113")

		assertTrue(subjectColors.color != Color.Black)
		assertTrue(subjectColors.containerColor != subjectColors.color)
	}

	@Test
	fun when_codeHasThreeLettersAndThreeDigits_then_returnsNonBlackColorPair() {
		val subjectColors = CourseCodeColorGenerator.fromCode("CSA211")

		assertTrue(subjectColors.color != Color.Black)
		assertTrue(subjectColors.containerColor != subjectColors.color)
	}

	@Test
	fun when_codeDoesNotMatchSupportedFormats_then_usesNeutralFallbackColorPair() {
		val fallback = CourseCodeColorGenerator.fromCode("A1")

		assertTrue(fallback.color != Color.Black)
		assertTrue(fallback.containerColor != fallback.color)
		assertEquals(fallback, CourseCodeColorGenerator.fromCode("INF-101"))
		assertEquals(fallback, CourseCodeColorGenerator.fromCode("ABCD12"))
	}

	@Test
	fun when_codeDoesNotMatchSupportedFormats_then_nullableGeneratorReturnsNull() {
		assertEquals(null, CourseCodeColorGenerator.fromCodeOrNull("EA1"))
		assertEquals(null, CourseCodeColorGenerator.fromCodeOrNull("EG4"))
		assertEquals(null, CourseCodeColorGenerator.fromCodeOrNull("EL1"))
	}
}
