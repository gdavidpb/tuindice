package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.mapper.DateTextStyle
import com.gdavidpb.tuindice.base.presentation.mapper.formatDate
import com.gdavidpb.tuindice.base.presentation.model.UiText
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_memory_since

// When the assistant saved a memory, as the list says it: "Desde el 12 de octubre", with the year after the day on
// another year. The day is read in the student's time zone, against the moment the list is read.
internal fun Long.toMemorySinceText(nowMillis: Long): UiText {
	val style = if (toLocalDate().year == nowMillis.toLocalDate().year) {
		DateTextStyle.DAY_MONTH
	} else {
		DateTextStyle.DAY_MONTH_YEAR_UNPADDED
	}

	return UiText.Resource(Res.string.assistant_memory_since, listOf(formatDate(style)))
}
