package com.gdavidpb.tuindice.summary.data.source.encoder

import android.content.ContentResolver
import android.graphics.Bitmap
import androidx.core.net.toUri
import com.gdavidpb.tuindice.base.utils.extensions.encodeToBase64String
import com.gdavidpb.tuindice.summary.data.source.EncoderDataSource
import com.gdavidpb.tuindice.summary.data.source.encoder.mappers.decodeRotationDegrees
import com.gdavidpb.tuindice.summary.data.source.encoder.mappers.decodeScaleFactor
import com.gdavidpb.tuindice.summary.data.source.encoder.mappers.decodeScaledBitmap
import com.gdavidpb.tuindice.summary.data.source.encoder.mappers.rotate
import java.io.ByteArrayOutputStream
import java.io.IOException

class ImageEncoderDataSource(
	private val contentResolver: ContentResolver
) : EncoderDataSource {

	private object Settings {
		const val SAMPLE = 1024
		const val QUALITY = 90
	}

	override suspend fun encodePicture(path: String): String {
		val pictureUri = path.toUri()

		val pictureInputStream = contentResolver.openInputStream(pictureUri)
			?: throw IOException("Unable to load picture.")

		val encodedPicture = pictureInputStream.use { inputStream ->
			val rotationDegrees = inputStream.decodeRotationDegrees()

			inputStream.reset()

			val scaleFactor = inputStream.decodeScaleFactor(Settings.SAMPLE)

			inputStream.reset()

			inputStream
				.decodeScaledBitmap(scaleFactor)
				.rotate(rotationDegrees)
		}.let { bitmap ->
			ByteArrayOutputStream().use { outputStream ->
				val compress = bitmap.compress(
					Bitmap.CompressFormat.JPEG,
					Settings.QUALITY,
					outputStream
				)

				if (compress)
					outputStream.flush()
				else
					throw IOException("Unable to compress picture.")

				bitmap.recycle()

				outputStream.toByteArray().encodeToBase64String()
			}
		}

		return encodedPicture
	}
}