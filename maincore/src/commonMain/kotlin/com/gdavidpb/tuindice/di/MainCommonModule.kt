package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.domain.usecase.GetUpdateInfoUseCase
import com.gdavidpb.tuindice.domain.usecase.RequestReviewUseCase
import com.gdavidpb.tuindice.domain.usecase.SetLastDestinationUseCase
import com.gdavidpb.tuindice.domain.usecase.StartUpUseCase
import com.gdavidpb.tuindice.domain.usecase.exceptionhandler.StartUpExceptionHandler
import com.gdavidpb.tuindice.presentation.action.browser.NavigateToActionProcessor
import com.gdavidpb.tuindice.presentation.action.browser.OpenExternalResourceActionProcessor
import com.gdavidpb.tuindice.presentation.action.browser.SetLoadingActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.RequestReviewActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.RequestUpdateActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.SetLastDestinationActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.StartUpActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.UpdateStateActionProcessor
import com.gdavidpb.tuindice.presentation.viewmodel.BrowserViewModel
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val mainCommonModule = module {
	/* View models */

	factoryOf(::MainViewModel)
	factoryOf(::BrowserViewModel)

	/* Action processor */

	factoryOf(::UpdateStateActionProcessor)
	factoryOf(::StartUpActionProcessor)
	factoryOf(::RequestReviewActionProcessor)
	factoryOf(::RequestUpdateActionProcessor)
	factoryOf(::SetLastDestinationActionProcessor)

	factoryOf(::NavigateToActionProcessor)
	factoryOf(::SetLoadingActionProcessor)
	factoryOf(::OpenExternalResourceActionProcessor)

	/* Use cases */

	factory {
		StartUpUseCase(
			sessionRepository = get(),
			settingsRepository = get(),
			configRepository = get(),
			exceptionHandler = get<StartUpExceptionHandler>()
		)
	}
	factoryOf(::RequestReviewUseCase)
	factoryOf(::SetLastDestinationUseCase)
	factoryOf(::GetUpdateInfoUseCase)

	/* Exception handlers */

	factoryOf(::StartUpExceptionHandler)
}
