package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_link_record
import tuindice.assistant.generated.resources.assistant_card_b_link_record_named
import tuindice.assistant.generated.resources.assistant_card_b_link_subject
import tuindice.assistant.generated.resources.assistant_card_b_link_subject_named
import kotlin.test.Test
import kotlin.test.assertEquals

// The link of a row of a list names the record it opens for a screen reader, since the links of a list read alike.
class ChatCardLinkItemTest {
	@Test
	fun when_aSubjectLinkIsBuilt_then_itOpensTheSubjectAndNamesItsCode() {
		val link = chatSubjectLinkItem("CI5311")

		assertEquals(NavTarget.SUBJECTS_DETAIL, link.target)
		assertEquals(UiText.Resource(Res.string.assistant_card_b_link_subject), link.text)
		assertEquals(listOf(EntityRef(EntityKind.SUBJECT, "CI5311")), link.entityRefs)
		assertEquals(
			UiText.Resource(Res.string.assistant_card_b_link_subject_named, listOf("CI5311")),
			link.accessibilityText
		)
	}

	@Test
	fun when_aTermLinkIsBuilt_then_itOpensTheReportAndNamesTheTerm() {
		val refs = listOf(EntityRef(EntityKind.TERM, "2025-SEP_DIC"))
		val link = chatRecordLinkItem(entityRefs = refs, termLabel = "Sep–Dic 2025")

		assertEquals(NavTarget.RECORD, link.target)
		assertEquals(UiText.Resource(Res.string.assistant_card_b_link_record), link.text)
		assertEquals(refs, link.entityRefs)
		assertEquals(
			UiText.Resource(Res.string.assistant_card_b_link_record_named, listOf("Sep–Dic 2025")),
			link.accessibilityText
		)
	}
}
