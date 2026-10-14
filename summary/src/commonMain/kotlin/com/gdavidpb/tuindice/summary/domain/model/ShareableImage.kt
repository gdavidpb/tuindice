package com.gdavidpb.tuindice.summary.domain.model

/**
 * An image ready to be shared: [content] is the PNG and [fileName] is the name it travels under. The
 * receiving app can see the name, so it must not carry personal data.
 */
class ShareableImage(
	val content: ByteArray,
	val fileName: String
) {
	init {
		require(content.isNotEmpty()) { "A shareable image needs content." }
		require(fileName.isNotBlank() && '/' !in fileName && '\\' !in fileName) {
			"The file name must be a plain name."
		}
	}
}
