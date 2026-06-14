package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_subject_detail_focus_subject

@Composable
fun PensumOpenRelatedSubjectIcon(
	code: String,
	modifier: Modifier = Modifier
) {
	Icon(
		imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
		contentDescription = stringResource(
			Res.string.pensum_subject_detail_focus_subject,
			code
		),
		tint = MaterialTheme.colorScheme.onSurfaceVariant,
		modifier = modifier.size(SubjectDetailOpenIconSize)
	)
}
