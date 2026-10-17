package com.gdavidpb.tuindice.assistant.ui.component.perfil

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFigure
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardValueView
import com.gdavidpb.tuindice.assistant.ui.model.toCardIndexText
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.chatcontract.component.ProfileSummaryComponent
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_fraction
import tuindice.assistant.generated.resources.assistant_card_a_profile_credits_caption
import tuindice.assistant.generated.resources.assistant_card_a_profile_credits_description
import tuindice.assistant.generated.resources.assistant_card_a_profile_current_term
import tuindice.assistant.generated.resources.assistant_card_a_profile_index_caption
import tuindice.assistant.generated.resources.assistant_card_a_profile_index_description
import tuindice.assistant.generated.resources.assistant_card_a_profile_label
import tuindice.assistant.generated.resources.assistant_card_a_profile_pensum

// perfil.resumen/1: the cumulative index, with the approved credits beside it, then the career, the pensum and the
// current term. A figure the card does not bring is left out, and the credits take its place.
@Composable
fun ProfileSummaryComponentView(
	component: ProfileSummaryComponent,
	modifier: Modifier = Modifier
) {
	val approved = component.approvedCredits.toString()
	val credits = component.totalCredits?.let { total ->
		stringResource(Res.string.assistant_card_a_fraction, approved, total)
	} ?: approved
	val creditsFigure = CardFigure(
		value = credits,
		caption = stringResource(Res.string.assistant_card_a_profile_credits_caption),
		contentDescription = stringResource(Res.string.assistant_card_a_profile_credits_description, credits)
	)
	val indexFigure = component.cumulativeIndex?.let { index ->
		val value = index.toCardIndexText()

		CardFigure(
			value = value,
			caption = stringResource(Res.string.assistant_card_a_profile_index_caption),
			contentDescription = stringResource(Res.string.assistant_card_a_profile_index_description, value)
		)
	}

	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Profile.summary(component.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_a_profile_label))
		CardFiguresView(
			primary = indexFigure ?: creditsFigure,
			secondary = if (indexFigure != null) creditsFigure else null
		)
		CardRowView(code = component.careerCode, name = component.careerName, showsDivider = false)
		CardRowView(
			name = stringResource(Res.string.assistant_card_a_profile_pensum),
			trailing = { CardValueView(text = component.pensumLabel) }
		)
		component.currentTermLabel?.let { term ->
			CardRowView(
				name = stringResource(Res.string.assistant_card_a_profile_current_term),
				trailing = { CardValueView(text = term) }
			)
		}
	}
}
