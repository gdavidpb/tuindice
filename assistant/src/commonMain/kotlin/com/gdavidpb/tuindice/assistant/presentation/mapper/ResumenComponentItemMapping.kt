package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.AttentionCardItem
import com.gdavidpb.tuindice.assistant.presentation.model.AttentionRowItem
import com.gdavidpb.tuindice.assistant.presentation.model.CardLinkItem
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.component.AttentionComponent
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import org.jetbrains.compose.resources.StringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_link_evaluation
import tuindice.assistant.generated.resources.assistant_card_a_link_evaluation_named
import tuindice.assistant.generated.resources.assistant_card_a_see_all_attention
import tuindice.assistant.generated.resources.assistant_card_b_link_pensum
import tuindice.assistant.generated.resources.assistant_card_b_link_record
import tuindice.assistant.generated.resources.assistant_card_b_link_subject
import tuindice.assistant.generated.resources.assistant_card_b_link_subject_named

internal fun AttentionComponent.toAttentionCardItem(): AttentionCardItem {
	return AttentionCardItem(
		id = id,
		opensSheet = opensSheet(),
		rows = items.map { item ->
			val ref = item.entityRef
			val link = ref?.kind?.attentionLink()

			AttentionRowItem(
				title = item.title,
				detail = item.detail,
				link = if (ref != null && link != null) {
					val (target, label) = link

					CardLinkItem(
						target = target,
						text = UiText.Resource(label),
						entityRefs = listOf(ref),
						accessibilityText = ref.attentionAccessibilityText(item.title)
					)
				} else {
					null
				}
			)
		},
		seeAllText = UiText.Resource(Res.string.assistant_card_a_see_all_attention, listOf(items.size))
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

// What a screen reader says for the link of an item, naming the record it opens: the evaluation by its title, the
// subject by its code. A kind with no name of its own reads its text.
private fun EntityRef.attentionAccessibilityText(title: String): UiText? {
	return when (kind) {
		EntityKind.EVALUATION -> UiText.Resource(Res.string.assistant_card_a_link_evaluation_named, listOf(title))
		EntityKind.SUBJECT -> UiText.Resource(Res.string.assistant_card_b_link_subject_named, listOf(id))
		EntityKind.TERM, EntityKind.ATTEMPT, EntityKind.PENSUM, EntityKind.PLAN -> null
	}
}
