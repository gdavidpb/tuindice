package com.gdavidpb.tuindice.base.presentation.model

sealed class BrowserDialog : Dialog {
	class ExternalResourceDialog(val url: String) : BrowserDialog()
}