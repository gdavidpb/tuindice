package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.ClassPlaceItem
import com.gdavidpb.tuindice.base.presentation.model.UiText

/**
 * The first row of the week: [leadText] is "En curso" ([isInProgress]) or the day of the next class
 * ("Hoy", "Lun 19"); [place] carries the blocks, the room and, after them, the hour.
 */
@Immutable
data class SummaryClassRowItem(
	val leadText: UiText,
	val isInProgress: Boolean,
	val subjectCode: String,
	val place: ClassPlaceItem
)
