package com.gdavidpb.tuindice.about.data.source


import com.gdavidpb.tuindice.about.presentation.utils.ShareTextHandler
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow

class IosShareTextHandler : ShareTextHandler {
	override fun invoke(subject: String, text: String) {
		val topController = topViewController()
			?: return

		val activityController = UIActivityViewController(
			activityItems = listOf(text),
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
