package com.gdavidpb.tuindice.wizard.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.base.presentation.model.UiText
import tuindice.wizard.generated.resources.Res
import tuindice.wizard.generated.resources.top_bar_wizard_about
import tuindice.wizard.generated.resources.top_bar_wizard_add_evaluation
import tuindice.wizard.generated.resources.top_bar_wizard_create_synthetic_term
import tuindice.wizard.generated.resources.top_bar_wizard_evaluations
import tuindice.wizard.generated.resources.top_bar_wizard_pensum
import tuindice.wizard.generated.resources.top_bar_wizard_record
import tuindice.wizard.generated.resources.top_bar_wizard_subject_detail
import tuindice.wizard.generated.resources.top_bar_wizard_summary
import tuindice.wizard.generated.resources.wizard_about_message
import tuindice.wizard.generated.resources.wizard_about_title
import tuindice.wizard.generated.resources.wizard_evaluation_form_message
import tuindice.wizard.generated.resources.wizard_evaluation_form_title
import tuindice.wizard.generated.resources.wizard_evaluation_swipe_message
import tuindice.wizard.generated.resources.wizard_evaluation_swipe_title
import tuindice.wizard.generated.resources.wizard_evaluations_message
import tuindice.wizard.generated.resources.wizard_evaluations_title
import tuindice.wizard.generated.resources.wizard_pensum_message
import tuindice.wizard.generated.resources.wizard_pensum_title
import tuindice.wizard.generated.resources.wizard_record_message
import tuindice.wizard.generated.resources.wizard_record_actions_message
import tuindice.wizard.generated.resources.wizard_record_actions_title
import tuindice.wizard.generated.resources.wizard_record_title
import tuindice.wizard.generated.resources.wizard_synthetic_term_message
import tuindice.wizard.generated.resources.wizard_synthetic_term_title
import tuindice.wizard.generated.resources.wizard_subject_charts_message
import tuindice.wizard.generated.resources.wizard_subject_charts_title
import tuindice.wizard.generated.resources.wizard_subject_message
import tuindice.wizard.generated.resources.wizard_subject_title
import tuindice.wizard.generated.resources.wizard_summary_message
import tuindice.wizard.generated.resources.wizard_summary_title
import tuindice.wizard.generated.resources.wizard_welcome_message
import tuindice.wizard.generated.resources.wizard_welcome_title

// Keep this sequence aligned with the bottom bar sections: Summary, Record, Pensum, Evaluations, About.
fun defaultWizardSteps(): List<WizardStep> = listOf(
	WizardStep(
		id = WizardStepId.Welcome,
		title = Res.string.wizard_welcome_title,
		message = Res.string.wizard_welcome_message,
		topBarTitle = UiText.Empty
	),
	WizardStep(
		id = WizardStepId.Summary,
		title = Res.string.wizard_summary_title,
		message = Res.string.wizard_summary_message,
		topBarTitle = UiText.Resource(Res.string.top_bar_wizard_summary),
		topBarConfig = TopBarConfig.Summary
	),
	WizardStep(
		id = WizardStepId.Record,
		title = Res.string.wizard_record_title,
		message = Res.string.wizard_record_message,
		topBarTitle = UiText.Resource(Res.string.top_bar_wizard_record),
		topBarConfig = TopBarConfig.Record,
		showsRecordViewMode = true
	),
	WizardStep(
		id = WizardStepId.RecordActions,
		title = Res.string.wizard_record_actions_title,
		message = Res.string.wizard_record_actions_message,
		topBarTitle = UiText.Resource(Res.string.top_bar_wizard_record),
		topBarConfig = TopBarConfig.Record,
		showsRecordViewMode = true
	),
	WizardStep(
		id = WizardStepId.CreateSyntheticTerm,
		title = Res.string.wizard_synthetic_term_title,
		message = Res.string.wizard_synthetic_term_message,
		topBarTitle = UiText.Resource(Res.string.top_bar_wizard_create_synthetic_term)
	),
	WizardStep(
		id = WizardStepId.Pensum,
		title = Res.string.wizard_pensum_title,
		message = Res.string.wizard_pensum_message,
		topBarTitle = UiText.Resource(Res.string.top_bar_wizard_pensum),
		topBarConfig = TopBarConfig.Pensum
	),
	WizardStep(
		id = WizardStepId.SubjectDetail,
		title = Res.string.wizard_subject_title,
		message = Res.string.wizard_subject_message,
		topBarTitle = UiText.Resource(Res.string.top_bar_wizard_subject_detail)
	),
	WizardStep(
		id = WizardStepId.SubjectCharts,
		title = Res.string.wizard_subject_charts_title,
		message = Res.string.wizard_subject_charts_message,
		topBarTitle = UiText.Resource(Res.string.top_bar_wizard_subject_detail)
	),
	WizardStep(
		id = WizardStepId.Evaluations,
		title = Res.string.wizard_evaluations_title,
		message = Res.string.wizard_evaluations_message,
		topBarTitle = UiText.Resource(Res.string.top_bar_wizard_evaluations)
	),
	WizardStep(
		id = WizardStepId.EvaluationSwipe,
		title = Res.string.wizard_evaluation_swipe_title,
		message = Res.string.wizard_evaluation_swipe_message,
		topBarTitle = UiText.Resource(Res.string.top_bar_wizard_evaluations)
	),
	WizardStep(
		id = WizardStepId.EvaluationForm,
		title = Res.string.wizard_evaluation_form_title,
		message = Res.string.wizard_evaluation_form_message,
		topBarTitle = UiText.Resource(Res.string.top_bar_wizard_add_evaluation)
	),
	WizardStep(
		id = WizardStepId.About,
		title = Res.string.wizard_about_title,
		message = Res.string.wizard_about_message,
		topBarTitle = UiText.Resource(Res.string.top_bar_wizard_about)
	)
)
