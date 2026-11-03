package com.gdavidpb.tuindice.base.domain.model

import com.gdavidpb.tuindice.base.domain.exception.EncodedImageTooLargeException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs

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

	@Test
	fun checkEncodedSize_acceptsAnImageOfExactlyTheLimit() {
		val spec = ImageEncodingSpec(maxDimensionPx = 1024, maxBytes = 100, jpegQuality = 85)

		spec.checkEncodedSize(encodedBytes = 100)
	}

	@Test
	fun checkEncodedSize_refusesAnImageOneByteOverTheLimitWithItsSizes() {
		val spec = ImageEncodingSpec(maxDimensionPx = 1024, maxBytes = 100, jpegQuality = 85)

		val exception = assertFailsWith<EncodedImageTooLargeException> {
			spec.checkEncodedSize(encodedBytes = 101)
		}

		assertEquals(101, exception.encodedBytes)
		assertEquals(100, exception.maxBytes)
	}

	@Test
	fun checkEncodedSize_refusalIsAnIllegalStateNotAnInvalidArgument() {
		val spec = ImageEncodingSpec(maxDimensionPx = 1024, maxBytes = 100, jpegQuality = 85)

		val refusal: Throwable = assertFailsWith<IllegalStateException> {
			spec.checkEncodedSize(encodedBytes = 101)
		}

		// The profile picture flow names IllegalArgumentException as an invalid image, so the refusal must not be one.
		assertIs<EncodedImageTooLargeException>(refusal)
		assertFalse(refusal is IllegalArgumentException)
	}
}
