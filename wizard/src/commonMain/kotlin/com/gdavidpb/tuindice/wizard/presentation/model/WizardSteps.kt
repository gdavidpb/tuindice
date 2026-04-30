package com.gdavidpb.tuindice.wizard.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import tuindice.wizard.generated.resources.Res
import tuindice.wizard.generated.resources.wizard_about_message
import tuindice.wizard.generated.resources.wizard_about_title
import tuindice.wizard.generated.resources.wizard_evaluation_form_message
import tuindice.wizard.generated.resources.wizard_evaluation_form_title
import tuindice.wizard.generated.resources.wizard_evaluation_swipe_message
import tuindice.wizard.generated.resources.wizard_evaluation_swipe_title
import tuindice.wizard.generated.resources.wizard_evaluations_message
import tuindice.wizard.generated.resources.wizard_evaluations_title
import tuindice.wizard.generated.resources.wizard_record_message
import tuindice.wizard.generated.resources.wizard_record_actions_message
import tuindice.wizard.generated.resources.wizard_record_actions_title
import tuindice.wizard.generated.resources.wizard_record_subject_entry_message
import tuindice.wizard.generated.resources.wizard_record_subject_entry_title
import tuindice.wizard.generated.resources.wizard_record_title
import tuindice.wizard.generated.resources.wizard_subject_charts_message
import tuindice.wizard.generated.resources.wizard_subject_charts_title
import tuindice.wizard.generated.resources.wizard_subject_message
import tuindice.wizard.generated.resources.wizard_subject_title
import tuindice.wizard.generated.resources.wizard_summary_message
import tuindice.wizard.generated.resources.wizard_summary_title
import tuindice.wizard.generated.resources.wizard_welcome_message
import tuindice.wizard.generated.resources.wizard_welcome_title

fun defaultWizardSteps(): List<WizardStep> = listOf(
	WizardStep(
		id = WizardStepId.Welcome,
		title = Res.string.wizard_welcome_title,
		message = Res.string.wizard_welcome_message,
		topBarTitle = ""
	),
	WizardStep(
		id = WizardStepId.Summary,
		title = Res.string.wizard_summary_title,
		message = Res.string.wizard_summary_message,
		topBarTitle = "Resumen",
		topBarConfig = TopBarConfig.Summary
	),
	WizardStep(
		id = WizardStepId.RecordActions,
		title = Res.string.wizard_record_actions_title,
		message = Res.string.wizard_record_actions_message,
		topBarTitle = "Informe Académico",
		topBarConfig = TopBarConfig.Record,
		showsRecordViewMode = true
	),
	WizardStep(
		id = WizardStepId.Record,
		title = Res.string.wizard_record_title,
		message = Res.string.wizard_record_message,
		topBarTitle = "Informe Académico",
		topBarConfig = TopBarConfig.Record,
		showsRecordViewMode = true
	),
	WizardStep(
		id = WizardStepId.RecordSubjectEntry,
		title = Res.string.wizard_record_subject_entry_title,
		message = Res.string.wizard_record_subject_entry_message,
		topBarTitle = "Informe Académico",
		topBarConfig = TopBarConfig.Record,
		showsRecordViewMode = true
	),
	WizardStep(
		id = WizardStepId.SubjectDetail,
		title = Res.string.wizard_subject_title,
		message = Res.string.wizard_subject_message,
		topBarTitle = "Sobre esta materia"
	),
	WizardStep(
		id = WizardStepId.SubjectCharts,
		title = Res.string.wizard_subject_charts_title,
		message = Res.string.wizard_subject_charts_message,
		topBarTitle = "Sobre esta materia"
	),
	WizardStep(
		id = WizardStepId.Evaluations,
		title = Res.string.wizard_evaluations_title,
		message = Res.string.wizard_evaluations_message,
		topBarTitle = "Evaluaciones"
	),
	WizardStep(
		id = WizardStepId.EvaluationSwipe,
		title = Res.string.wizard_evaluation_swipe_title,
		message = Res.string.wizard_evaluation_swipe_message,
		topBarTitle = "Evaluaciones"
	),
	WizardStep(
		id = WizardStepId.EvaluationForm,
		title = Res.string.wizard_evaluation_form_title,
		message = Res.string.wizard_evaluation_form_message,
		topBarTitle = "Agregar evaluación"
	),
	WizardStep(
		id = WizardStepId.About,
		title = Res.string.wizard_about_title,
		message = Res.string.wizard_about_message,
		topBarTitle = "Acerca de"
	)
)

fun List<WizardStep>.indexOfStep(stepId: WizardStepId): Int {
	return indexOfFirst { step -> step.id == stepId }
		.takeIf { index -> index >= 0 }
		?: 0
}
