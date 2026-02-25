package com.gdavidpb.tuindice.summary.presentation.route

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef
import com.gdavidpb.tuindice.base.domain.model.PlatformUri

interface ProfilePictureActions {
	fun openCamera(output: PlatformFileRef)
	fun openPicker()
}

interface ProfilePictureActionsFactory {
	@Composable
	fun remember(
		onPicturePicked: (PlatformUri) -> Unit,
		onPictureTaken: () -> Unit
	): ProfilePictureActions
}
