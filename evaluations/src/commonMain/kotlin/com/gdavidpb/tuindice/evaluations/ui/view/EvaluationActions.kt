package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.presentation.navigation.AssistantLauncher
import com.gdavidpb.tuindice.base.ui.icon.rememberAssistantSparkleIcon
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.label_evaluation_swipe_ask
import tuindice.evaluations.generated.resources.label_evaluation_swipe_delete
import tuindice.evaluations.generated.resources.label_evaluation_swipe_edit

// The start side of the leading action keeps the rounded corners.
private val LeadingActionShape = RoundedCornerShape(
	topStart = TuIndiceRadius.Small,
	bottomStart = TuIndiceRadius.Small,
	topEnd = 0.dp,
	bottomEnd = 0.dp
)

// A row asks the assistant only where the assistant is provided, and then it asks with the row's own context.
internal fun assistantAskAction(
	launcher: AssistantLauncher?,
	context: AssistantScreenContext
): (() -> Unit)? = launcher?.let { assistant ->
	{
		assistant.launch(context)
	}
}

@Composable
fun EvaluationActions(
	modifier: Modifier = Modifier,
	onAsk: (() -> Unit)? = null,
	onEdit: () -> Unit,
	onDelete: () -> Unit
) {
	Row(
		modifier = modifier,
		horizontalArrangement = Arrangement.End,
		verticalAlignment = Alignment.CenterVertically
	) {
		// The assistant's action leads when it is available; Edit then takes the middle, square on both sides.
		if (onAsk != null) {
			val sparkleIcon = rememberAssistantSparkleIcon()

			EvaluationActionButton(
				modifier = Modifier.weight(1f).testTag(EvaluationsUiTags.EvaluationSwipeAskAction),
				text = stringResource(Res.string.label_evaluation_swipe_ask),
				containerColor = MaterialTheme.colorScheme.surfaceVariant,
				contentColor = MaterialTheme.colorScheme.onSurface,
				shape = LeadingActionShape,
				onClick = onAsk
			) {
				Icon(imageVector = sparkleIcon, contentDescription = null, tint = Color.Unspecified)
			}
		}

		EvaluationActionButton(
			modifier = Modifier
				.weight(1f)
				.testTag(EvaluationsUiTags.EvaluationSwipeEditAction),
			text = stringResource(Res.string.label_evaluation_swipe_edit),
			containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
			contentColor = MaterialTheme.colorScheme.onSurface,
			shape = if (onAsk != null) RectangleShape else LeadingActionShape,
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
				topEnd = TuIndiceRadius.Small,
				bottomEnd = TuIndiceRadius.Small
			),
			onClick = onDelete
		) {
			Icon(
				imageVector = Icons.Outlined.DeleteOutline,
				contentDescription = null
			)
		}
	}
}
