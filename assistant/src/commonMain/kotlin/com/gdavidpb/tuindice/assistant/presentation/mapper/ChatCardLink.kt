package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.CardLinkItem
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import org.jetbrains.compose.resources.StringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_link_evaluation
import tuindice.assistant.generated.resources.assistant_card_b_link_pensum
import tuindice.assistant.generated.resources.assistant_card_b_link_record
import tuindice.assistant.generated.resources.assistant_card_b_link_subject

// The link of a card: the screen of the record it names, and the text of the link. The card takes the first of its
// references that has a screen, and it has no link when that kind is named more than once, because the link would open
// one of those records by chance. A plan has no screen of its own, so a card that names only plans has no link.
fun List<EntityRef>.chatCardLink(): Pair<NavTarget, StringResource>? {
	val named = firstOrNull { ref -> ref.kind.link() != null } ?: return null
	val sameKind = count { ref -> ref.kind == named.kind }

	return if (sameKind == 1) named.kind.link() else null
}

// The screen a kind of record opens, and the text of its link. Null for a kind with no screen of its own.
private fun EntityKind.link(): Pair<NavTarget, StringResource>? =
	when (this) {
		EntityKind.EVALUATION -> NavTarget.EVALUATIONS_DETAIL to Res.string.assistant_card_b_link_evaluation
		EntityKind.SUBJECT -> NavTarget.SUBJECTS_DETAIL to Res.string.assistant_card_b_link_subject
		EntityKind.TERM, EntityKind.ATTEMPT -> NavTarget.RECORD to Res.string.assistant_card_b_link_record
		EntityKind.PENSUM -> NavTarget.PENSUM to Res.string.assistant_card_b_link_pensum
		EntityKind.PLAN -> null
	}

// The link of a card as the card shows it: the screen of the record it names, and the text of the link.
internal fun List<EntityRef>.toRecordLinkItem(): CardLinkItem? {
	val (target, label) = chatCardLink() ?: return null

	return CardLinkItem(target = target, text = UiText.Resource(label), entityRefs = this)
}
