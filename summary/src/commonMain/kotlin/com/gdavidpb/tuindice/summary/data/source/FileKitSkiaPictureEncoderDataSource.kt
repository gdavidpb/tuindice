package com.gdavidpb.tuindice.summary.data.source

import com.gdavidpb.tuindice.base.domain.model.EncodedImage
import com.gdavidpb.tuindice.summary.data.repository.user.PictureEncoderDataRepository
import com.gdavidpb.tuindice.summary.domain.exception.ProfilePictureIllegalArgumentException
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureUseCaseError
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
		const val OPAQUE_BACKGROUND_COLOR = 0xFFFFFFFF.toInt()
	}

	override suspend fun encodePicture(file: PlatformFile): EncodedImage {
		val accessGranted = file.startAccessingSecurityScopedResource()

		try {
			val content = file.readBytes()
			val decodedImage = runCatching {
				Image.makeFromEncoded(content)
			}.getOrElse {
				throw ProfilePictureIllegalArgumentException(ProfilePictureUseCaseError.NotImage)
			}

			try {
				if (decodedImage.width <= 0 || decodedImage.height <= 0) {
					throw ProfilePictureIllegalArgumentException(ProfilePictureUseCaseError.NotImage)
				}

				val normalizedImage = decodedImage.normalizeForProfilePicture()

				try {
					val encodedData = normalizedImage.encodeToData(
						format = EncodedImageFormat.JPEG,
						quality = Settings.JPEG_QUALITY
					)
						?: throw ProfilePictureIllegalArgumentException(ProfilePictureUseCaseError.NotImage)

					return EncodedImage(
						content = encodedData.bytes,
						mimeType = Settings.JPEG_MIME_TYPE
					)
				} finally {
					normalizedImage.close()
				}
			} finally {
				decodedImage.close()
			}
		} finally {
			if (accessGranted) {
				file.stopAccessingSecurityScopedResource()
			}
		}
	}

	private fun Image.normalizeForProfilePicture(): Image {
		val maxDimension = maxOf(width, height)
		val scaleFactor = if (maxDimension <= Settings.MAX_DIMENSION_PX) {
			1.0
		} else {
			Settings.MAX_DIMENSION_PX.toDouble() / maxDimension.toDouble()
		}
		val targetWidth = maxOf(1, (width * scaleFactor).toInt())
		val targetHeight = maxOf(1, (height * scaleFactor).toInt())
		val surface = Surface.makeRasterN32Premul(targetWidth, targetHeight)

		try {
			surface.canvas.clear(Settings.OPAQUE_BACKGROUND_COLOR)
			surface.canvas.drawImageRect(
				image = this,
				src = Rect.makeWH(width.toFloat(), height.toFloat()),
				dst = Rect.makeWH(targetWidth.toFloat(), targetHeight.toFloat())
			)

			return surface.makeImageSnapshot()
		} finally {
			surface.close()
		}
	}
}
