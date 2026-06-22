package com.gdavidpb.tuindice.wizard.ui.anchor

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.geometry.Rect
import com.gdavidpb.tuindice.wizard.presentation.model.CoachmarkId

class CoachmarkAnchorRegistry {
	private val boundsById = mutableStateMapOf<CoachmarkId, Rect>()

	fun boundsFor(id: CoachmarkId): Rect? = boundsById[id]

	fun update(id: CoachmarkId, bounds: Rect) {
		boundsById[id] = bounds
	}

	fun clear(id: CoachmarkId) {
		boundsById.remove(id)
	}
}
