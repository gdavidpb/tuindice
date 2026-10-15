package com.gdavidpb.tuindice.summary.data.repository

import okio.Path

/**
 * Shows the system share sheet for the image written at [file]. The iOS data source reaches the sheet
 * only through this contract, so its tests record the call instead of presenting UIKit.
 */
fun interface ImageSharePresenterDataRepository {
	fun present(file: Path)
}
