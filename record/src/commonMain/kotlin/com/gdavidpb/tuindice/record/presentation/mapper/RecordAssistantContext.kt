package com.gdavidpb.tuindice.record.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.isHistorical
import com.gdavidpb.tuindice.academiccore.domain.model.isSynthetic
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityKind
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityRef
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantSuggestionFacts
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.record.domain.model.filteredProjectionFor
import com.gdavidpb.tuindice.record.presentation.contract.Record

private const val ASSISTANT_GRADE_DECIMALS = 4

// What the assistant reads of the record screen: the term on screen, its attempts and their subjects in the order the
// list shows them, and the figures the term shows. It reads the projection the screen draws, so a grade being dragged
// or a term reached by a jump is described as the student sees it.
internal fun recordAssistantContext(content: Record.State.Content): AssistantScreenContext {
	val terms = content.visibleRecord
		.filteredProjectionFor(content.viewMode)
		.terms
	val term = terms.firstOrNull { projected -> projected.id == content.selectedTermId }
	val attempts = term?.attempts.orEmpty()
	val closedTerms = terms.filter { it.kind.isHistorical }
	val latestClosed = closedTerms.maxWithOrNull(compareBy({ it.termOrder }, { it.id }))

	return AssistantScreenContext(
		screen = AssistantScreen.RECORD,
		entities = buildList {
			add(AssistantEntityRef(kind = AssistantEntityKind.TERM, id = content.selectedTermId))
			attempts.forEach { attempt ->
				add(AssistantEntityRef(kind = AssistantEntityKind.ATTEMPT, id = attempt.id))
			}
			attempts
				.map { attempt -> attempt.subjectCode }
				.distinct()
				.forEach { subjectCode ->
					add(AssistantEntityRef(kind = AssistantEntityKind.SUBJECT, id = subjectCode))
				}
		},
		visibleFields = buildMap {
			// termIndex is the term's own average (labelled "Trimestre" on screen) and cumulativeIndex the running
			// average (labelled "Acumulado"). Both come from the projection, as the screen shows them.
			if (term != null) {
				put("termLabel", term.periodLabel)
				put("termIndex", term.periodAverage.formatGrade(decimals = ASSISTANT_GRADE_DECIMALS))
				put("cumulativeIndex", term.cumulativeAverage.formatGrade(decimals = ASSISTANT_GRADE_DECIMALS))
				put("credits", term.periodCredits.toString())
			}
			put("viewMode", content.viewMode.storageValue)
			put("isSynthetic", (term?.kind?.isSynthetic ?: content.isSelectedTermSynthetic).toString())
		},
		filters = emptyMap(),
		suggestionFacts = AssistantSuggestionFacts(
			selectedTermIsCurrent = term?.let { content.record.isCurrentTerm(it.id) },
			selectedTermIsLatestClosed = term?.let { it.id == latestClosed?.id },
			hasIndexHistory = closedTerms.count { it.periodAverage > 0 } > 1,
			hasBestTerm = closedTerms.any { it.periodAverage > 0 && it.periodCode != AcademicTermPeriod.JUL_AUG }
		)
	)
}
