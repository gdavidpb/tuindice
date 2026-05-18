package com.gdavidpb.tuindice.wizard.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.about.ui.screen.AboutScreen
import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.ui.screen.EvaluationScreen
import com.gdavidpb.tuindice.evaluations.ui.screen.EvaluationsScreen
import com.gdavidpb.tuindice.pensum.ui.screen.PensumScreen
import com.gdavidpb.tuindice.record.ui.screen.CreateSyntheticTermScreen
import com.gdavidpb.tuindice.record.ui.screen.RecordScreen
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSegmentTab
import com.gdavidpb.tuindice.subjects.ui.screen.SubjectDetailScreen
import com.gdavidpb.tuindice.summary.ui.screen.SummaryScreen
import com.gdavidpb.tuindice.wizard.presentation.contract.Wizard
import com.gdavidpb.tuindice.wizard.presentation.model.WizardStepId
import com.gdavidpb.tuindice.wizard.ui.model.sampleAboutState
import com.gdavidpb.tuindice.wizard.ui.model.sampleCreateSyntheticTermState
import com.gdavidpb.tuindice.wizard.ui.model.sampleEvaluationFormState
import com.gdavidpb.tuindice.wizard.ui.model.sampleEvaluationsState
import com.gdavidpb.tuindice.wizard.ui.model.samplePensumState
import com.gdavidpb.tuindice.wizard.ui.model.sampleRecordState
import com.gdavidpb.tuindice.wizard.ui.model.sampleSubjectDetailState
import com.gdavidpb.tuindice.wizard.ui.model.sampleSummaryState
import org.jetbrains.compose.resources.stringResource
import tuindice.wizard.generated.resources.Res
import tuindice.wizard.generated.resources.wizard_subject_close
import tuindice.wizard.generated.resources.wizard_subject_failed_body
import tuindice.wizard.generated.resources.wizard_subject_failed_title
import tuindice.wizard.generated.resources.wizard_subject_loading_stats
import tuindice.wizard.generated.resources.wizard_subject_retry
import tuindice.wizard.generated.resources.wizard_subject_tab_career
import tuindice.wizard.generated.resources.wizard_subject_tab_global
import tuindice.wizard.generated.resources.wizard_subject_title_loading_stats
import tuindice.wizard.generated.resources.wizard_subject_unavailable_body
import tuindice.wizard.generated.resources.wizard_subject_unavailable_title

