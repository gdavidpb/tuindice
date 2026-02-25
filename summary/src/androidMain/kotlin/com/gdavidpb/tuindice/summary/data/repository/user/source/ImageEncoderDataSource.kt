package com.gdavidpb.tuindice.summary.data.repository.user.source

import android.content.ContentResolver
import android.graphics.Bitmap
import androidx.core.net.toUri
import com.gdavidpb.tuindice.base.domain.model.EncodedImage
import com.gdavidpb.tuindice.base.domain.model.PlatformUri
import com.gdavidpb.tuindice.summary.data.repository.user.source.encoder.mapper.decodeRotationDegrees
import com.gdavidpb.tuindice.summary.data.repository.user.source.encoder.mapper.decodeScaleFactor
import com.gdavidpb.tuindice.summary.data.repository.user.source.encoder.mapper.decodeScaledBitmap
import com.gdavidpb.tuindice.summary.data.repository.user.source.encoder.mapper.rotate
import com.gdavidpb.tuindice.summary.domain.repository.EncoderRepository
import java.io.ByteArrayOutputStream
import java.io.IOException

class ImageEncoderDataSource(
	private val contentResolver: ContentResolver
) : EncoderRepository {

	private object Settings {
		const val SAMPLE = 1024
		const val QUALITY = 90
		const val MIME_TYPE = "image/jpeg"
	}

	override suspend fun encodePicture(uri: PlatformUri): EncodedImage {
		val pictureUri = uri.value.toUri()

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
			}
			?.let { bitmap ->
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

					outputStream
				}
			}

		return encodedPicture
			?.toByteArray()
			?.let { bytes ->
				EncodedImage(
					content = bytes,
					mimeType = Settings.MIME_TYPE
				)
			}
			?: throw IOException("Unable to encode picture.")
	}
}
