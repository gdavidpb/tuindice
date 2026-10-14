package com.gdavidpb.tuindice.summary.data.source

import com.gdavidpb.tuindice.base.ui.presentActivitySheet
import okio.Path
import platform.UIKit.UIImage

/** Presents the share sheet for the image, through the same sheet as every other share of the app. */
class UIKitImageSharePresenter : ImageSharePresenter {
	override fun present(file: Path) {
		val image = checkNotNull(UIImage(contentsOfFile = file.toString())) {
			"The shared image could not be read."
		}

		check(presentActivitySheet(activityItems = listOf(image))) {
			"There is no view controller to present the share sheet from."
		}
	}
}
