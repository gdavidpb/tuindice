package com.gdavidpb.tuindice.assistant.ui.component.pensum

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.mapper.subjectStatusText
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardChipView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLinkView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRecordLinkView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.component.RequirementsComponent
import com.gdavidpb.tuindice.chatcontract.component.SubjectRef
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_link_subject
import tuindice.assistant.generated.resources.assistant_card_b_requirements_corequisites
import tuindice.assistant.generated.resources.assistant_card_b_requirements_prerequisites
import tuindice.assistant.generated.resources.assistant_card_b_requirements_unlocks

// pensum.requisitos/1: the subject is the label. Then one section each for its prerequisites, its corequisites and the
// subjects it unlocks, and a section with no subjects is not drawn. A subject shows its status and links to its screen.
@Composable
fun RequirementsComponentView(
	component: RequirementsComponent,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Pensum.requirements(component.id))) {
		CardLabelView(text = component.subjectName)
		RequirementSection(
			label = Res.string.assistant_card_b_requirements_prerequisites,
			subjects = component.prerequisites,
			onAction = onAction
		)
		RequirementSection(
			label = Res.string.assistant_card_b_requirements_corequisites,
			subjects = component.corequisites,
			onAction = onAction
		)
		RequirementSection(
			label = Res.string.assistant_card_b_requirements_unlocks,
			subjects = component.unlocks,
			onAction = onAction
		)
		CardRecordLinkView(entityRefs = component.entityRefs, onAction = onAction)
	}
}

@Composable
private fun RequirementSection(
	label: StringResource,
	subjects: List<SubjectRef>,
	onAction: (AssistantChat.Action) -> Unit
) {
	if (subjects.isEmpty()) return

	CardLabelView(text = stringResource(label))
	subjects.forEachIndexed { index, subject ->
		CardRowView(
			code = subject.code,
			name = subject.name,
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
}
