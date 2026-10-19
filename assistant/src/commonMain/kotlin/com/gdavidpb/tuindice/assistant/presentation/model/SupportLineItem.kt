package com.gdavidpb.tuindice.assistant.presentation.model

// One line of the support: its label, the phone to call, when it has one, and the page to open, when it has one.
data class SupportLineItem(
	val label: String,
	val call: SupportCallItem?,
	val pageUri: String?
)
