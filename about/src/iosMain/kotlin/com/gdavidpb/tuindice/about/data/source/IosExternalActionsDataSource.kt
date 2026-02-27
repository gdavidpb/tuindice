package com.gdavidpb.tuindice.about.data.source

import com.gdavidpb.tuindice.about.domain.repository.ExternalActionsRepository
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow

class IosExternalActionsDataSource : ExternalActionsRepository {
	override fun sendEmail(email: String, subject: String, text: String) {
		val encodedSubject = subject.toMailQueryValue()
		val encodedText = text.toMailQueryValue()

		openUrl("mailto:$email?subject=$encodedSubject&body=$encodedText")
	}

	override fun shareText(subject: String, text: String) {
		val topController = topViewController()
			?: run {
				sendEmail(email = "", subject = subject, text = text)
				return
			}

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

	override fun openStore() {
		openUrl("itms-apps://apps.apple.com")
	}

	private fun openUrl(url: String) {
		val platformUrl = NSURL.URLWithString(url) ?: return
		UIApplication.sharedApplication.openURL(platformUrl)
	}

	private fun String.toMailQueryValue(): String {
		return replace(" ", "%20")
			.replace("\n", "%0A")
			.replace("&", "%26")
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
