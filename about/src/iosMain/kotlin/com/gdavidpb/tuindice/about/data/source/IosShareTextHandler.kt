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
