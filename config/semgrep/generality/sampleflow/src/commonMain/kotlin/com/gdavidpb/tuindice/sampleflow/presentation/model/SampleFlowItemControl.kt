package com.gdavidpb.tuindice.sampleflow.presentation.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.sampleflow.ui.model.SampleFlowLabel

// Control negativo: los tipos de valor que los mappers precalculan, los modelos de ui.model y los tokens de
// base.ui.style no disparan ninguna regla de presentación.
class SampleFlowItemControl(
	val tint: Color,
	val icon: ImageVector,
	val text: AnnotatedString,
	val weight: FontWeight,
	val label: SampleFlowLabel,
	val alpha: Float = TuIndiceAlpha
) {
	val rich: AnnotatedString = buildAnnotatedString {
		withStyle(SpanStyle(fontWeight = weight)) { append(text) }
	}
}
