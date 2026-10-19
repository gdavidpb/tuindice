package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.mapper.DateTextStyle
import com.gdavidpb.tuindice.base.presentation.mapper.formatDate
import com.gdavidpb.tuindice.base.presentation.model.UiText

// A moment the chat tells the student about, read in their time zone: "lunes, 03:05 p. m.".
internal fun Long.toChatDateText(): UiText = formatDate(DateTextStyle.WEEKDAY_TIME)
