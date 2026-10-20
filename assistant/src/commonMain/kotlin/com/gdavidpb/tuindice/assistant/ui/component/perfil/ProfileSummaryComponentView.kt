package com.gdavidpb.tuindice.assistant.ui.component.perfil

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.model.ProfileSummaryItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardValueView
import com.gdavidpb.tuindice.assistant.ui.model.toCardFigure
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_profile_current_term
import tuindice.assistant.generated.resources.assistant_card_a_profile_label
import tuindice.assistant.generated.resources.assistant_card_a_profile_pensum

// perfil.resumen/1: the cumulative index, with the approved credits beside it, then the career, the pensum and the
// current term. A figure the card does not bring is left out, and the credits take its place.
@Composable
fun ProfileSummaryComponentView(
	item: ProfileSummaryItem,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Profile.summary(item.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_a_profile_label))
		CardFiguresView(
			primary = item.primaryFigure.toCardFigure(),
			secondary = item.secondaryFigure?.toCardFigure()
		)
		CardRowView(code = item.careerCode, name = item.careerName, showsDivider = false)
		CardRowView(
			name = stringResource(Res.string.assistant_card_a_profile_pensum),
			showsDivider = true,
			trailing = { CardValueView(text = item.pensumLabel) }
		)
		item.currentTermLabel?.let { term ->
			CardRowView(
				name = stringResource(Res.string.assistant_card_a_profile_current_term),
				showsDivider = true,
				trailing = { CardValueView(text = term) }
			)
		}
	}
}
