package com.gdavidpb.tuindice.base.data.source

import com.gdavidpb.tuindice.base.domain.model.ImageEncodingSpec
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.test.runTest
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.Surface
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class FileKitSkiaImageEncoderDataSourceTest {
	private val dataSource = FileKitSkiaImageEncoderDataSource()

	@Test
	fun encode_keepsSmallImageSize_whenImageIsSmallerThanMaxDimension() = runTest {
		val inputFile = createInputFile(
			prefix = "small",
			extension = "png",
			content = createPngBytes(width = 128, height = 128)
		)

		try {
			val encodedImage = dataSource.encode(file = inputFile, spec = defaultSpec)

			assertEquals(JPEG_MIME_TYPE, encodedImage.mimeType)
			assertTrue(encodedImage.content.isNotEmpty())
			assertTrue(encodedImage.content.size <= defaultSpec.maxBytes)
			assertEncodedImageDimensions(
				content = encodedImage.content,
				expectedWidth = 128,
				expectedHeight = 128
			)
		} finally {
			deleteInputFileIfExists(inputFile)
		}
	}

	@Test
	fun encode_scalesLargeImageToMaxDimension() = runTest {
		val inputFile = createInputFile(
			prefix = "large",
			extension = "png",
			content = createPngBytes(width = 2048, height = 1024)
		)

		try {
			val encodedImage = dataSource.encode(file = inputFile, spec = defaultSpec)

			assertEquals(JPEG_MIME_TYPE, encodedImage.mimeType)
			assertTrue(encodedImage.content.isNotEmpty())
			assertTrue(encodedImage.content.size <= defaultSpec.maxBytes)
			assertEncodedImageDimensions(
				content = encodedImage.content,
				expectedWidth = 1024,
				expectedHeight = 512
			)
		} finally {
			deleteInputFileIfExists(inputFile)
		}
	}

	@Test
	fun encode_scalesLargeImageToSmallerMaxDimension() = runTest {
		val inputFile = createInputFile(
			prefix = "smaller",
			extension = "png",
			content = createPngBytes(width = 2048, height = 1024)
		)

		try {
			val encodedImage = dataSource.encode(file = inputFile, spec = smallDimensionSpec)

			assertEquals(JPEG_MIME_TYPE, encodedImage.mimeType)
			assertTrue(encodedImage.content.size <= smallDimensionSpec.maxBytes)
			assertEncodedImageDimensions(
				content = encodedImage.content,
				expectedWidth = 256,
				expectedHeight = 128
			)
		} finally {
			deleteInputFileIfExists(inputFile)
		}
	}

	@Test
	fun encode_throwsIllegalStateWhenEncodedImageExceedsMaxBytes() = runTest {
		val inputFile = createInputFile(
			prefix = "heavy",
			extension = "png",
			content = createPngBytes(width = 128, height = 128)
		)

		try {
			assertFailsWith<IllegalStateException> {
				dataSource.encode(file = inputFile, spec = tinyBytesSpec)
			}
		} finally {
			deleteInputFileIfExists(inputFile)
		}
	}

	@Test
	fun encode_throwsIllegalArgumentWhenContentIsNotImage() = runTest {
		val inputFile = createInputFile(
			prefix = "invalid",
			extension = "bin",
			content = byteArrayOf(1, 2, 3)
		)

		try {
			assertFailsWith<IllegalArgumentException> {
				dataSource.encode(file = inputFile, spec = defaultSpec)
			}
		} finally {
			deleteInputFileIfExists(inputFile)
		}
	}

	private fun assertEncodedImageDimensions(
		content: ByteArray,
		expectedWidth: Int,
		expectedHeight: Int
	) {
		val decodedImage = Image.makeFromEncoded(content)

		try {
			assertEquals(expectedWidth, decodedImage.width)
			assertEquals(expectedHeight, decodedImage.height)
		} finally {
			decodedImage.close()
		}
	}

	private fun createPngBytes(width: Int, height: Int): ByteArray {
		val surface = Surface.makeRasterN32Premul(width, height)

		try {
			surface.canvas.clear(TEST_COLOR)

			val snapshot = surface.makeImageSnapshot()
			try {
				return checkNotNull(snapshot.encodeToData(EncodedImageFormat.PNG)) {
					"Expected a PNG-encoded image for tests."
				}.bytes
			} finally {
				snapshot.close()
			}
		} finally {
			surface.close()
		}
	}

	private suspend fun createInputFile(
		prefix: String,
		extension: String,
		content: ByteArray
	): PlatformFile {
		val directory = FileKit.filesDir / "baseImageEncoderTests"
		val file = directory / "${prefix}_${Random.nextInt(1_000_000)}.$extension"

		directory.createDirectories()
		file.write(content)

		return file
	}

	private suspend fun deleteInputFileIfExists(file: PlatformFile) {
		if (file.exists()) {
			file.delete(mustExist = false)
		}
	}

	private companion object {
		const val JPEG_MIME_TYPE = "image/jpeg"
		const val MAX_BYTES = 1_048_576
		const val TINY_MAX_BYTES = 16
		const val TEST_COLOR = 0xFF447799.toInt()

		val defaultSpec = ImageEncodingSpec(maxDimensionPx = 1024, maxBytes = MAX_BYTES, jpegQuality = 85)
		val smallDimensionSpec = ImageEncodingSpec(maxDimensionPx = 256, maxBytes = MAX_BYTES, jpegQuality = 70)
		val tinyBytesSpec = ImageEncodingSpec(maxDimensionPx = 1024, maxBytes = TINY_MAX_BYTES, jpegQuality = 85)
	}
}
