package com.gdavidpb.tuindice.assistant.data.mapper

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantNavTarget
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.insights.InsightAction
import com.gdavidpb.tuindice.chatcontract.insights.TermInsight
import kotlin.test.Test
import kotlin.test.assertEquals

class AssistantTermInsightMapperTest {

	@Test
	fun everyTargetOfTheChatBecomesItsOwnAssistantTarget() {
		val insight = TermInsight(
			id = "term-1:2",
			termKey = "2026-APR_JUL",
			kind = "term_end",
			title = "Cerraste con 4,18",
			body = "Aprobaste las 3 materias.",
			nextActions = NavTarget.entries.map { target -> InsightAction(target = target, label = target.name) }
		)

		val targets = insight.toAssistantTermInsight().nextActions.map { action -> action.target }

		assertEquals(NavTarget.entries.size, targets.size)
		assertEquals(AssistantNavTarget.entries.toSet(), targets.toSet())
	}
}
