package com.gdavidpb.tuindice.summary.data.source.user


import com.gdavidpb.tuindice.summary.domain.exception.ProfilePictureIllegalArgumentException
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureUseCaseError
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.Surface
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class FileKitSkiaPictureEncoderDataSourceTest {
	private val dataSource = FileKitSkiaPictureEncoderDataSource()

	@Test
	fun encode_reencodesValidImageAsJpeg() {
		val encodedImage = dataSource.encodeContent(content = createPngBytes(width = 128, height = 128))

		assertEquals("image/jpeg", encodedImage.mimeType)
		assertTrue(encodedImage.content.size > 2)
		assertEquals(0xFF.toByte(), encodedImage.content[0])
		assertEquals(0xD8.toByte(), encodedImage.content[1])
	}

	@Test
	fun encode_resizesLargeImagesToConfiguredMaxDimension() {
		val encodedImage = dataSource.encodeContent(content = createPngBytes(width = 2048, height = 1024))
		val decodedImage = Image.makeFromEncoded(encodedImage.content)

		try {
			assertEquals(1024, decodedImage.width)
			assertEquals(512, decodedImage.height)
		} finally {
			decodedImage.close()
		}
	}

	@Test
	fun encode_throwsNotImageForInvalidBytes() {
		val exception = assertFailsWith<ProfilePictureIllegalArgumentException> {
			dataSource.encodeContent(content = byteArrayOf(1, 2, 3))
		}

		assertEquals(ProfilePictureUseCaseError.NotImage, exception.error)
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
}
