package com.gdavidpb.tuindice.summary.data.source

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSItemProvider
import platform.Foundation.NSURL
import platform.LinkPresentation.LPLinkMetadata
import platform.UIKit.UIActivityItemSourceProtocol
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIImage
import platform.darwin.NSObject

/**
 * The image the share sheet hands over, with what the sheet shows of it in its header: a bare image leaves the header
 * with a generic icon, so the sheet is also given the shared PNG as the preview, and the file's name.
 */
@OptIn(ExperimentalForeignApi::class)
internal class ShareImageItemSource(
	private val image: UIImage,
	private val file: NSURL
) : NSObject(), UIActivityItemSourceProtocol {
	override fun activityViewControllerPlaceholderItem(activityViewController: UIActivityViewController): Any = image

	override fun activityViewController(
		activityViewController: UIActivityViewController,
		itemForActivityType: String?
	): Any = image

	// UIKit declares the metadata ahead of LinkPresentation, so it names the forward declaration: the object is the same.
	@Suppress("CAST_NEVER_SUCCEEDS")
	override fun activityViewControllerLinkMetadata(
		activityViewController: UIActivityViewController
	): objcnames.classes.LPLinkMetadata? {
		val metadata = LPLinkMetadata().apply {
			// The file's name alone: with its URL as well, the sheet would repeat the name as a subtitle.
			title = file.lastPathComponent
			// The PNG on disk is the picture the sheet shares, so it is the one it previews.
			imageProvider = NSItemProvider(contentsOfURL = file)
			iconProvider = NSItemProvider(contentsOfURL = file)
		}

		return metadata as objcnames.classes.LPLinkMetadata
	}
}
