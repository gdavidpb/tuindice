package com.gdavidpb.tuindice.wizard.presentation.model

import tuindice.wizard.generated.resources.Res
import tuindice.wizard.generated.resources.coachmark_about_actions_message
import tuindice.wizard.generated.resources.coachmark_about_actions_title
import tuindice.wizard.generated.resources.coachmark_about_message
import tuindice.wizard.generated.resources.coachmark_about_title
import tuindice.wizard.generated.resources.coachmark_evaluation_editor_message
import tuindice.wizard.generated.resources.coachmark_evaluation_editor_title
import tuindice.wizard.generated.resources.coachmark_evaluations_message
import tuindice.wizard.generated.resources.coachmark_evaluations_title
import tuindice.wizard.generated.resources.coachmark_evaluations_tools_message
import tuindice.wizard.generated.resources.coachmark_evaluations_tools_title
import tuindice.wizard.generated.resources.coachmark_pensum_message
import tuindice.wizard.generated.resources.coachmark_pensum_title
import tuindice.wizard.generated.resources.coachmark_pensum_tools_message
import tuindice.wizard.generated.resources.coachmark_pensum_tools_title
import tuindice.wizard.generated.resources.coachmark_record_controls_message
import tuindice.wizard.generated.resources.coachmark_record_controls_title
import tuindice.wizard.generated.resources.coachmark_record_message
import tuindice.wizard.generated.resources.coachmark_record_title
import tuindice.wizard.generated.resources.coachmark_subject_detail_message
import tuindice.wizard.generated.resources.coachmark_subject_detail_title
import tuindice.wizard.generated.resources.coachmark_subject_search_message
import tuindice.wizard.generated.resources.coachmark_subject_search_title
import tuindice.wizard.generated.resources.coachmark_summary_message
import tuindice.wizard.generated.resources.coachmark_summary_title
import tuindice.wizard.generated.resources.coachmark_synthetic_term_message
import tuindice.wizard.generated.resources.coachmark_synthetic_term_title

fun contextualCoachmarks(): List<Coachmark> = listOf(
	Coachmark(
		id = CoachmarkId.Summary,
		title = Res.string.coachmark_summary_title,
		message = Res.string.coachmark_summary_message
	),
	Coachmark(
		id = CoachmarkId.Record,
		title = Res.string.coachmark_record_title,
		message = Res.string.coachmark_record_message
	),
	Coachmark(
		id = CoachmarkId.RecordControls,
		title = Res.string.coachmark_record_controls_title,
		message = Res.string.coachmark_record_controls_message
	),
	Coachmark(
		id = CoachmarkId.Pensum,
		title = Res.string.coachmark_pensum_title,
		message = Res.string.coachmark_pensum_message
	),
	Coachmark(
		id = CoachmarkId.PensumTools,
		title = Res.string.coachmark_pensum_tools_title,
		message = Res.string.coachmark_pensum_tools_message
	),
	Coachmark(
		id = CoachmarkId.Evaluations,
		title = Res.string.coachmark_evaluations_title,
		message = Res.string.coachmark_evaluations_message
	),
	Coachmark(
		id = CoachmarkId.EvaluationsTools,
		title = Res.string.coachmark_evaluations_tools_title,
		message = Res.string.coachmark_evaluations_tools_message
	),
	Coachmark(
		id = CoachmarkId.EvaluationEditor,
		title = Res.string.coachmark_evaluation_editor_title,
		message = Res.string.coachmark_evaluation_editor_message
	),
	Coachmark(
		id = CoachmarkId.SubjectSearch,
		title = Res.string.coachmark_subject_search_title,
		message = Res.string.coachmark_subject_search_message
	),
	Coachmark(
		id = CoachmarkId.SubjectDetail,
		title = Res.string.coachmark_subject_detail_title,
		message = Res.string.coachmark_subject_detail_message
	),
	Coachmark(
		id = CoachmarkId.SyntheticTerm,
		title = Res.string.coachmark_synthetic_term_title,
		message = Res.string.coachmark_synthetic_term_message
	),
	Coachmark(
		id = CoachmarkId.About,
		title = Res.string.coachmark_about_title,
		message = Res.string.coachmark_about_message
	),
	Coachmark(
		id = CoachmarkId.AboutActions,
		title = Res.string.coachmark_about_actions_title,
		message = Res.string.coachmark_about_actions_message
	)
)
