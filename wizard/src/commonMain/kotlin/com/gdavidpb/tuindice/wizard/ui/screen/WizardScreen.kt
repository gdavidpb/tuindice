package com.gdavidpb.tuindice.wizard.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.about.ui.screen.AboutScreen
import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
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
import com.gdavidpb.tuindice.wizard.ui.WizardUiTags
import com.gdavidpb.tuindice.wizard.ui.model.sampleAboutState
import com.gdavidpb.tuindice.wizard.ui.model.sampleCreateSyntheticTermState
import com.gdavidpb.tuindice.wizard.ui.model.sampleEvaluationFormState
import com.gdavidpb.tuindice.wizard.ui.model.sampleEvaluationsState
import com.gdavidpb.tuindice.wizard.ui.model.samplePensumState
import com.gdavidpb.tuindice.wizard.ui.model.sampleRecordState
import com.gdavidpb.tuindice.wizard.ui.model.sampleSubjectDetailState
import com.gdavidpb.tuindice.wizard.ui.model.sampleSummaryState
import com.gdavidpb.tuindice.wizard.ui.view.WizardFocusOverlay
import com.gdavidpb.tuindice.wizard.ui.view.WizardGuideBar
import com.gdavidpb.tuindice.wizard.ui.view.WizardStepContent
import com.gdavidpb.tuindice.wizard.ui.view.WizardWelcomeView
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
fun WizardScreen(
	state: Wizard.State.Content,
	onBack: () -> Unit,
	onSkip: () -> Unit,
	onNext: () -> Unit,
	onFinish: () -> Unit,
	onOpenSubjectDetail: () -> Unit,
	onOpenEvaluationForm: () -> Unit,
	onSubjectTabSelected: (SubjectSegmentTab) -> Unit,
	onSelectedTermChange: (String) -> Unit,
	onSubjectChartsVisibilityChange: (Boolean) -> Unit,
	modifier: Modifier = Modifier
) {
		if (state.isWelcomeStep) {
			Box(
				modifier = modifier
					.fillMaxSize()
					.background(MaterialTheme.colorScheme.background)
					.testTag(WizardUiTags.Screen)
			) {
				WizardWelcomeView(
				onStart = onNext,
				onSkip = onSkip
			)
		}
		return
	}

	val focusTargetBounds = remember {
		mutableStateOf<Rect?>(null)
	}

	LaunchedEffect(state.currentStep.id) {
		focusTargetBounds.value = null
	}

	Column(
		modifier = modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
			.testTag(WizardUiTags.Screen)
	) {
		Box(
			modifier = Modifier
				.weight(1f)
				.fillMaxSize()
		) {
			WizardStepContent(
				state = state,
				onOpenSubjectDetail = onOpenSubjectDetail,
				onOpenEvaluationForm = onOpenEvaluationForm,
				onSubjectTabSelected = onSubjectTabSelected,
				onSelectedTermChange = onSelectedTermChange,
				onSubjectChartsVisibilityChange = onSubjectChartsVisibilityChange,
				onEvaluationFocusTargetBoundsChange = { bounds ->
					focusTargetBounds.value = bounds
				}
			)
			WizardFocusOverlay(
				stepId = state.currentStep.id,
				targetBoundsInRoot = focusTargetBounds.value,
				modifier = Modifier.fillMaxSize()
			)
		}

		WizardGuideBar(
			state = state,
			onBack = onBack,
			onSkip = onSkip,
			onNext = onNext,
			onFinish = onFinish
		)
	}
}
