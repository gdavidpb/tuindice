package com.gdavidpb.tuindice.base.ui

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.popoverPresentationController

/**
 * Presents the system share sheet for [activityItems] from the top-most view controller, the one every share of the
 * app goes through. Returns false, presenting nothing, when there is no view controller to present it from.
 */
@OptIn(ExperimentalForeignApi::class)
fun presentActivitySheet(activityItems: List<Any>): Boolean {
	val topController = topViewController() ?: return false
	val activityController = UIActivityViewController(
		activityItems = activityItems,
		applicationActivities = null
	)

	// The sheet is a popover whose dismissal listens to the touches of the presenting side without stopping them: the
	// tap that closes it also reached the Compose view under it (a link of About opened in Safari). While the sheet is
	// up the view that holds the app does not take touches; every way the sheet ends reaches this handler.
	val presentingView = topController.view
	val wasInteractive = presentingView.userInteractionEnabled

	presentingView.userInteractionEnabled = false

	activityController.completionWithItemsHandler = { _, _, _, _ ->
		presentingView.userInteractionEnabled = wasInteractive
	}

	// On an iPad the sheet is a popover that must point somewhere, or presenting it raises; it has no control of its
	// own to anchor to, so it points at the centre of the presenting view.
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

	return true
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
