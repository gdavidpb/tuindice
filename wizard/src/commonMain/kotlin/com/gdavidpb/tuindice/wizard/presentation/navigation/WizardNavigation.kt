package com.gdavidpb.tuindice.wizard.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.wizard.presentation.model.WizardTopBarActionBus
import com.gdavidpb.tuindice.wizard.presentation.route.WizardRoute
import com.gdavidpb.tuindice.wizard.presentation.viewmodel.WizardViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.wizardNavigation(
	onFinishWizard: () -> Unit,
	onTopBarViewModeChangeAvailable: (((RecordViewMode) -> Unit)?) -> Unit,
	onViewStateChanged: (ViewState) -> Unit
) {
	navigation<WizardDestination.NavGraph>(startDestination = WizardDestination.Wizard) {
		composable<WizardDestination.Wizard> { backStackEntry ->
			val viewModel = koinViewModel<WizardViewModel>(viewModelStoreOwner = backStackEntry)
			val topBarActionBus = koinInject<WizardTopBarActionBus>()

			WizardRoute(
				topBarActionBus = topBarActionBus,
				onFinishWizard = onFinishWizard,
				onTopBarViewModeChangeAvailable = onTopBarViewModeChangeAvailable,
				onViewStateChanged = onViewStateChanged,
				viewModel = viewModel
			)
		}
	}
}
