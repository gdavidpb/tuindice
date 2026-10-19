package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_link_evaluation
import tuindice.assistant.generated.resources.assistant_card_b_link_pensum
import tuindice.assistant.generated.resources.assistant_card_b_link_record
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

// A card links to the record it names, and only when that record is one the card can name without choosing.
class ChatCardLinkTest {
	@Test
	fun when_aCardNamesOneRecordWithAScreen_then_itLinksToThatScreen() {
		val evaluation = listOf(EntityRef(EntityKind.EVALUATION, "eval-1"))
		val term = listOf(EntityRef(EntityKind.TERM, "2026-JAN_MAR"))

		assertEquals(NavTarget.EVALUATIONS_DETAIL to Res.string.assistant_card_a_link_evaluation, evaluation.chatCardLink())
		assertEquals(NavTarget.RECORD to Res.string.assistant_card_b_link_record, term.chatCardLink())
	}

	@Test
	fun when_theFirstRecordHasNoScreen_then_theNextOneIsTheLink() {
		val refs = listOf(EntityRef(EntityKind.PLAN, "plan-1"), EntityRef(EntityKind.PENSUM, "IS-2016"))

		assertEquals(NavTarget.PENSUM to Res.string.assistant_card_b_link_pensum, refs.chatCardLink())
	}

	@Test
	fun when_aCardNamesSeveralRecordsOfOneKind_then_itHasNoLink() {
		val subjects = listOf(EntityRef(EntityKind.SUBJECT, "IS-101"), EntityRef(EntityKind.SUBJECT, "IS-102"))

		assertNull(subjects.chatCardLink())
	}

	@Test
	fun when_aCardNamesOnlyAPlanOrNothing_then_itHasNoLink() {
		assertNull(listOf(EntityRef(EntityKind.PLAN, "plan-1")).chatCardLink())
		assertNull(emptyList<EntityRef>().chatCardLink())
	}
}
