package com.gdavidpb.tuindice.record.presentation.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.gdavidpb.tuindice.base.presentation.model.UiText

// A calm explanation shown with the record, already resolved to text, icon and place. Only one at
// a time.
data class RecordNotice(
	val title: UiText?,
	val message: UiText,
	val kind: RecordNoticeKind,
	val placement: RecordNoticePlacement,
	val icon: ImageVector
)
