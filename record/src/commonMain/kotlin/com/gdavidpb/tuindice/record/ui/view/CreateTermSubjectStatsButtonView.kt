package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.create_term_subject_stats_content_description

@Composable
fun CreateTermSubjectStatsButton(
	subjectCode: String,
	onClick: () -> Unit
) {
	Surface(
		shape = RoundedCornerShape(10.dp),
		color = MaterialTheme.colorScheme.surface.copy(alpha = 0.48f),
		border = BorderStroke(
			width = 1.dp,
			color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
		)
	) {
		IconButton(
			modifier = Modifier
				.size(38.dp)
				.testTag(RecordUiTags.createSyntheticTermSubjectStatsButton(subjectCode)),
			onClick = onClick
		) {
			Icon(
				imageVector = Icons.Outlined.BarChart,
				contentDescription = stringResource(
					Res.string.create_term_subject_stats_content_description,
					subjectCode
				),
				tint = MaterialTheme.colorScheme.onSurface
			)
		}
	}
}
