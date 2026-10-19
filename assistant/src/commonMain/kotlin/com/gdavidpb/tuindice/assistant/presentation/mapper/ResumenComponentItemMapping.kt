package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.AttentionCardItem
import com.gdavidpb.tuindice.assistant.presentation.model.AttentionRowItem
import com.gdavidpb.tuindice.assistant.presentation.model.CardFigureItem
import com.gdavidpb.tuindice.assistant.presentation.model.CardLinkItem
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.component.AttentionComponent
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import org.jetbrains.compose.resources.StringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_attention_caption
import tuindice.assistant.generated.resources.assistant_card_a_attention_description
import tuindice.assistant.generated.resources.assistant_card_a_link_evaluation
import tuindice.assistant.generated.resources.assistant_card_b_link_pensum
import tuindice.assistant.generated.resources.assistant_card_b_link_record
import tuindice.assistant.generated.resources.assistant_card_b_link_subject

internal fun AttentionComponent.toAttentionCardItem(): AttentionCardItem {
	val count = UiText.Raw(items.size.toString())

	return AttentionCardItem(
		id = id,
		opensSheet = opensSheet(),
		count = CardFigureItem(
			value = count,
			caption = UiText.Resource(Res.string.assistant_card_a_attention_caption),
			contentDescription = UiText.Resource(Res.string.assistant_card_a_attention_description, listOf(count))
		),
		rows = items.map { item ->
			val ref = item.entityRef
			val link = ref?.kind?.attentionLink()

			AttentionRowItem(
				title = item.title,
				detail = item.detail,
				link = if (ref != null && link != null) {
					val (target, label) = link

					CardLinkItem(target = target, text = UiText.Resource(label), entityRefs = listOf(ref))
				} else {
					null
				}
			)
		}
	)
}

// The screen an item opens, and the text of its link, by the kind of the record it names.
private fun EntityKind.attentionLink(): Pair<NavTarget, StringResource>? {
	return when (this) {
		EntityKind.EVALUATION -> NavTarget.EVALUATIONS_DETAIL to Res.string.assistant_card_a_link_evaluation
		EntityKind.SUBJECT -> NavTarget.SUBJECTS_DETAIL to Res.string.assistant_card_b_link_subject
		EntityKind.TERM, EntityKind.ATTEMPT -> NavTarget.RECORD to Res.string.assistant_card_b_link_record
		EntityKind.PENSUM -> NavTarget.PENSUM to Res.string.assistant_card_b_link_pensum
		EntityKind.PLAN -> null
	}
}
