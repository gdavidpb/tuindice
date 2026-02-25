package com.gdavidpb.tuindice.base.presentation.model

data class SnackBarMessage(
	val message: String,
	val actionLabel: String? = null,
	val onAction: (() -> Unit)? = null,
	val onDismissed: (() -> Unit)? = null
)