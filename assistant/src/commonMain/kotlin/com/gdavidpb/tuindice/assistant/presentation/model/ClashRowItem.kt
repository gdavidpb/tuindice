package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// One clash: the subjects that meet at the same time, and the day and the hours of the clash.
data class ClashRowItem(
	val codes: UiText,
	val dayAndHoursText: UiText
)
