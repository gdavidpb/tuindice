package com.gdavidpb.tuindice.summary.presentation.route

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.net.toUri
import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef
import com.gdavidpb.tuindice.base.domain.model.PlatformUri

class AndroidProfilePictureActionsFactory : ProfilePictureActionsFactory {
	@Composable
	override fun remember(
		onPicturePicked: (PlatformUri) -> Unit,
		onPictureTaken: () -> Unit
	): ProfilePictureActions {
		val pickVisualMediaLauncher = rememberLauncherForActivityResult(
			contract = ActivityResultContracts.PickVisualMedia()
		) { uri ->
			if (uri != null) {
				onPicturePicked(PlatformUri(uri.toString()))
			}
		}

		val takePictureLauncher = rememberLauncherForActivityResult(
			contract = ActivityResultContracts.TakePicture()
		) { success ->
			if (success) {
				onPictureTaken()
			}
		}

		return remember(
			pickVisualMediaLauncher,
			takePictureLauncher
		) {
			object : ProfilePictureActions {
				override fun openCamera(output: PlatformFileRef) {
					takePictureLauncher.launch(output.value.toUri())
				}

				override fun openPicker() {
					pickVisualMediaLauncher.launch(
						PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
					)
				}
			}
		}
	}
}
