package com.gdavidpb.tuindice.wizard.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.wizard.presentation.contract.Wizard
import com.gdavidpb.tuindice.wizard.presentation.model.WizardTopBarActionBus
import com.gdavidpb.tuindice.wizard.presentation.viewmodel.WizardViewModel
import com.gdavidpb.tuindice.wizard.ui.screen.WizardScreen

@Composable
fun WizardRoute(
	topBarActionBus: WizardTopBarActionBus,
	onFinishWizard: () -> Unit,
	onTopBarViewModeChangeAvailable: (((RecordViewMode) -> Unit)?) -> Unit,
	onViewStateChanged: (ViewState) -> Unit,
	viewModel: WizardViewModel
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()

	DisposableEffect(viewModel) {
		onTopBarViewModeChangeAvailable(viewModel::setRecordViewModeAction)

		onDispose {
			onTopBarViewModeChangeAvailable(null)
		}
	}

	LaunchedEffect(viewState) {
		onViewStateChanged(viewState)
	}

	LaunchedEffect(topBarActionBus, viewModel) {
		topBarActionBus.actions.collect { action ->
			viewModel.consumeTopBarAction(action)
		}
	}

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			Wizard.Effect.FinishWizard ->
				onFinishWizard()
		}
	}

	WizardScreen(
		state = viewState as Wizard.State.Content,
		onBack = viewModel::backAction,
		onSkip = viewModel::dismissAction,
		onNext = viewModel::advanceAction,
		onFinish = viewModel::finishAction,
		onOpenSubjectDetail = viewModel::openSubjectDetailAction,
		onOpenEvaluationForm = viewModel::openEvaluationFormAction,
		onSubjectTabSelected = viewModel::selectSubjectTabAction,
		onSelectedTermChange = viewModel::selectTermAction,
		onSubjectChartsVisibilityChange = viewModel::setSubjectChartsVisibleAction
	)
}
