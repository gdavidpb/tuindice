package com.gdavidpb.tuindice.summary.data.source

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

class FileKitSkiaPictureEncoderDataSourceTest {
	private val dataSource = FileKitSkiaPictureEncoderDataSource()

	@Test
	fun encode_encodesJpegWhenDecodedImageHasPositiveDimensions() = runTest {
		val inputFile = createInputFile(
			prefix = "valid",
			extension = "png",
			content = createPngBytes(width = 128, height = 128)
		)

		try {
			val encodedImage = dataSource.encodePicture(file = inputFile)

			assertEquals("image/jpeg", encodedImage.mimeType)
			assertTrue(encodedImage.content.isNotEmpty())
			assertTrue(encodedImage.content.size <= 1_048_576)
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
	fun encode_resizesBeforeEncodingWhenImageExceedsMaxDimension() = runTest {
		val inputFile = createInputFile(
			prefix = "large",
			extension = "png",
			content = createPngBytes(width = 2048, height = 1024)
		)

		try {
			val encodedImage = dataSource.encodePicture(file = inputFile)

			assertEquals("image/jpeg", encodedImage.mimeType)
			assertTrue(encodedImage.content.isNotEmpty())
			assertTrue(encodedImage.content.size <= 1_048_576)
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
	fun encode_throwsNotImageForInvalidBytes() = runTest {
		val inputFile = createInputFile(
			prefix = "invalid",
			extension = "bin",
			content = byteArrayOf(1, 2, 3)
		)

		try {
			assertFailsWith<IllegalArgumentException> {
				dataSource.encodePicture(file = inputFile)
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
			surface.canvas.clear(0xFF447799.toInt())

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
		val directory = FileKit.filesDir / "summaryTests"
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
}
