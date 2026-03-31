package com.gdavidpb.tuindice.summary.data.source

import com.gdavidpb.tuindice.base.domain.model.EncodedImage
import com.gdavidpb.tuindice.summary.data.contract.user.PictureEncoderDataSource
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

class FileKitSkiaPictureEncoderDataSource : PictureEncoderDataSource {
	private object Settings {
		const val JPEG_MIME_TYPE = "image/jpeg"
		const val JPEG_QUALITY = 85
		const val MAX_DIMENSION_PX = 1024
		const val OPAQUE_BACKGROUND_COLOR = 0xFFFFFFFF.toInt()
	}

	private data class ImageSize(
		val width: Int,
		val height: Int
	)

	override suspend fun encodePicture(file: PlatformFile): EncodedImage {
		val accessGranted = file.startAccessingSecurityScopedResource()

		try {
			return encodeContent(content = file.readBytes())
		} finally {
			if (accessGranted) {
				file.stopAccessingSecurityScopedResource()
			}
		}
	}

	internal fun encodeContent(content: ByteArray): EncodedImage {
		val decodedImage = decodeImageOrThrow(content = content)

		try {
			val normalizedImage = decodedImage.normalizeForProfilePicture()

			try {
				val encodedData = normalizedImage.encodeToData(
					format = EncodedImageFormat.JPEG,
					quality = Settings.JPEG_QUALITY
				) ?: throw ProfilePictureIllegalArgumentException(ProfilePictureUseCaseError.NotImage)

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
	}

	private fun decodeImageOrThrow(content: ByteArray): Image {
		val image = runCatching { Image.makeFromEncoded(content) }
			.getOrElse { throw ProfilePictureIllegalArgumentException(ProfilePictureUseCaseError.NotImage) }

		if (image.width <= 0 || image.height <= 0) {
			image.close()
			throw ProfilePictureIllegalArgumentException(ProfilePictureUseCaseError.NotImage)
		}

		return image
	}

	private fun Image.normalizeForProfilePicture(): Image {
		val targetSize = calculateTargetSize(width = width, height = height)
		val surface = Surface.makeRasterN32Premul(targetSize.width, targetSize.height)

		try {
			surface.canvas.clear(Settings.OPAQUE_BACKGROUND_COLOR)
			surface.canvas.drawImageRect(
				image = this,
				src = Rect.makeWH(width.toFloat(), height.toFloat()),
				dst = Rect.makeWH(targetSize.width.toFloat(), targetSize.height.toFloat())
			)

			return surface.makeImageSnapshot()
		} finally {
			surface.close()
		}
	}

	private fun calculateTargetSize(width: Int, height: Int): ImageSize {
		require(width > 0) { "Image width must be positive." }
		require(height > 0) { "Image height must be positive." }

		val maxDimension = maxOf(width, height)

		if (maxDimension <= Settings.MAX_DIMENSION_PX) {
			return ImageSize(width = width, height = height)
		}

		val scaleFactor = Settings.MAX_DIMENSION_PX.toDouble() / maxDimension.toDouble()

		return ImageSize(
			width = maxOf(1, (width * scaleFactor).toInt()),
			height = maxOf(1, (height * scaleFactor).toInt())
		)
	}
}
