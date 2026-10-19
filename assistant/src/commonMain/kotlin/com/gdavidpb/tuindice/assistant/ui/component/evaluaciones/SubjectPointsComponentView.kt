package com.gdavidpb.tuindice.assistant.ui.component.evaluaciones

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.SubjectPointsItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLinkItemView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardProgressView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardValueView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_points_label

// evaluaciones.puntos/1: no figure, since the subjects have no single count. A row per subject: its code, its points
// earned over the points evaluated so far, and a thin bar of what is graded over the total of the subject.
@Composable
fun SubjectPointsComponentView(
	item: SubjectPointsItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Evaluations.points(item.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_b_points_label))
		item.rows.forEachIndexed { index, row ->
			Column(verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Small)) {
				CardRowView(
					code = row.code,
					name = row.name,
					showsDivider = index > 0,
					trailing = { CardValueView(text = row.pointsText.asString()) }
				)
				CardProgressView(fraction = row.progress)
			}
		}
		item.recordLink?.let { link -> CardLinkItemView(link = link, onAction = onAction) }
	}
}