@Composable
fun WizardStepContent(
	state: Wizard.State.Content,
	onOpenSubjectDetail: () -> Unit,
	onOpenEvaluationForm: () -> Unit,
	onSubjectTabSelected: (SubjectSegmentTab) -> Unit,
	onSelectedTermChange: (String) -> Unit,
	onSubjectChartsVisibilityChange: (Boolean) -> Unit,
	onEvaluationFocusTargetBoundsChange: (Rect?) -> Unit
) {
	val isSubjectStep = state.currentStep.id == WizardStepId.SubjectDetail
		|| state.currentStep.id == WizardStepId.SubjectCharts
	val isSubjectChartsStep = state.currentStep.id == WizardStepId.SubjectCharts
	when (state.currentStep.id) {
		WizardStepId.Welcome ->
			Unit

		WizardStepId.Summary ->
			SummaryScreen(
				state = sampleSummaryState(),
				syncStatus = SyncStatus.Healthy,
				isSyncing = false,
				onRetryClick = {},
				onEditProfilePictureClick = {},
				onUpdatePasswordClick = {}
			)

		WizardStepId.Record,
		WizardStepId.RecordActions ->
			RecordScreen(
				state = sampleRecordState(
					viewMode = state.recordViewMode,
					selectedTermId = state.selectedTermId
				),
				selectedTermId = state.selectedTermId,
				onSelectedTermChange = onSelectedTermChange,
				onRetryClick = {},
				onAttemptSelectionChange = emptyAttemptSelectionHandler(),
				onCreateSyntheticTermClick = {}
			)

		WizardStepId.CreateSyntheticTerm ->
			CreateSyntheticTermScreen(
				state = sampleCreateSyntheticTermState(),
				onQueryChange = {},
				onClearQueryClick = {},
				onPeriodSelected = {},
				onSubjectAdd = {},
				onSubjectRemove = {},
				onCreateClick = {}
			)

		WizardStepId.SubjectDetail,
		WizardStepId.SubjectCharts ->
			SubjectDetailScreen(
				state = sampleSubjectDetailState(selectedTab = state.selectedSubjectTab),
				careerTabText = stringResource(Res.string.wizard_subject_tab_career),
				globalTabText = stringResource(Res.string.wizard_subject_tab_global),
				loadingTitle = stringResource(Res.string.wizard_subject_title_loading_stats),
				loadingMessage = stringResource(Res.string.wizard_subject_loading_stats),
				unavailableTitle = stringResource(Res.string.wizard_subject_unavailable_title),
				unavailableBody = stringResource(Res.string.wizard_subject_unavailable_body),
				failedTitle = stringResource(Res.string.wizard_subject_failed_title),
				failedMessage = stringResource(Res.string.wizard_subject_failed_body),
				retryText = stringResource(Res.string.wizard_subject_retry),
				closeText = stringResource(Res.string.wizard_subject_close),
				onRetryClick = {},
				onTabSelected = onSubjectTabSelected,
				onDismissRequest = {},
				scrollEnabled = true,
				initialScrollOffset = if (isSubjectChartsStep) 540.dp else 0.dp,
				onChartsVisibilityChange = if (isSubjectStep) onSubjectChartsVisibilityChange else ({})
			)

		WizardStepId.Pensum ->
			PensumScreen(
				state = samplePensumState(),
				onRetryClick = {},
				showSelectionSheet = false,
				onSelectionSheetDismiss = {},
				onSubjectStatsClick = { onOpenSubjectDetail() },
				onSelectionApplied = { _, _ -> }
			)

		WizardStepId.Evaluations,
		WizardStepId.EvaluationSwipe -> {
			val focusedEvaluationId = if (state.currentStep.id == WizardStepId.EvaluationSwipe) {
				"evaluation_2"
			} else {
				null
			}

			EvaluationsScreen(
				state = sampleEvaluationsState(),
				onAddEvaluationClick = onOpenEvaluationForm,
				onEvaluationClick = { _, _ -> },
				onEvaluationEdit = {},
				onEvaluationDelete = {},
				onFilterCheckedChange = { _: EvaluationFilter, _: Boolean -> },
				onClearFiltersClick = {},
				onRetryClick = {},
				scrollEnabled = false,
				openActionsEvaluationId = focusedEvaluationId,
				focusEvaluationId = focusedEvaluationId,
				onFocusEvaluationBoundsChange = onEvaluationFocusTargetBoundsChange
			)
		}

		WizardStepId.EvaluationForm ->
			EvaluationScreen(
				state = sampleEvaluationFormState(),
				onAttemptChange = { _: EditableAttemptDescriptor? -> },
				onTypeChange = { _: EvaluationType? -> },
				onDateChange = { _: Long? -> },
				onGradeClick = { _: Double?, _: Double? -> },
				onMaxGradeClick = { _: Double? -> },
				onDoneClick = emptyEvaluationDoneHandler(),
				onRetryClick = {}
			)

		WizardStepId.About ->
			AboutScreen(
				state = sampleAboutState(),
				onCreativeCommonsClick = {},
				onXClick = {},
				onGithubClick = {},
				onKotlinClick = {},
				onComposeClick = {},
				onFirebaseClick = {},
				onKoinClick = {},
				onKtorClick = {},
				onDstClick = {},
				onTermsAndConditionsClick = {},
				onPrivacyPolicyClick = {},
				onSupportClick = {},
				onShareAppClick = {},
				onRateOnPlayStoreClick = {},
				onContactDeveloperClick = {},
				onReportBugClick = {}
			)
	}
}

private fun emptyAttemptSelectionHandler(): (
	attemptId: String,
	newGrade: Int?,
	newOutcome: AttemptOutcome?,
	isSelected: Boolean
) -> Unit = { _, _, _, _ -> }

private fun emptyEvaluationDoneHandler(): (
	attempt: EditableAttemptDescriptor?,
	type: EvaluationType?,
	scheduleMode: EvaluationScheduleMode,
	date: Long?,
	grade: Double?,
	maxGrade: Double?
) -> Unit = { _, _, _, _, _, _ -> }
