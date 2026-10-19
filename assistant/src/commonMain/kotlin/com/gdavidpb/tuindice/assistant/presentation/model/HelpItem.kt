package com.gdavidpb.tuindice.assistant.presentation.model

// conocimiento.ayuda/1: the title of the help, its steps numbered in order, and the button to the screen it names.
data class HelpItem(
	override val id: String,
	override val opensSheet: Boolean,
	val title: String,
	val steps: List<HelpStepItem>,
	// Null when the help names no screen.
	val openLink: CardLinkItem?
) : ChatComponentItem
