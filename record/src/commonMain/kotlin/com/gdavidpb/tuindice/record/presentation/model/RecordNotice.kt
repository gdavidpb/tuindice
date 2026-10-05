package com.gdavidpb.tuindice.record.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// A calm explanation shown with the record, already resolved to text. Only one at a time.
data class RecordNotice(
	val title: UiText?,
	val message: UiText,
	val kind: RecordNoticeKind
)
