package com.gdavidpb.tuindice.assistant.ui.component.evaluaciones

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.mapper.chatCardFraction
import com.gdavidpb.tuindice.assistant.presentation.mapper.toCardCompactNumberText
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardProgressView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRecordLinkView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardValueView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.chatcontract.component.SubjectPointsComponent
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_fraction
import tuindice.assistant.generated.resources.assistant_card_b_points_label

// evaluaciones.puntos/1: no figure, since the subjects have no single count. A row per subject: its code, its points
// earned over the points evaluated so far, and a thin bar of what is graded over the total of the subject.
@Composable
fun SubjectPointsComponentView(
	component: SubjectPointsComponent,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Evaluations.points(component.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_b_points_label))
		component.rows.forEachIndexed { index, row ->
			val points = stringResource(
				Res.string.assistant_card_b_fraction,
				row.earned.toCardCompactNumberText(),
				row.evaluated.toCardCompactNumberText()
			)

			Column(verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Small)) {
				CardRowView(
					code = row.subjectCode,
					name = row.subjectName,
					showsDivider = index > 0,
					trailing = { CardValueView(text = points) }
				)
				CardProgressView(fraction = chatCardFraction(row.evaluated, row.total))
			}
		}
		CardRecordLinkView(entityRefs = component.entityRefs, onAction = onAction)
	}
}
