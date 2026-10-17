package com.gdavidpb.tuindice.assistant.ui.component.materias

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardChipView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFigure
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLinkView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRecordLinkView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.model.subjectStatusText
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.component.SubjectListComponent
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_credits
import tuindice.assistant.generated.resources.assistant_card_b_joined
import tuindice.assistant.generated.resources.assistant_card_b_link_subject
import tuindice.assistant.generated.resources.assistant_card_b_subjects_caption
import tuindice.assistant.generated.resources.assistant_card_b_subjects_description
import tuindice.assistant.generated.resources.assistant_card_b_subjects_label

// materias.lista/1: how many subjects there are, then a row per subject: its code, its name and its credits, the chip
// of its status, and a link to its screen.
@Composable
fun SubjectListComponentView(
	component: SubjectListComponent,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	val count = component.items.size.toString()

	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Subjects.list(component.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_b_subjects_label))
		CardFiguresView(
			primary = CardFigure(
				value = count,
				caption = stringResource(Res.string.assistant_card_b_subjects_caption),
				contentDescription = stringResource(Res.string.assistant_card_b_subjects_description, count)
			)
		)
		component.items.forEachIndexed { index, subject ->
			val credits = stringResource(Res.string.assistant_card_b_credits, subject.credits)

			CardRowView(
				code = subject.code,
				name = stringResource(Res.string.assistant_card_b_joined, subject.name, credits),
				showsDivider = index > 0,
				trailing = {
					Column(horizontalAlignment = Alignment.End) {
						subjectStatusText(subject.status)?.let { text -> CardChipView(text = text.asString()) }
						CardLinkView(
							text = stringResource(Res.string.assistant_card_b_link_subject),
							onClick = {
								val ref = EntityRef(EntityKind.SUBJECT, subject.code)

								onAction(AssistantChat.Action.OpenNavTarget(NavTarget.SUBJECTS_DETAIL, listOf(ref)))
							}
						)
					}
				}
			)
		}
		CardRecordLinkView(entityRefs = component.entityRefs, onAction = onAction)
	}
}
