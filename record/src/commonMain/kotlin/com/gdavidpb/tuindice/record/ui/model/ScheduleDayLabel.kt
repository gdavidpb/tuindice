package com.gdavidpb.tuindice.record.ui.model

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.record.presentation.mapper.toShortNameText
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay

/** The column header of a day ("Lun"), read where the grid and the table draw it. */
@Composable
fun ScheduleDay.label(): String = toShortNameText().asString()
