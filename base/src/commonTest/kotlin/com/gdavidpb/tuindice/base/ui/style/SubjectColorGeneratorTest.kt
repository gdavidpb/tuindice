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
	fun when_codeDoesNotMatchSupportedFormats_then_usesBlackAsCodeColor() {
		assertEquals(Color.Black, CourseCodeColorGenerator.fromCode("A1").color)
		assertEquals(Color.Black, CourseCodeColorGenerator.fromCode("INF-101").color)
		assertEquals(Color.Black, CourseCodeColorGenerator.fromCode("ABCD12").color)
	}
}
