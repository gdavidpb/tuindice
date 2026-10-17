package com.gdavidpb.tuindice.assistant.ui.component.resumen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFigure
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLinkView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.chatcontract.component.AttentionComponent
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_attention_caption
import tuindice.assistant.generated.resources.assistant_card_a_attention_description
import tuindice.assistant.generated.resources.assistant_card_a_attention_label
import tuindice.assistant.generated.resources.assistant_card_a_link_evaluation
import tuindice.assistant.generated.resources.assistant_card_a_link_pensum
import tuindice.assistant.generated.resources.assistant_card_a_link_record
import tuindice.assistant.generated.resources.assistant_card_a_link_subject

// resumen.atencion/1: how many items need attention, then one row per item. An item links to the screen of the
// record it names; a kind of record with no screen of its own has no link.
@Composable
fun AttentionComponentView(
	component: AttentionComponent,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	val count = component.items.size.toString()

	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Summary.attention(component.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_a_attention_label))
		CardFiguresView(
			primary = CardFigure(
				value = count,
				caption = stringResource(Res.string.assistant_card_a_attention_caption),
				contentDescription = stringResource(Res.string.assistant_card_a_attention_description, count)
			)
		)
		component.items.forEachIndexed { index, item ->
			val ref = item.entityRef
			val link = ref?.kind?.link()

			CardRowView(
				code = item.title,
				name = item.detail,
				showsDivider = index > 0,
				trailing = {
					if (ref != null && link != null) {
						val (target, label) = link

						CardLinkView(
							text = stringResource(label),
							onClick = { onAction(AssistantChat.Action.OpenNavTarget(target, listOf(ref))) }
						)
					}
				}
			)
		}
	}
}

// The screen an item opens, and the text of its link, by the kind of the record it names.
private fun EntityKind.link(): Pair<NavTarget, StringResource>? =
	when (this) {
		EntityKind.EVALUATION -> NavTarget.EVALUATIONS_DETAIL to Res.string.assistant_card_a_link_evaluation
		EntityKind.SUBJECT -> NavTarget.SUBJECTS_DETAIL to Res.string.assistant_card_a_link_subject
		EntityKind.TERM, EntityKind.ATTEMPT -> NavTarget.RECORD to Res.string.assistant_card_a_link_record
		EntityKind.PENSUM -> NavTarget.PENSUM to Res.string.assistant_card_a_link_pensum
		EntityKind.PLAN -> null
	}
