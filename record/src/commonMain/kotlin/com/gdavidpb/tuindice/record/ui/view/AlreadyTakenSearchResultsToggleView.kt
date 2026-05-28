package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.create_term_hide_taken_subjects
import tuindice.record.generated.resources.create_term_show_taken_subjects

@Composable
fun AlreadyTakenSearchResultsToggle(
	count: Int,
	isExpanded: Boolean,
	onClick: () -> Unit
) {
	TextButton(
		modifier = Modifier
			.fillMaxWidth()
			.testTag(RecordUiTags.CreateSyntheticTermTakenSubjectsToggle),
		onClick = onClick,
		contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.Start,
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				text = stringResource(
					if (isExpanded)
						Res.string.create_term_hide_taken_subjects
					else
						Res.string.create_term_show_taken_subjects,
					count
				),
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.SemiBold,
				color = MaterialTheme.colorScheme.primary,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
		}
	}
}
