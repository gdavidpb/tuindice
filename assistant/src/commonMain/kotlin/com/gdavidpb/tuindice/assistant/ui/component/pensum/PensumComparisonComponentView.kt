package com.gdavidpb.tuindice.assistant.ui.component.pensum

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFigure
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRecordLinkView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardValueView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.chatcontract.component.PensumComparisonComponent
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_comparison_career
import tuindice.assistant.generated.resources.assistant_card_b_comparison_label
import tuindice.assistant.generated.resources.assistant_card_b_comparison_missing
import tuindice.assistant.generated.resources.assistant_card_b_comparison_pensum
import tuindice.assistant.generated.resources.assistant_card_b_comparison_shared_caption
import tuindice.assistant.generated.resources.assistant_card_b_comparison_shared_description

// pensum.comparacion/1: the credits the two pensums share are the figure. Then the career and the pensum compared, the
// subjects still missing, and the notes the service gives, in rows and nothing more than the service brings.
@Composable
fun PensumComparisonComponentView(
	component: PensumComparisonComponent,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	val shared = component.sharedCredits.toString()

	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Pensum.comparison(component.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_b_comparison_label))
		CardFiguresView(
			primary = CardFigure(
				value = shared,
				caption = stringResource(Res.string.assistant_card_b_comparison_shared_caption),
				contentDescription = stringResource(Res.string.assistant_card_b_comparison_shared_description, shared)
			)
		)
		CardRowView(
			name = stringResource(Res.string.assistant_card_b_comparison_career),
			showsDivider = false,
			trailing = { CardValueView(text = component.otherCareerName) }
		)
		CardRowView(
			name = stringResource(Res.string.assistant_card_b_comparison_pensum),
			showsDivider = true,
			trailing = { CardValueView(text = component.otherPensumLabel) }
		)
		if (component.missingSubjects.isNotEmpty()) {
			CardLabelView(text = stringResource(Res.string.assistant_card_b_comparison_missing))
			component.missingSubjects.forEachIndexed { index, subject ->
				CardRowView(code = subject.code, name = subject.name, showsDivider = index > 0)
			}
		}
		component.notes.forEach { note ->
			Text(
				text = note,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface
			)
		}
		CardRecordLinkView(entityRefs = component.entityRefs, onAction = onAction)
	}
}
