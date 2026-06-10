package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.label_evaluation_swipe_delete
import tuindice.evaluations.generated.resources.label_evaluation_swipe_edit

@Composable
fun EvaluationActions(
	modifier: Modifier = Modifier,
	onEdit: () -> Unit,
	onDelete: () -> Unit
) {
	Row(
		modifier = modifier,
		horizontalArrangement = Arrangement.End,
		verticalAlignment = Alignment.CenterVertically
	) {
		EvaluationActionButton(
			modifier = Modifier
				.weight(1f)
				.testTag(EvaluationsUiTags.EvaluationSwipeEditAction),
			text = stringResource(Res.string.label_evaluation_swipe_edit),
			containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
			contentColor = MaterialTheme.colorScheme.onSurface,
			shape = RoundedCornerShape(
				topStart = 8.dp,
				bottomStart = 8.dp,
				topEnd = 0.dp,
				bottomEnd = 0.dp
			),
			onClick = onEdit
		) {
			Icon(
				imageVector = Icons.Outlined.Edit,
				contentDescription = null
			)
		}

		EvaluationActionButton(
			modifier = Modifier
				.weight(1f)
				.testTag(EvaluationsUiTags.EvaluationSwipeDeleteAction),
			text = stringResource(Res.string.label_evaluation_swipe_delete),
			containerColor = MaterialTheme.colorScheme.errorContainer,
			contentColor = MaterialTheme.colorScheme.onErrorContainer,
			shape = RoundedCornerShape(
				topStart = 0.dp,
				bottomStart = 0.dp,
				topEnd = 8.dp,
				bottomEnd = 8.dp
			),
			onClick = onDelete
		) {
			Icon(
				imageVector = Icons.Default.Delete,
				contentDescription = null
			)
		}
	}
}
