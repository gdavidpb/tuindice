package com.gdavidpb.tuindice.wizard.ui

import com.gdavidpb.tuindice.wizard.presentation.model.CoachmarkId
import com.gdavidpb.tuindice.wizard.presentation.model.tagValue

object CoachmarkUiTags {
	const val Host = "coachmark_host"
	const val Bubble = "coachmark_bubble"
	const val BackButton = "coachmark_back"
	const val ConfirmButton = "coachmark_confirm"

	fun currentCoachmark(coachmarkId: CoachmarkId): String =
		"coachmark_current_${coachmarkId.tagValue}"

	fun anchor(coachmarkId: CoachmarkId): String =
		"coachmark_anchor_${coachmarkId.tagValue}"
}
