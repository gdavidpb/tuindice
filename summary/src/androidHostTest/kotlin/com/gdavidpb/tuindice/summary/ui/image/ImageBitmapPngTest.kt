package com.gdavidpb.tuindice.summary.ui.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ImageBitmapPngTest {
	@Test
	fun toPngBytesEncodesAPngThatKeepsTheSizeOfTheBitmap() {
		val bytes = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888).asImageBitmap().toPngBytes()
		val decoded = assertNotNull(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))

		try {
			assertContentEquals(PNG_SIGNATURE, bytes.copyOf(PNG_SIGNATURE.size))
			assertEquals(WIDTH, decoded.width)
			assertEquals(HEIGHT, decoded.height)
		} finally {
			decoded.recycle()
		}
	}

	private companion object {
		const val WIDTH = 4
		const val HEIGHT = 3
		val PNG_SIGNATURE = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)
	}
}
