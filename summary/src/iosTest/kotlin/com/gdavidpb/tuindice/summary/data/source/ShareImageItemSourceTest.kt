package com.gdavidpb.tuindice.summary.data.source

import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSURL
import platform.LinkPresentation.LPLinkMetadata
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIImage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame

@OptIn(ExperimentalForeignApi::class)
class ShareImageItemSourceTest {
	@Test
	fun theSheetIsHandedTheImage_whateverTheActivity() {
		val image = image()
		val source = ShareImageItemSource(image = image, file = file())

		assertSame(image, source.activityViewControllerPlaceholderItem(controller()))
		assertSame(image, source.activityViewController(controller(), itemForActivityType = null))
	}

	@Suppress("CAST_NEVER_SUCCEEDS")
	@Test
	fun theHeaderPreviewsTheSharedPng_underItsFileName() {
		val source = ShareImageItemSource(image = image(), file = file())

		val metadata = assertNotNull(source.activityViewControllerLinkMetadata(controller())) as LPLinkMetadata

		assertEquals("mi-trimestre.png", metadata.title)
		assertNotNull(metadata.imageProvider)
		assertNotNull(metadata.iconProvider)
	}

	private fun image(): UIImage = UIGraphicsImageRenderer(size = CGSizeMake(2.0, 3.0)).imageWithActions { }

	private fun file(): NSURL = NSURL.fileURLWithPath("/tmp/statistics_share/mi-trimestre.png")

	private fun controller() = UIActivityViewController(activityItems = emptyList<Any>(), applicationActivities = null)
}
