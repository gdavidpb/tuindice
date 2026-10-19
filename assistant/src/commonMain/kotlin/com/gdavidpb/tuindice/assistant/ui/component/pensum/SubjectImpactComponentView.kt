package com.gdavidpb.tuindice.assistant.ui.component.pensum

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.mapper.subjectStatusText
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardChipView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRecordLinkView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.model.CardFigure
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.chatcontract.component.SubjectImpactComponent
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_impact_caption
import tuindice.assistant.generated.resources.assistant_card_b_impact_description

// pensum.impacto/1: the subject is the label, and the terms it delays are the figure. Then the chain of subjects that
// depend on it, in the order the service gives them, each with its status.
@Composable
fun SubjectImpactComponentView(
	component: SubjectImpactComponent,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	val terms = component.lengthInTerms.toString()

	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Pensum.impact(component.id))) {
		CardLabelView(text = component.subjectName)
		CardFiguresView(
			primary = CardFigure(
				value = terms,
				caption = stringResource(Res.string.assistant_card_b_impact_caption),
				contentDescription = stringResource(Res.string.assistant_card_b_impact_description, terms)
			)
		)
		component.chain.forEachIndexed { index, subject ->
			CardRowView(
				code = subject.code,
				name = subject.name,
				showsDivider = index > 0,
				trailing = {
					subjectStatusText(subject.status)?.let { text -> CardChipView(text = text.asString()) }
				}
			)
		}
		CardRecordLinkView(entityRefs = component.entityRefs, onAction = onAction)
	}
}
