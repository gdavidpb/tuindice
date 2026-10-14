package com.gdavidpb.tuindice.summary.data.source

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import okio.Path
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.popoverPresentationController

/**
 * Presents the share sheet from the top-most view controller, as the About share does. The popover an
 * iPad needs points at the centre of the presenting view, because the sheet has no control to anchor to.
 */
@OptIn(ExperimentalForeignApi::class)
class UIKitImageSharePresenter : ImageSharePresenter {
	override fun present(file: Path) {
		val topController = checkNotNull(topViewController()) {
			"There is no view controller to present the share sheet from."
		}
		val image = checkNotNull(UIImage(contentsOfFile = file.toString())) {
			"The shared image could not be read."
		}
		val activityController = UIActivityViewController(
			activityItems = listOf(image),
			applicationActivities = null
		)

		// The sheet is a popover whose dismissal listens to the touches of the presenting side without
		// stopping them, so the tap that closes it also reaches the view under it. While the sheet is up
		// that view takes no touches, and it takes them back when the sheet ends, as About's share does.
		val presentingView = topController.view
		val wasInteractive = presentingView.userInteractionEnabled

		presentingView.userInteractionEnabled = false

		activityController.completionWithItemsHandler = { _, _, _, _ ->
			presentingView.userInteractionEnabled = wasInteractive
		}

		activityController.popoverPresentationController?.apply {
			sourceView = presentingView
			sourceRect = presentingView.bounds.useContents {
				CGRectMake(size.width / 2, size.height / 2, 1.0, 1.0)
			}
		}

		topController.presentViewController(
			viewControllerToPresent = activityController,
			animated = true,
			completion = null
		)
	}

	private fun topViewController(): UIViewController? {
		val keyWindow = UIApplication.sharedApplication
			.windows
			.firstOrNull { window ->
				(window as? UIWindow)?.isKeyWindow() == true
			} as? UIWindow

		var current = keyWindow?.rootViewController

		while (current?.presentedViewController != null) {
			current = current.presentedViewController
		}

		return current
	}
}
