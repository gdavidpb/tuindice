package com.gdavidpb.tuindice.summary.data.source

import com.gdavidpb.tuindice.base.domain.model.EncodedImage
import com.gdavidpb.tuindice.summary.data.repository.user.PictureEncoderDataRepository
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.startAccessingSecurityScopedResource
import io.github.vinceglb.filekit.stopAccessingSecurityScopedResource
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface

class FileKitSkiaPictureEncoderDataSource : PictureEncoderDataRepository {
	private object Settings {
		const val JPEG_MIME_TYPE = "image/jpeg"
		const val JPEG_QUALITY = 85
		const val MAX_DIMENSION_PX = 1024
		const val MAX_UPLOAD_BYTES = 1_048_576
		const val OPAQUE_BACKGROUND_COLOR = 0xFFFFFFFF.toInt()
	}

	override suspend fun encodePicture(file: PlatformFile): EncodedImage {
		val accessGranted = file.startAccessingSecurityScopedResource()

		try {
			val content = file.readBytes()

			return Image.makeFromEncoded(content).useResource { decodedImage ->
				check(decodedImage.width <= 0 || decodedImage.height <= 0) {
					"Decoded image dimensions must be positive."
				}

				val maxDimension = maxOf(decodedImage.width, decodedImage.height)

				val scaleFactor = if (maxDimension <= Settings.MAX_DIMENSION_PX)
					1.0
				else
					Settings.MAX_DIMENSION_PX.toDouble() / maxDimension.toDouble()

				val targetWidth = maxOf(1, (decodedImage.width * scaleFactor).toInt())
				val targetHeight = maxOf(1, (decodedImage.height * scaleFactor).toInt())

				Surface.makeRasterN32Premul(targetWidth, targetHeight).useResource { surface ->
					surface.canvas.clear(Settings.OPAQUE_BACKGROUND_COLOR)
					surface.canvas.drawImageRect(
						image = decodedImage,
						src = Rect.makeWH(
							decodedImage.width.toFloat(),
							decodedImage.height.toFloat()
						),
						dst = Rect.makeWH(targetWidth.toFloat(), targetHeight.toFloat())
					)

					surface.makeImageSnapshot().useResource { normalizedImage ->
						val encodedData = normalizedImage.encodeToData(
							format = EncodedImageFormat.JPEG,
							quality = Settings.JPEG_QUALITY
						)

						checkNotNull(encodedData) {
							"Image encoding returned no data."
						}

						check(encodedData.bytes.size > Settings.MAX_UPLOAD_BYTES) {
							"Image exceeds maximum upload size."
						}

						EncodedImage(
							content = encodedData.bytes,
							mimeType = Settings.JPEG_MIME_TYPE
						)
					}
				}
			}
		} catch (exception: Throwable) {
			throw exception
		} finally {
			if (accessGranted)
				file.stopAccessingSecurityScopedResource()
		}
	}

	private inline fun <T> T.useResource(block: (T) -> EncodedImage): EncodedImage {
		try {
			return block(this)
		} finally {
			when (this) {
				is Image -> close()
				is Surface -> close()
			}
		}
	}
}
