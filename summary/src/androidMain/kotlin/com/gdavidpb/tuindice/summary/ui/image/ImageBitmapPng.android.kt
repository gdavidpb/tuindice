package com.gdavidpb.tuindice.summary.ui.image

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import java.io.ByteArrayOutputStream

private const val PNG_QUALITY = 100

actual fun ImageBitmap.toPngBytes(): ByteArray {
	val output = ByteArrayOutputStream()

	check(asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, output)) {
		"Image encoding returned no data."
	}

	return output.toByteArray()
}
