package com.gdavidpb.tuindice.wizard.ui.anchor

import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.toSize
import com.gdavidpb.tuindice.wizard.presentation.model.CoachmarkId
import com.gdavidpb.tuindice.wizard.ui.CoachmarkUiTags

fun Modifier.coachmarkAnchor(
	id: CoachmarkId?,
	registry: CoachmarkAnchorRegistry
): Modifier = composed {
	if (id == null) return@composed this

	DisposableEffect(id, registry) {
		onDispose {
			registry.clear(id)
		}
	}

	this
		.testTag(CoachmarkUiTags.anchor(id))
		.onGloballyPositioned { coordinates ->
			registry.update(
				id = id,
				bounds = Rect(
					offset = coordinates.positionInRoot(),
					size = coordinates.size.toSize()
				)
			)
		}
}
