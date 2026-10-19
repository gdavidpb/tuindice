package com.gdavidpb.tuindice.assistant.ui.component.plan

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.mapper.chatCardCountText
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFigure
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLinkView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRecordLinkView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardValueView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.component.PlanListComponent
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_count_subject_many
import tuindice.assistant.generated.resources.assistant_card_b_count_subject_one
import tuindice.assistant.generated.resources.assistant_card_b_credits
import tuindice.assistant.generated.resources.assistant_card_b_joined
import tuindice.assistant.generated.resources.assistant_card_b_link_record
import tuindice.assistant.generated.resources.assistant_card_b_plans_caption
import tuindice.assistant.generated.resources.assistant_card_b_plans_description
import tuindice.assistant.generated.resources.assistant_card_b_plans_label

// plan.lista/1: how many plans there are, then a row per term plan: its term, its subjects and its credits, and a link
// to the record of that term.
@Composable
fun PlanListComponentView(
	component: PlanListComponent,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	val count = component.plans.size.toString()

	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Plan.list(component.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_b_plans_label))
		CardFiguresView(
			primary = CardFigure(
				value = count,
				caption = stringResource(Res.string.assistant_card_b_plans_caption),
				contentDescription = stringResource(Res.string.assistant_card_b_plans_description, count)
			)
		)
		component.plans.forEachIndexed { index, plan ->
			val subjects = chatCardCountText(
				count = plan.subjectCodes.size,
				one = Res.string.assistant_card_b_count_subject_one,
				many = Res.string.assistant_card_b_count_subject_many
			).asString()
			val summary = stringResource(
				Res.string.assistant_card_b_joined,
				subjects,
				stringResource(Res.string.assistant_card_b_credits, plan.credits)
			)

			CardRowView(
				name = plan.termLabel,
				showsDivider = index > 0,
				trailing = {
					Column(horizontalAlignment = Alignment.End) {
						CardValueView(text = summary)
						CardLinkView(
							text = stringResource(Res.string.assistant_card_b_link_record),
							onClick = {
								val ref = EntityRef(EntityKind.TERM, plan.termKey)

								onAction(AssistantChat.Action.OpenNavTarget(NavTarget.RECORD, listOf(ref)))
							}
						)
					}
				}
			)
		}
		CardRecordLinkView(entityRefs = component.entityRefs, onAction = onAction)
	}
}
