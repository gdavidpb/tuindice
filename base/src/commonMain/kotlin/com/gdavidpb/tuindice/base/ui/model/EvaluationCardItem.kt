package com.gdavidpb.tuindice.base.ui.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.gdavidpb.tuindice.base.presentation.model.ClassPlaceItem
import com.gdavidpb.tuindice.base.presentation.model.EvaluationTone

// The plain values one evaluation card shows. A line or a chip that is null is not drawn: an evaluation with no date
// has no date line, and one the chat does not give a state has no state box.
data class EvaluationCardItem(
	val name: String,
	val subjectCode: String,
	val dateText: String?,
	val highlightTone: EvaluationTone,
	val typeIcon: ImageVector? = null,
	val place: ClassPlaceItem? = null,
	val gradeText: String? = null,
	val statusText: String? = null,
	val statusTone: EvaluationTone = EvaluationTone.Neutral
)
