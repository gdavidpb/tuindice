package com.gdavidpb.tuindice.summary.ui.image

import androidx.compose.ui.graphics.ImageBitmap
import org.jetbrains.skia.Image
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ImageBitmapPngTest {
	@Test
	fun toPngBytesEncodesAPngThatKeepsTheSizeOfTheBitmap() {
		val bytes = ImageBitmap(width = WIDTH, height = HEIGHT).toPngBytes()
		val decoded = Image.makeFromEncoded(bytes)

		try {
			assertContentEquals(PNG_SIGNATURE, bytes.copyOf(PNG_SIGNATURE.size))
			assertEquals(WIDTH, decoded.width)
			assertEquals(HEIGHT, decoded.height)
		} finally {
			decoded.close()
		}
	}

	private companion object {
		const val WIDTH = 4
		const val HEIGHT = 3
		val PNG_SIGNATURE = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)
	}
}
