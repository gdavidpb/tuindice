package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.CardLinkItem
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import org.jetbrains.compose.resources.StringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_link_evaluation
import tuindice.assistant.generated.resources.assistant_card_b_link_pensum
import tuindice.assistant.generated.resources.assistant_card_b_link_record
import tuindice.assistant.generated.resources.assistant_card_b_link_record_named
import tuindice.assistant.generated.resources.assistant_card_b_link_subject
import tuindice.assistant.generated.resources.assistant_card_b_link_subject_named

// The link of a card: the screen of the record it names, and the text of the link. The card takes the first of its
// references that has a screen, and it has no link when that kind is named more than once, because the link would open
// one of those records by chance. A plan has no screen of its own, so a card that names only plans has no link.
fun List<EntityRef>.chatCardLink(): Pair<NavTarget, StringResource>? {
	val named = firstOrNull { ref -> ref.kind.link() != null } ?: return null
	val sameKind = count { ref -> ref.kind == named.kind }

	return if (sameKind == 1) named.kind.link() else null
}

// The screen a kind of record opens, and the text of its link. Null for a kind with no screen of its own.
internal fun EntityKind.link(): Pair<NavTarget, StringResource>? =
	when (this) {
		EntityKind.EVALUATION -> NavTarget.EVALUATIONS_DETAIL to Res.string.assistant_card_a_link_evaluation
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

// The link of a subject in a list. Every subject of a list says «Ver materia» on its own, so the screen reader names
// the code it opens («Ver materia CI5311»).
internal fun chatSubjectLinkItem(code: String): CardLinkItem =
	CardLinkItem(
		target = NavTarget.SUBJECTS_DETAIL,
		text = UiText.Resource(Res.string.assistant_card_b_link_subject),
		entityRefs = listOf(EntityRef(EntityKind.SUBJECT, code)),
		accessibilityText = UiText.Resource(Res.string.assistant_card_b_link_subject_named, listOf(code))
	)

// The link to the report of a term, with the term it names for a screen reader («Ver Sep–Dic 2025 en Informe»).
internal fun chatRecordLinkItem(entityRefs: List<EntityRef>, termLabel: String): CardLinkItem =
	CardLinkItem(
		target = NavTarget.RECORD,
		text = UiText.Resource(Res.string.assistant_card_b_link_record),
		entityRefs = entityRefs,
		accessibilityText = UiText.Resource(Res.string.assistant_card_b_link_record_named, listOf(termLabel))
	)
