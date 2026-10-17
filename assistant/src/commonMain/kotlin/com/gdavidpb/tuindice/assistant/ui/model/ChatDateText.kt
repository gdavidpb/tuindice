package com.gdavidpb.tuindice.assistant.ui.model

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.presentation.mapper.DateTextStyle
import com.gdavidpb.tuindice.base.presentation.mapper.formatDate
import com.gdavidpb.tuindice.base.presentation.model.asString

// A moment the chat tells the student about, read in their time zone: "lunes, 03:05 p. m.".
@Composable
fun Long.chatDateText(): String = formatDate(DateTextStyle.WEEKDAY_TIME).asString()
