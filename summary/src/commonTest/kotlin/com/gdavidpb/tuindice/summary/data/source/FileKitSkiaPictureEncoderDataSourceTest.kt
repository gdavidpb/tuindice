package com.gdavidpb.tuindice.summary.data.source

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.write
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Surface
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FileKitSkiaPictureEncoderDataSourceTest {
	private val dataSource = FileKitSkiaPictureEncoderDataSource()

	@Test
	fun encode_throwsWhenDecodedImageHasPositiveDimensions() = runTest {
		val inputFile = createInputFile(
			prefix = "valid",
			extension = "png",
			content = createPngBytes(width = 128, height = 128)
		)

		try {
			val exception = assertFailsWith<IllegalStateException> {
				dataSource.encodePicture(file = inputFile)
			}

			assertEquals("Decoded image dimensions must be positive.", exception.message)
		} finally {
			deleteInputFileIfExists(inputFile)
		}
	}

	@Test
	fun encode_throwsBeforeResizeWhenDecodedImageHasPositiveDimensions() = runTest {
		val inputFile = createInputFile(
			prefix = "large",
			extension = "png",
			content = createPngBytes(width = 2048, height = 1024)
		)

		try {
			val exception = assertFailsWith<IllegalStateException> {
				dataSource.encodePicture(file = inputFile)
			}

			assertEquals("Decoded image dimensions must be positive.", exception.message)
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
	) = (FileKit.filesDir / "summaryTests" / "${prefix}_${Random.nextInt(1_000_000)}.$extension").also { file ->
		(FileKit.filesDir / "summaryTests").createDirectories()
		file.write(content)
	}

	private suspend fun deleteInputFileIfExists(file: io.github.vinceglb.filekit.PlatformFile) {
		if (file.exists()) {
			file.delete(mustExist = false)
		}
	}
}
