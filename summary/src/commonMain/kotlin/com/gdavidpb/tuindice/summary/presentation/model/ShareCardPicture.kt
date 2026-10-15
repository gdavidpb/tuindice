package com.gdavidpb.tuindice.summary.presentation.model

/**
 * The card of a closed term as the share sheet hands it on: a picture that is encoded as a PNG only when the share needs
 * its bytes. The encoding runs on the background job of the sheet, so the presentation never handles the platform's
 * image type.
 */
fun interface ShareCardPicture {
	fun encodePng(): ByteArray
}
