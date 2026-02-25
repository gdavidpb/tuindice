package com.gdavidpb.tuindice.summary.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef
import com.gdavidpb.tuindice.base.domain.model.PlatformUri
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.random.Random
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerImageURL
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.posix.memcpy

class IosProfilePictureActionsFactory : ProfilePictureActionsFactory {
	@Composable
	override fun remember(
		onPicturePicked: (PlatformUri) -> Unit,
		onPictureTaken: () -> Unit
	): ProfilePictureActions {
		val launcher = remember(onPicturePicked, onPictureTaken) {
			IosProfilePictureLauncher(
				onPicturePicked = onPicturePicked,
				onPictureTaken = onPictureTaken
			)
		}

		return remember(launcher) {
			object : ProfilePictureActions {
				override fun openCamera(output: PlatformFileRef) {
					launcher.openCamera(output)
				}

				override fun openPicker() {
					launcher.openPicker()
				}
			}
		}
	}
}

private class IosProfilePictureLauncher(
	private val onPicturePicked: (PlatformUri) -> Unit,
	private val onPictureTaken: () -> Unit
) {
	private var delegateHolder: PickerDelegate? = null

	fun openPicker() {
		presentPicker(
			sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary,
			outputPath = null
		) { imagePath ->
			onPicturePicked(PlatformUri(imagePath))
		}
	}

	fun openCamera(output: PlatformFileRef) {
		presentPicker(
			sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera,
			outputPath = output.value
		) {
			onPictureTaken()
		}
	}

	private fun presentPicker(
		sourceType: UIImagePickerControllerSourceType,
		outputPath: String?,
		onSuccess: (imagePath: String) -> Unit
	) {
		if (!UIImagePickerController.isSourceTypeAvailable(sourceType)) return

		val hostController = topViewController() ?: return
		val picker = UIImagePickerController().apply {
			this.sourceType = sourceType
		}

		val delegate = PickerDelegate(
			outputPath = outputPath,
			onSuccess = onSuccess,
			onDispose = { delegateHolder = null }
		)

		delegateHolder = delegate
		picker.delegate = delegate

		hostController.presentViewController(
			viewControllerToPresent = picker,
			animated = true,
			completion = null
		)
	}
}

private class PickerDelegate(
	private val outputPath: String?,
	private val onSuccess: (imagePath: String) -> Unit,
	private val onDispose: () -> Unit
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
	override fun imagePickerControllerDidCancel(
		picker: UIImagePickerController
	) {
		picker.dismissViewControllerAnimated(
			flag = true,
			completion = { onDispose() }
		)
	}

	override fun imagePickerController(
		picker: UIImagePickerController,
		didFinishPickingMediaWithInfo: Map<Any?, *>
	) {
		val imagePath = resolveImagePath(didFinishPickingMediaWithInfo)

		picker.dismissViewControllerAnimated(
			flag = true,
			completion = {
				if (!imagePath.isNullOrBlank()) {
					onSuccess(imagePath)
				}

				onDispose()
			}
		)
	}

	private fun resolveImagePath(
		info: Map<Any?, *>
	): String? {
		val selectedImageUrl = info[UIImagePickerControllerImageURL] as? NSURL
		val selectedImagePath = selectedImageUrl?.path

		if (!selectedImagePath.isNullOrBlank()) {
			return selectedImagePath
		}

		val image = info[UIImagePickerControllerOriginalImage] as? UIImage ?: return null
		val destinationPath = outputPath ?: temporaryImagePath()
		val data = UIImageJPEGRepresentation(image, 0.95)
		val content = data?.toByteArray() ?: return null
		val destination = destinationPath.toPath()
		val didWrite = runCatching {
			FileSystem.SYSTEM.write(destination) {
				write(content)
			}
		}.isSuccess

		return destinationPath.takeIf { didWrite }
	}

	private fun temporaryImagePath(): String {
		return "${NSTemporaryDirectory().trimEnd('/')}/profile-picture-${Random.nextInt()}.jpg"
	}
}

@OptIn(ExperimentalForeignApi::class)
private fun platform.Foundation.NSData.toByteArray(): ByteArray {
	val size = length.toInt()
	if (size <= 0) return ByteArray(0)

	val output = ByteArray(size)
	output.usePinned { pinned ->
		memcpy(
			pinned.addressOf(0),
			bytes,
			length
		)
	}
	return output
}

private fun topViewController(): UIViewController? {
	var controller = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return null

	while (controller.presentedViewController != null) {
		controller = controller.presentedViewController ?: break
	}

	return controller
}
