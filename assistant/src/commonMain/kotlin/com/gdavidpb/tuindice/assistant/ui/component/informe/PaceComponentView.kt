package com.gdavidpb.tuindice.assistant.ui.component.informe

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.mapper.toCardDecimalText
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFigure
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardValueView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.chatcontract.component.PaceComponent
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_pace_caption
import tuindice.assistant.generated.resources.assistant_card_a_pace_description
import tuindice.assistant.generated.resources.assistant_card_a_pace_label
import tuindice.assistant.generated.resources.assistant_card_a_pace_remaining

private const val AVERAGE_DECIMALS = 1

// informe.ritmo/1: the average of credits per term as the figure, then the credits of each term and, when the service
// estimates them, the terms left.
@Composable
fun PaceComponentView(
	component: PaceComponent,
	modifier: Modifier = Modifier
) {
	val average = component.averageCredits.toCardDecimalText(AVERAGE_DECIMALS)

	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Report.pace(component.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_a_pace_label))
		CardFiguresView(
			primary = CardFigure(
				value = average,
				caption = stringResource(Res.string.assistant_card_a_pace_caption),
				contentDescription = stringResource(Res.string.assistant_card_a_pace_description, average)
			)
		)
		component.creditsPerTerm.forEachIndexed { index, term ->
			CardRowView(
				name = term.termLabel,
				showsDivider = index > 0,
				trailing = { CardValueView(text = term.credits.toString()) }
			)
		}
		component.remainingTermsEstimate?.let { terms ->
			CardRowView(
				name = stringResource(Res.string.assistant_card_a_pace_remaining),
				showsDivider = component.creditsPerTerm.isNotEmpty(),
				trailing = { CardValueView(text = terms.toString()) }
			)
		}
	}
}
