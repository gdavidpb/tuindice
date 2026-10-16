package com.gdavidpb.tuindice.summary.ui.image

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

actual fun ImageBitmap.toPngBytes(): ByteArray {
	val image = Image.makeFromBitmap(asSkiaBitmap())

	try {
		val encodedData = checkNotNull(image.encodeToData(EncodedImageFormat.PNG)) {
			"Image encoding returned no data."
		}

		return encodedData.bytes
	} finally {
		image.close()
	}
}
