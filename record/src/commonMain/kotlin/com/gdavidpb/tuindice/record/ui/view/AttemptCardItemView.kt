package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.base.presentation.navigation.LocalAssistantLauncher
import com.gdavidpb.tuindice.base.ui.view.AskAssistantMenuItemView
import com.gdavidpb.tuindice.base.ui.view.arrivalHighlight
import com.gdavidpb.tuindice.record.presentation.model.AttemptItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.model.AttemptArrivalHighlight
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.a11y_attempt_show_actions

@Composable
fun AttemptCardItemView(
	modifier: Modifier = Modifier,
	item: AttemptItem,
	gradeState: MutableIntState? = null,
	onGradeChange: (newGrade: Int?, newOutcome: AttemptOutcome?, isSelected: Boolean) -> Unit,
	// The arrival highlight a jump plays on this card, while it plays.
	arrivalHighlight: AttemptArrivalHighlight? = null
) {
	// A tap on the card opens its actions, as an evaluation's card does: «Preguntar sobre esto» asks the assistant about
	// this subject. Without a way to reach the assistant the card has no actions and does not take the tap.
	val assistantLauncher = LocalAssistantLauncher.current
	val isMenuExpanded = remember { mutableStateOf(false) }
	val showActionsLabel = stringResource(Res.string.a11y_attempt_show_actions)
	val actionsModifier = if (assistantLauncher != null) {
		Modifier
			.clip(CardDefaults.shape)
			.clickable(
				onClickLabel = showActionsLabel,
				onClick = { isMenuExpanded.value = true }
			)
	} else {
		Modifier
	}

	// The margin is outside the card, so the highlight frames the card's own shape and not the space around it.
	Box(
		modifier = modifier
			.fillMaxWidth()
			.padding(
				horizontal = 16.dp,
				vertical = 6.dp
			)
	) {
		Card(
			colors = CardDefaults.cardColors(
				containerColor = MaterialTheme.colorScheme.surfaceVariant,
				contentColor = MaterialTheme.colorScheme.onSurface
			),
			modifier = Modifier
				.fillMaxWidth()
				.testTag(RecordUiTags.attemptCard(item.attemptId))
				.arrivalHighlight(
					trigger = arrivalHighlight?.trigger,
					shape = CardDefaults.shape,
					onFinished = { arrivalHighlight?.onFinished?.invoke() }
				)
				.then(actionsModifier)
		) {
			AttemptItemView(
				item = item,
				gradeState = gradeState,
				onGradeChange = onGradeChange
			)
		}

		if (assistantLauncher != null) {
			DropdownMenu(
				expanded = isMenuExpanded.value,
				onDismissRequest = { isMenuExpanded.value = false }
			) {
				AskAssistantMenuItemView(
					onAsk = {
						isMenuExpanded.value = false
						assistantLauncher.launch(item.assistantContext)
					}
				)
			}
		}
	}
}
