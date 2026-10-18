package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityKind
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityRef
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.summary.presentation.model.TermReportItem

// What the assistant reads of the report: the term it is about, named by the label the screen gives the term, and its
// index as the screen shows it. The chip of the chat shows the label, so the report is named by its term.
internal fun termReportAssistantContext(report: TermReportItem): AssistantScreenContext {
	return AssistantScreenContext(
		screen = AssistantScreen.TERM_REPORT,
		entities = buildList {
			report.termId?.let { termId -> add(AssistantEntityRef(kind = AssistantEntityKind.TERM, id = termId)) }
		},
		visibleFields = buildMap {
			put("termLabel", report.periodLabel)
			report.hero.index?.let { index -> put("termIndex", index) }
		}
	)
}
