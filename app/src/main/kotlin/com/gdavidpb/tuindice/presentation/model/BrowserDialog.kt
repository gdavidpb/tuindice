package com.gdavidpb.tuindice.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.Dialog

sealed class BrowserDialog : Dialog {
	class ExternalResourceDialog(val url: String) : BrowserDialog()
}