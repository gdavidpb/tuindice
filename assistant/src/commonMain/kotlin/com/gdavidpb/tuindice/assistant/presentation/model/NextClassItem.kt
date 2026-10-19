package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// horario.proxima/1: the hours of the next class, or of the one in progress, with its subject, its room, its day and how
// long is left until it starts.
data class NextClassItem(
	override val id: String,
	override val opensSheet: Boolean,
	val labelText: UiText,
	val classFigure: CardFigureItem,
	val subjectCode: String,
	val subjectName: String,
	val inProgress: Boolean,
	val dayText: UiText,
	// Null when the class has no room.
	val roomText: String?,
	// Null for a class in progress, or one that has already started, which leaves nothing to count down.
	val remainingText: UiText?
) : ChatComponentItem
