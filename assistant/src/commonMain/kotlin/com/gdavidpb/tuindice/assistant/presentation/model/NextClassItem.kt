package com.gdavidpb.tuindice.assistant.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

// horario.proxima/1: the blocks of the next class, or of the one in progress, and their hours, with its subject, its
// room, its day and how long is left until it starts.
@Immutable
data class NextClassItem(
	override val id: String,
	override val opensSheet: Boolean,
	val labelText: UiText,
	val classFigure: CardFigureItem,
	val subjectCode: String,
	val subjectName: String,
	val inProgress: Boolean,
	// The chip that says the class is in progress; null for a class not started yet.
	val statusChip: CardChipItem?,
	val dayText: UiText,
	// Null when the class has no room.
	val roomText: String?,
	// Null for a class in progress, or one that has already started, which leaves nothing to count down.
	val remainingText: UiText?
) : RecordComponentItem
