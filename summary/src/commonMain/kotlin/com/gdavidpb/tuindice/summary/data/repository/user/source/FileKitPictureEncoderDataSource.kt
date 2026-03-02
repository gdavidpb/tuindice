package com.gdavidpb.tuindice.summary.data.repository.user.source

import com.gdavidpb.tuindice.base.domain.model.EncodedImage
import com.gdavidpb.tuindice.summary.data.repository.user.PictureEncoderDataSource
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.mimeType
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.startAccessingSecurityScopedResource
import io.github.vinceglb.filekit.stopAccessingSecurityScopedResource

class FileKitPictureEncoderDataSource : PictureEncoderDataSource {
	private object Settings {
		const val JPEG_MIME_TYPE = "image/jpeg"
		const val PNG_MIME_TYPE = "image/png"
	}

	override suspend fun encodePicture(file: PlatformFile): EncodedImage {
		val accessGranted = file.startAccessingSecurityScopedResource()

		try {
			val mimeType = file.mimeType()
				?.let { "${it.primaryType}/${it.subtype}" }
				?.lowercase()
				?: when (file.extension.lowercase()) {
					"png" -> Settings.PNG_MIME_TYPE
					else -> Settings.JPEG_MIME_TYPE
				}

			return EncodedImage(
				content = file.readBytes(),
				mimeType = mimeType
			)
		} finally {
			if (accessGranted) {
				file.stopAccessingSecurityScopedResource()
			}
		}
	}
}
