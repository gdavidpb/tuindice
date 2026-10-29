package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.base.presentation.model.EvaluationTone
import com.gdavidpb.tuindice.base.ui.model.EvaluationCardItem
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationTermDescriptor
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationHighlightTone
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationItem

// The card the Evaluations tab draws for [this] row: its type icon, its name, its subject, its date and place, its
// grade and its state, in the tones the tab gives them. The tab and the assistant both draw it from here, so an
// evaluation reads the same in both.
fun EvaluationItem.toEvaluationCardItem(): EvaluationCardItem {
	return EvaluationCardItem(
		name = typeNameText,
		subjectCode = subjectCodeText,
		dateText = dateText,
		highlightTone = highlightTone.toEvaluationTone(),
		typeIcon = typeIcon,
		place = place,
		gradeText = if (showsGradeAction) gradeText else null,
		statusText = statusText,
		statusTone = statusTone.toEvaluationTone()
	)
}

// The card of every evaluation the Evaluations tab lists, by evaluation id. [this] are the evaluations in the order
// the tab reads them (see sortedAsListed): they go through the tab's own groups by week, so each name carries the
// number the tab gives it. An evaluation the tab does not list (one with no subject, or out of the weeks of the term)
// has no card.
fun List<Evaluation>.toEvaluationCardItems(
	currentTerm: EvaluationTermDescriptor?,
	attempts: List<EditableAttemptDescriptor>,
	mapping: EvaluationItemMapping
): Map<String, EvaluationCardItem> {
	return toWeeklyEvaluationGroups(currentTerm = currentTerm, attempts = attempts, mapping = mapping)
		.values
		.flatten()
		.flatMap { group -> group.items }
		.associate { item -> item.evaluationId to item.toEvaluationCardItem() }
}

private fun EvaluationHighlightTone.toEvaluationTone(): EvaluationTone =
	when (this) {
		EvaluationHighlightTone.Success -> EvaluationTone.Success
		EvaluationHighlightTone.Error -> EvaluationTone.Error
		EvaluationHighlightTone.Neutral -> EvaluationTone.Neutral
	}
