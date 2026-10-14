package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicWeekEngine
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.domain.model.SummaryNextEvaluation
import com.gdavidpb.tuindice.summary.presentation.model.SummaryCardIcon
import com.gdavidpb.tuindice.summary.presentation.model.SummaryCardItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.a11y_summary_open_evaluations_week
import tuindice.summary.generated.resources.summary_evaluations_invite_action
import tuindice.summary.generated.resources.summary_evaluations_invite_message
import tuindice.summary.generated.resources.summary_evaluations_invite_title
import tuindice.summary.generated.resources.summary_evaluations_pending_message
import tuindice.summary.generated.resources.summary_evaluations_pending_title
import tuindice.summary.generated.resources.summary_term_first_message
import tuindice.summary.generated.resources.summary_term_first_title
import tuindice.summary.generated.resources.summary_term_plan_action
import tuindice.summary.generated.resources.summary_term_plan_message
import tuindice.summary.generated.resources.summary_term_plan_title
import tuindice.summary.generated.resources.summary_week_idle_title
import tuindice.summary.generated.resources.summary_week_next_evaluation
import tuindice.summary.generated.resources.summary_week_no_class_message
import tuindice.summary.generated.resources.summary_week_no_class_title
import tuindice.summary.generated.resources.summary_week_no_term_message
import tuindice.summary.generated.resources.summary_week_no_term_title

// The evaluations of the term have not been downloaded yet: nothing is known about them, so the card claims nothing and
// says where they come from.
internal fun evaluationsPendingCard(): SummaryCardItem {
	return SummaryCardItem(
		icon = SummaryCardIcon.Evaluations,
		titleText = UiText.Resource(Res.string.summary_evaluations_pending_title),
		messageText = UiText.Resource(Res.string.summary_evaluations_pending_message)
	)
}

// The term has no evaluation registered: the invitation to add the first, with the button that opens its form.
internal fun addEvaluationCard(): SummaryCardItem {
	return SummaryCardItem(
		icon = SummaryCardIcon.Evaluations,
		titleText = UiText.Resource(Res.string.summary_evaluations_invite_title),
		messageText = UiText.Resource(Res.string.summary_evaluations_invite_message),
		action = SummaryCardItem.Action(
			text = UiText.Resource(Res.string.summary_evaluations_invite_action),
			target = SummaryHubTarget.AddEvaluation
		)
	)
}

// The week has no class and no evaluation, but the term has evaluations: nothing is pending, and the next one to come
// names itself and leads to its week when there is one.
internal fun nothingPendingCard(next: SummaryNextEvaluation?): SummaryCardItem {
	return SummaryCardItem(
		icon = SummaryCardIcon.Done,
		titleText = UiText.Resource(Res.string.summary_week_idle_title),
		messageText = next?.let { evaluation ->
			UiText.Resource(
				Res.string.summary_week_next_evaluation,
				listOf(evaluation.toNameText(), evaluation.subjectCode, evaluation.toDayText())
			)
		},
		target = next?.let { evaluation ->
			SummaryHubTarget.EvaluationsWeek(
				weekStart = AcademicWeekEngine.mondayOf(evaluation.date),
				evaluationId = evaluation.evaluationId
			)
		},
		targetLabel = next?.let { UiText.Resource(Res.string.a11y_summary_open_evaluations_week) }
	)
}

// No term is in progress and the record has terms: the week has no class to show until the next term is enrolled.
internal fun noClassCard(): SummaryCardItem {
	return SummaryCardItem(
		icon = SummaryCardIcon.Calendar,
		titleText = UiText.Resource(Res.string.summary_week_no_class_title),
		messageText = UiText.Resource(Res.string.summary_week_no_class_message)
	)
}

// The record has no term at all, a new student: the week has nothing yet and says when it will.
internal fun noTermCard(): SummaryCardItem {
	return SummaryCardItem(
		icon = SummaryCardIcon.Calendar,
		titleText = UiText.Resource(Res.string.summary_week_no_term_title),
		messageText = UiText.Resource(Res.string.summary_week_no_term_message)
	)
}

// No term is in progress but the record has terms: the invitation to plan the next one, which opens its form in Record.
internal fun planTermCard(): SummaryCardItem {
	return SummaryCardItem(
		icon = SummaryCardIcon.NewTerm,
		titleText = UiText.Resource(Res.string.summary_term_plan_title),
		messageText = UiText.Resource(Res.string.summary_term_plan_message),
		action = SummaryCardItem.Action(
			text = UiText.Resource(Res.string.summary_term_plan_action),
			target = SummaryHubTarget.CreateTerm
		)
	)
}

// A new student's first term has not been published yet: the card says it will show up with its subjects and points.
internal fun firstTermCard(): SummaryCardItem {
	return SummaryCardItem(
		icon = SummaryCardIcon.NewTerm,
		titleText = UiText.Resource(Res.string.summary_term_first_title),
		messageText = UiText.Resource(Res.string.summary_term_first_message)
	)
}
