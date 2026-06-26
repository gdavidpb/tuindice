package com.gdavidpb.tuindice.wizard.presentation.machine

import com.gdavidpb.tuindice.wizard.domain.model.CoachmarkResolution

internal sealed class CoachmarkOverlayInternalEvent {
	class CoachmarkResolved(
		val visitKey: String,
		val resolution: CoachmarkResolution?
	) : CoachmarkOverlayInternalEvent()
}
