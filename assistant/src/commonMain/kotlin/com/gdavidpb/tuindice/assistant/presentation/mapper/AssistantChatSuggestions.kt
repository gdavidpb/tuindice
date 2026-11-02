package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityKind
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantSuggestionFacts
import com.gdavidpb.tuindice.chatcontract.message.SuggestedQuestions
import com.gdavidpb.tuindice.chatcontract.message.SuggestedReply

// The service resolves each id to its own catalog question, including in guided mode.
internal fun contextualSuggestions(context: AssistantScreenContext?, guided: Boolean): List<SuggestedReply> {
	val facts = context?.suggestionFacts ?: AssistantSuggestionFacts()
	val ids = when (context?.screen) {
		AssistantScreen.SUMMARY -> summarySuggestions(facts)
		AssistantScreen.STATISTICS -> statisticsSuggestions(facts)
		AssistantScreen.RECORD -> recordSuggestions(context, facts, guided)
		AssistantScreen.PENSUM -> pensumSuggestions(context, facts)
		AssistantScreen.EVALUATIONS -> evaluationSuggestions(facts)
		AssistantScreen.EVALUATION_DETAIL -> evaluationDetailSuggestions(context)
		AssistantScreen.SUBJECT_DETAIL -> subjectSuggestions(context)
		AssistantScreen.SUBJECT_SEARCH -> listOf("sq.search.electives", "sq.search.available")
		AssistantScreen.ABOUT -> listOf("sq.about.what", "sq.about.data", "sq.about.memory")
		AssistantScreen.TERM_REPORT -> reportSuggestions(context, facts)
		null -> emptyList()
	}

	return ids.ifEmpty { listOf("sq.about.what") }
		.distinct()
		.take(MAX_SUGGESTIONS)
		.mapNotNull(SuggestedQuestions::byId)
}

private fun summarySuggestions(facts: AssistantSuggestionFacts): List<String> = buildList {
	if (facts.hasCurrentTerm == true) add("sq.summary.term")
	if (facts.hasVisibleAttention == true) add("sq.summary.attention")
	if (facts.hasCreditPace == true) add("sq.summary.pace")
}

private fun statisticsSuggestions(facts: AssistantSuggestionFacts): List<String> = buildList {
	if (facts.hasBestTerm == true) add("sq.statistics.best")
	if (facts.hasIndexHistory == true) add("sq.statistics.history")
	if (facts.canProjectRemainingTerms == true) add("sq.statistics.pace")
}

private fun recordSuggestions(
	context: AssistantScreenContext,
	facts: AssistantSuggestionFacts,
	guided: Boolean
): List<String> {
	if (context.hasEntity(AssistantEntityKind.ATTEMPT) && context.hasEntity(AssistantEntityKind.SUBJECT) &&
		!context.hasEntity(AssistantEntityKind.TERM)
	) {
		return subjectSuggestions(context)
	}

	return buildList {
		if (facts.selectedTermIsCurrent == true) add("sq.summary.term")
		if (facts.selectedTermIsLatestClosed == true) add("sq.record.last")
		if (facts.selectedTermIsCurrent == true && !guided) add("sq.record.simulate")
		if (facts.hasIndexHistory == true) add("sq.statistics.history")
		if (facts.hasBestTerm == true) add("sq.statistics.best")
		if (!guided && context.hasEntity(AssistantEntityKind.TERM)) add("sq.record.goal")
	}
}

private fun pensumSuggestions(context: AssistantScreenContext, facts: AssistantSuggestionFacts): List<String> =
	buildList {
		if (context.hasEntity(AssistantEntityKind.PENSUM)) add("sq.pensum.progress")
		if (facts.hasAvailableSubjects == true) add("sq.pensum.available")
	}

private fun evaluationSuggestions(facts: AssistantSuggestionFacts): List<String> = buildList {
	if (facts.hasEvaluations == true && facts.selectedWeekIsCurrent == true) add("sq.evaluations.week")
	if (facts.hasEvaluations == true) {
		add("sq.evaluations.heavy")
		add("sq.evaluations.points")
	}
}

private fun evaluationDetailSuggestions(context: AssistantScreenContext): List<String> =
	if (context.hasEntity(AssistantEntityKind.SUBJECT)) {
		listOf("sq.evaluation.needed", "sq.evaluation.points")
	} else {
		emptyList()
	}

private fun subjectSuggestions(context: AssistantScreenContext): List<String> =
	if (context.hasEntity(AssistantEntityKind.SUBJECT)) {
		listOf("sq.subject.difficulty", "sq.subject.requirements", "sq.subject.unlocks")
	} else {
		// A slot in the pensum is not a subject and has no subject statistics or subject code.
		listOf("sq.pensum.progress")
	}

private fun reportSuggestions(context: AssistantScreenContext, facts: AssistantSuggestionFacts): List<String> =
	buildList {
		if (context.visibleFields["termIndex"] != null) add("sq.term_report.why")
		add("sq.term_report.next")
		if (facts.hasCreditPace == true) add("sq.term_report.pace")
	}

private fun AssistantScreenContext.hasEntity(kind: AssistantEntityKind): Boolean =
	entities.any { it.kind == kind && it.id.isNotBlank() }

private const val MAX_SUGGESTIONS = 3
