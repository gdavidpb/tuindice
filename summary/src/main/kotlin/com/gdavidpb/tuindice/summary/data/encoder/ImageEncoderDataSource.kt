package com.gdavidpb.tuindice.summary.data.encoder

import android.content.ContentResolver
import android.graphics.Bitmap
import androidx.core.net.toUri
import com.gdavidpb.tuindice.base.utils.extensions.encodeToBase64String
import com.gdavidpb.tuindice.summary.data.encoder.mappers.decodeRotationDegrees
import com.gdavidpb.tuindice.summary.data.encoder.mappers.decodeScaleFactor
import com.gdavidpb.tuindice.summary.data.encoder.mappers.decodeScaledBitmap
import com.gdavidpb.tuindice.summary.data.encoder.mappers.rotate
import com.gdavidpb.tuindice.summary.domain.repository.EncoderRepository
import java.io.ByteArrayOutputStream
import java.io.IOException

class ImageEncoderDataSource(
	private val contentResolver: ContentResolver
) : EncoderRepository {

	private object Settings {
		const val SAMPLE = 1024
		const val QUALITY = 90
	}

	override suspend fun encodePicture(path: String): String {
		val pictureUri = path.toUri()

		val inputStream = { contentResolver.openInputStream(pictureUri) }

		val rotationDegrees = inputStream()
			?.use { stream -> stream.decodeRotationDegrees() }
			?: throw IOException("Unable to get picture rotation degrees.")

		val scaleFactor = inputStream()
			?.use { stream -> stream.decodeScaleFactor(Settings.SAMPLE) }
			?: throw IOException("Unable to scale picture.")

		val encodedPicture = inputStream()
			?.use { stream ->
				stream
					.decodeScaledBitmap(scaleFactor)
					.rotate(rotationDegrees)
			}?.let { bitmap ->
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

		return encodedPicture ?: throw IOException("Unable to encode picture.")
	}
}