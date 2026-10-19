package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityKind
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityRef
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.summary.presentation.model.TermReportItem
import com.gdavidpb.tuindice.summary.presentation.model.TermReportSectionItem

// What the assistant reads of the report: the term it is about, named by the label the screen gives the term, and its
// index as the screen shows it. The chat's chip shows the label, so the report is named by its term. The report itself
// comes with its id and its text, so the chat knows which report the question is about and what the report says.
internal fun termReportAssistantContext(report: TermReportItem): AssistantScreenContext {
	return AssistantScreenContext(
		screen = AssistantScreen.TERM_REPORT,
		entities = buildList {
			report.termId?.let { termId -> add(AssistantEntityRef(kind = AssistantEntityKind.TERM, id = termId)) }
		},
		visibleFields = buildMap {
			put("termLabel", report.periodLabel)
			report.hero.index?.let { index -> put("termIndex", index) }
			put("reportId", report.insightId)
			put("reportTitle", report.title.take(REPORT_TITLE_LIMIT))
			put("reportText", reportTextOf(sections = report.sections))
		}
	)
}

// The text the chat reads of the report: each section, its title and its text, cut to its own limit, in order, and the
// whole cut to the total limit. The backend sends it as it is, within the context the model is given.
internal fun reportTextOf(sections: List<TermReportSectionItem>): String {
	return sections
		.joinToString(separator = SECTION_SEPARATOR) { section ->
			"${section.title}$TITLE_SEPARATOR${section.text}".take(SECTION_LIMIT)
		}
		.take(TEXT_LIMIT)
}

private const val REPORT_TITLE_LIMIT = 200
private const val SECTION_LIMIT = 600
private const val TEXT_LIMIT = 2500
private const val SECTION_SEPARATOR = "\n\n"
private const val TITLE_SEPARATOR = "\n"
