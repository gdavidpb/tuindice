package com.gdavidpb.tuindice.base.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ImageEncodingSpecTest {
	@Test
	fun init_acceptsTheLowestAndHighestValidLimits() {
		val lowest = ImageEncodingSpec(maxDimensionPx = 1, maxBytes = 1, jpegQuality = 1)
		val highest = ImageEncodingSpec(maxDimensionPx = 1024, maxBytes = 1_048_576, jpegQuality = 100)

		assertEquals(1, lowest.jpegQuality)
		assertEquals(100, highest.jpegQuality)
	}

	@Test
	fun init_rejectsMaxDimensionThatIsNotPositive() {
		assertFailsWith<IllegalArgumentException> {
			ImageEncodingSpec(maxDimensionPx = 0, maxBytes = 1_048_576, jpegQuality = 85)
		}

		assertFailsWith<IllegalArgumentException> {
			ImageEncodingSpec(maxDimensionPx = -1, maxBytes = 1_048_576, jpegQuality = 85)
		}
	}

	@Test
	fun init_rejectsMaxBytesThatIsNotPositive() {
		assertFailsWith<IllegalArgumentException> {
			ImageEncodingSpec(maxDimensionPx = 1024, maxBytes = 0, jpegQuality = 85)
		}
	}

	@Test
	fun init_rejectsJpegQualityOutsideOneToOneHundred() {
		assertFailsWith<IllegalArgumentException> {
			ImageEncodingSpec(maxDimensionPx = 1024, maxBytes = 1_048_576, jpegQuality = 0)
		}

		assertFailsWith<IllegalArgumentException> {
			ImageEncodingSpec(maxDimensionPx = 1024, maxBytes = 1_048_576, jpegQuality = 101)
		}
	}
}
