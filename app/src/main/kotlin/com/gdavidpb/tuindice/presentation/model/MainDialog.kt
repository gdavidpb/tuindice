package com.gdavidpb.tuindice.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.Dialog

sealed class MainDialog : Dialog {
	object SignOutConfirmation : MainDialog()
	object GooglePlayServicesUnavailable : MainDialog()
}