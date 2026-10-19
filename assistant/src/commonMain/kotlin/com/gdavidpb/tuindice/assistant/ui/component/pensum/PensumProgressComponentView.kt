package com.gdavidpb.tuindice.assistant.ui.component.pensum

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.mapper.chatCardFraction
import com.gdavidpb.tuindice.assistant.presentation.mapper.chatCardPercentOf
import com.gdavidpb.tuindice.assistant.presentation.mapper.chatCardPercentText
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardProgressView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRecordLinkView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardValueView
import com.gdavidpb.tuindice.assistant.ui.model.CardFigure
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.chatcontract.component.PensumProgressComponent
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_fraction
import tuindice.assistant.generated.resources.assistant_card_b_progress_approved
import tuindice.assistant.generated.resources.assistant_card_b_progress_available
import tuindice.assistant.generated.resources.assistant_card_b_progress_blocked
import tuindice.assistant.generated.resources.assistant_card_b_progress_caption
import tuindice.assistant.generated.resources.assistant_card_b_progress_credits_caption
import tuindice.assistant.generated.resources.assistant_card_b_progress_credits_description
import tuindice.assistant.generated.resources.assistant_card_b_progress_description
import tuindice.assistant.generated.resources.assistant_card_b_progress_in_progress
import tuindice.assistant.generated.resources.assistant_card_b_progress_label

// pensum.avance/1: the share of the credits approved is the figure, with the credits beside it, and a bar of the same
// share. Then how many subjects are approved, in progress, available and blocked, one row each.
@Composable
fun PensumProgressComponentView(
	component: PensumProgressComponent,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	val percent = chatCardPercentText(chatCardPercentOf(component.approvedCredits, component.totalCredits)).asString()
	val approvedCredits = component.approvedCredits.toString()
	val totalCredits = component.totalCredits.toString()
	val statuses = listOf(
		Res.string.assistant_card_b_progress_approved to component.approved,
		Res.string.assistant_card_b_progress_in_progress to component.inProgress,
		Res.string.assistant_card_b_progress_available to component.available,
		Res.string.assistant_card_b_progress_blocked to component.blocked
	)

	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Pensum.progress(component.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_b_progress_label))
		CardFiguresView(
			primary = CardFigure(
				value = percent,
				caption = stringResource(Res.string.assistant_card_b_progress_caption),
				contentDescription = stringResource(Res.string.assistant_card_b_progress_description, percent)
			),
			secondary = CardFigure(
				value = stringResource(Res.string.assistant_card_b_fraction, approvedCredits, totalCredits),
				caption = stringResource(Res.string.assistant_card_b_progress_credits_caption),
				contentDescription = stringResource(
					Res.string.assistant_card_b_progress_credits_description,
					approvedCredits,
					totalCredits
				)
			)
		)
		CardProgressView(
			fraction = chatCardFraction(component.approvedCredits.toDouble(), component.totalCredits.toDouble())
		)
		statuses.forEachIndexed { index, (label, count) ->
			CardRowView(
				name = stringResource(label),
				showsDivider = index > 0,
				trailing = { CardValueView(text = count.toString()) }
			)
		}
		CardRecordLinkView(entityRefs = component.entityRefs, onAction = onAction)
	}
}
