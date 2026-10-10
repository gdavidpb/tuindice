package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.record.presentation.model.AttemptItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags

/**
 * Second row of a subject card: the code chip, the section and classroom in the free space (they
 * give way with an ellipsis) and the credits.
 */
@Composable
fun AttemptSubjectRowView(
	modifier: Modifier = Modifier,
	item: AttemptItem
) {
	Row(
		modifier = modifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			modifier = Modifier
				.testTag(RecordUiTags.attemptSubjectChip(item.attemptId))
				.background(
					color = item.codeContainerColor,
					shape = RoundedCornerShape(TuIndiceRadius.Small)
				)
				.padding(vertical = 5.dp, horizontal = 10.dp),
			text = item.codeText,
			color = item.codeColor,
			style = MaterialTheme.typography.labelLarge
		)

		if (item.detailText != null) {
			Text(
				modifier = Modifier
					.weight(1f)
					.padding(horizontal = TuIndiceSpacing.XLarge)
					.testTag(RecordUiTags.attemptDetail(item.attemptId)),
				text = item.detailText,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				style = MaterialTheme.typography.labelLarge,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
		} else {
			Spacer(modifier = Modifier.weight(1f))
		}

		Text(
			text = item.creditsText,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			style = MaterialTheme.typography.labelLarge,
			maxLines = 1
		)
	}
}
