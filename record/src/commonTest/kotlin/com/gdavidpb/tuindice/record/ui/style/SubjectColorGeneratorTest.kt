package com.gdavidpb.tuindice.record.ui.style

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SubjectColorGeneratorTest {

	@Test
	fun when_codeHasTwoLettersAndFourDigits_then_returnsNonBlackColorPair() {
		val subjectColors = SubjectColorGenerator.fromCode("FS1113")

		assertTrue(subjectColors.color != Color.Black)
		assertTrue(subjectColors.containerColor != subjectColors.color)
	}

	@Test
	fun when_codeHasThreeLettersAndThreeDigits_then_returnsNonBlackColorPair() {
		val subjectColors = SubjectColorGenerator.fromCode("CSA211")

		assertTrue(subjectColors.color != Color.Black)
		assertTrue(subjectColors.containerColor != subjectColors.color)
	}

	@Test
	fun when_codeDoesNotMatchSupportedFormats_then_usesBlackAsCodeColor() {
		assertEquals(Color.Black, SubjectColorGenerator.fromCode("A1").color)
		assertEquals(Color.Black, SubjectColorGenerator.fromCode("INF-101").color)
		assertEquals(Color.Black, SubjectColorGenerator.fromCode("ABCD12").color)
	}
}
