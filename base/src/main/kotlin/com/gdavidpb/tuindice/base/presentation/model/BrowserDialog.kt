package com.gdavidpb.tuindice.base.presentation.model

sealed class BrowserDialog {
	class ExternalResourceDialog(val url: String) : BrowserDialog()
}