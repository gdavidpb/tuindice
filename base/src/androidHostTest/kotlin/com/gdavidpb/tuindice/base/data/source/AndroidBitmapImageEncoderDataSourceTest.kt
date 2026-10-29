package com.gdavidpb.tuindice.base.data.source

import com.gdavidpb.tuindice.base.domain.model.ImageEncodingSpec
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AndroidBitmapImageEncoderDataSourceTest {
	private val dataSource = AndroidBitmapImageEncoderDataSource()

	@Test
	fun encodeKeepsSmallImageSizeWhenImageIsSmallerThanMaxDimension() = runTest {
		val inputFile = createInputFile(
			prefix = "small",
			extension = "png",
			content = createPngBytes(width = 128, height = 128)
		)

		try {
			val encodedImage = dataSource.encode(file = PlatformFile(inputFile), spec = defaultSpec)

			assertEquals(JPEG_MIME_TYPE, encodedImage.mimeType)
			assertTrue(encodedImage.content.isNotEmpty())
			assertTrue(encodedImage.content.size <= defaultSpec.maxBytes)
			assertEncodedImageDimensions(
				content = encodedImage.content,
				expectedWidth = 128,
				expectedHeight = 128
			)
		} finally {
			inputFile.delete()
		}
	}

	@Test
	fun encodeScalesLargeImageToMaxDimension() = runTest {
		val inputFile = createInputFile(
			prefix = "large",
			extension = "png",
			content = createPngBytes(width = 2048, height = 1024)
		)

		try {
			val encodedImage = dataSource.encode(file = PlatformFile(inputFile), spec = defaultSpec)

			assertEquals(JPEG_MIME_TYPE, encodedImage.mimeType)
			assertTrue(encodedImage.content.isNotEmpty())
			assertTrue(encodedImage.content.size <= defaultSpec.maxBytes)
			assertEncodedImageDimensions(
				content = encodedImage.content,
				expectedWidth = 1024,
				expectedHeight = 512
			)
		} finally {
			inputFile.delete()
		}
	}

	@Test
	fun encodeScalesLargeImageToSmallerMaxDimension() = runTest {
		val inputFile = createInputFile(
			prefix = "smaller",
			extension = "png",
			content = createPngBytes(width = 2048, height = 1024)
		)

		try {
			val encodedImage = dataSource.encode(file = PlatformFile(inputFile), spec = smallDimensionSpec)

			assertEquals(JPEG_MIME_TYPE, encodedImage.mimeType)
			assertTrue(encodedImage.content.size <= smallDimensionSpec.maxBytes)
			assertEncodedImageDimensions(
				content = encodedImage.content,
				expectedWidth = 256,
				expectedHeight = 128
			)
		} finally {
			inputFile.delete()
		}
	}

	@Test
	fun encodeThrowsIllegalStateWhenEncodedImageExceedsMaxBytes() = runTest {
		val inputFile = createInputFile(
			prefix = "heavy",
			extension = "png",
			content = createPngBytes(width = 128, height = 128)
		)

		try {
			assertFailsWith<IllegalStateException> {
				dataSource.encode(file = PlatformFile(inputFile), spec = tinyBytesSpec)
			}
		} finally {
			inputFile.delete()
		}
	}

	@Test
	fun encodeThrowsIllegalArgumentWhenContentIsNotImage() = runTest {
		val inputFile = createInputFile(
			prefix = "invalid",
			extension = "bin",
			content = "not an image".encodeToByteArray()
		)

		try {
			assertFailsWith<IllegalArgumentException> {
				dataSource.encode(file = PlatformFile(inputFile), spec = defaultSpec)
			}
		} finally {
			inputFile.delete()
		}
	}

	private fun assertEncodedImageDimensions(
		content: ByteArray,
		expectedWidth: Int,
		expectedHeight: Int
	) {
		val decodedImage = decodeImage(content)

		try {
			assertEquals(expectedWidth, decodedImage.width)
			assertEquals(expectedHeight, decodedImage.height)
		} finally {
			decodedImage.recycle()
		}
	}

	private companion object {
		const val JPEG_MIME_TYPE = "image/jpeg"
		const val MAX_BYTES = 1_048_576
		const val TINY_MAX_BYTES = 16

		val defaultSpec = ImageEncodingSpec(maxDimensionPx = 1024, maxBytes = MAX_BYTES, jpegQuality = 85)
		val smallDimensionSpec = ImageEncodingSpec(maxDimensionPx = 256, maxBytes = MAX_BYTES, jpegQuality = 70)
		val tinyBytesSpec = ImageEncodingSpec(maxDimensionPx = 1024, maxBytes = TINY_MAX_BYTES, jpegQuality = 85)
	}
}
