package com.gdavidpb.tuindice.summary.data.source

import com.gdavidpb.tuindice.base.domain.model.EncodedImage
import com.gdavidpb.tuindice.base.domain.model.PlatformUri
import com.gdavidpb.tuindice.summary.domain.repository.EncoderRepository
import okio.FileSystem
import okio.Path.Companion.toPath

class IosImageEncoderDataSource : EncoderRepository {
	override suspend fun encodePicture(uri: PlatformUri): EncodedImage {
		val path = uri.value
			.removePrefix("file://")
			.toPath()

		val content = FileSystem.SYSTEM.read(path) {
			readByteArray()
		}

		return EncodedImage(
			content = content,
			mimeType = path.mimeType()
		)
	}
}

private fun okio.Path.mimeType(): String {
	return when (name.substringAfterLast('.', "").lowercase()) {
		"png" -> "image/png"
		"webp" -> "image/webp"
		else -> "image/jpeg"
	}
}
