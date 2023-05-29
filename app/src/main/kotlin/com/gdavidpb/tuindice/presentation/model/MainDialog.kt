package com.gdavidpb.tuindice.presentation.model

sealed class MainDialog {
	object SignOutConfirmation : MainDialog()
	object GooglePlayServicesUnavailable : MainDialog()
}