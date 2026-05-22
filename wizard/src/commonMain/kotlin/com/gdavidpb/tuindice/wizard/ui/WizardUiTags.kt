package com.gdavidpb.tuindice.wizard.ui

import com.gdavidpb.tuindice.wizard.presentation.model.WizardStepId

object WizardUiTags {
	const val Screen = "wizard_screen"
	const val WelcomeScreen = "wizard_welcome_screen"
	const val GuideBar = "wizard_guide_bar"
	const val BackButton = "wizard_back_button"
	const val SkipButton = "wizard_skip_button"
	const val PrimaryButton = "wizard_primary_button"
	const val ProgressText = "wizard_progress_text"
	const val FocusOverlay = "wizard_focus_overlay"
	const val FocusLabel = "wizard_focus_label"

	fun currentStep(stepId: WizardStepId): String = "wizard_current_step_${stepId.tagValue()}"

	private fun WizardStepId.tagValue(): String = when (this) {
		WizardStepId.Welcome -> "welcome"
		WizardStepId.Summary -> "summary"
		WizardStepId.Record -> "record"
		WizardStepId.RecordActions -> "record_actions"
		WizardStepId.CreateSyntheticTerm -> "create_synthetic_term"
		WizardStepId.Pensum -> "pensum"
		WizardStepId.SubjectDetail -> "subject_detail"
		WizardStepId.SubjectCharts -> "subject_charts"
		WizardStepId.Evaluations -> "evaluations"
		WizardStepId.EvaluationSwipe -> "evaluation_swipe"
		WizardStepId.EvaluationForm -> "evaluation_form"
		WizardStepId.About -> "about"
	}
}
