package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeVisualStyle
import com.gdavidpb.tuindice.pensum.ui.view.PensumSubjectCodeChip

@Composable
fun PensumStyledSubjectCodeChip(
	code: String,
	visualStyle: PensumNodeVisualStyle,
	modifier: Modifier = Modifier
) {
	PensumSubjectCodeChip(
		modifier = modifier,
		code = code,
		fallbackContainer = Color(visualStyle.chipArgb),
		fallbackContent = Color(visualStyle.chipTextArgb)
	)
}
