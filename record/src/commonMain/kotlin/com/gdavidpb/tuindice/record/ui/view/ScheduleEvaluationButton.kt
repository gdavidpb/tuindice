package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.schedule_evaluation_click_label

// A cell or meeting of the schedule as a button for the evaluation of its mark, or as it is when it has none.
// Clipped to the shape so the ripple does not leave it; the view says what it is.
@Composable
internal fun Modifier.scheduleEvaluationButton(
	shape: Shape,
	evaluationId: String?,
	onClick: (evaluationId: String) -> Unit
): Modifier {
	val label = stringResource(Res.string.schedule_evaluation_click_label)

	if (evaluationId == null) return this

	return clip(shape).clickable(onClickLabel = label, role = Role.Button) { onClick(evaluationId) }
}
