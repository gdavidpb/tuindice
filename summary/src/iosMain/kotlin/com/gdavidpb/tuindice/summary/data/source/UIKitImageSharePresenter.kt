package com.gdavidpb.tuindice.summary.data.source

import com.gdavidpb.tuindice.base.ui.presentActivitySheet
import okio.Path
import platform.Foundation.NSURL
import platform.UIKit.UIImage

/**
 * Presents the share sheet for the image, through the same sheet as every other share of the app, with the image as
 * the preview of its header.
 */
class UIKitImageSharePresenter : ImageSharePresenter {
	override fun present(file: Path) {
		val image = checkNotNull(UIImage(contentsOfFile = file.toString())) {
			"The shared image could not be read."
		}

		// With its metadata the sheet's header previews the card, not a generic icon.
		val item = ShareImageItemSource(image = image, file = NSURL.fileURLWithPath(file.toString()))

		check(presentActivitySheet(activityItems = listOf(item))) {
			"There is no view controller to present the share sheet from."
		}
	}
}
