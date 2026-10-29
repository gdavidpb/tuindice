package com.gdavidpb.tuindice.base.data.source

import com.gdavidpb.tuindice.base.domain.model.EncodedImage
import com.gdavidpb.tuindice.base.domain.model.ImageEncodingSpec
import com.gdavidpb.tuindice.base.domain.repository.ImageEncoderRepository
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.startAccessingSecurityScopedResource
import io.github.vinceglb.filekit.stopAccessingSecurityScopedResource
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface

class FileKitSkiaImageEncoderDataSource : ImageEncoderRepository {
	private object Settings {
		const val JPEG_MIME_TYPE = "image/jpeg"
		const val OPAQUE_BACKGROUND_COLOR = 0xFFFFFFFF.toInt()
	}

	override suspend fun encode(file: PlatformFile, spec: ImageEncodingSpec): EncodedImage {
		val accessGranted = file.startAccessingSecurityScopedResource()

		try {
			val content = file.readBytes()

			return Image.makeFromEncoded(content).useResource { decodedImage ->
				check(decodedImage.width > 0 && decodedImage.height > 0) {
					"Decoded image dimensions must be positive."
				}

				val maxDimension = maxOf(decodedImage.width, decodedImage.height)

				val scaleFactor = if (maxDimension <= spec.maxDimensionPx) {
					1.0
				} else {
					spec.maxDimensionPx.toDouble() / maxDimension.toDouble()
				}

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
							quality = spec.jpegQuality
						)

						checkNotNull(encodedData) {
							"Image encoding returned no data."
						}

						check(encodedData.bytes.size <= spec.maxBytes) {
							"Image exceeds the maximum size of ${spec.maxBytes} bytes."
						}

						EncodedImage(
							content = encodedData.bytes,
							mimeType = Settings.JPEG_MIME_TYPE
						)
					}
				}
			}
		} finally {
			if (accessGranted) {
				file.stopAccessingSecurityScopedResource()
			}
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
