package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.domain.usecase.GetUpdateInfoUseCase
import com.gdavidpb.tuindice.domain.usecase.RequestReviewUseCase
import com.gdavidpb.tuindice.domain.usecase.SetLastDestinationUseCase
import com.gdavidpb.tuindice.domain.usecase.StartUpUseCase
import com.gdavidpb.tuindice.domain.usecase.exceptionhandler.StartUpExceptionHandler
import com.gdavidpb.tuindice.presentation.action.browser.NavigateToActionProcessor
import com.gdavidpb.tuindice.presentation.action.browser.OpenExternalResourceActionProcessor
import com.gdavidpb.tuindice.presentation.action.browser.SetLoadingActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.*
import com.gdavidpb.tuindice.presentation.viewmodel.BrowserViewModel
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val mainModule = module {
	/* View models */

	viewModelOf(::MainViewModel)
	viewModelOf(::BrowserViewModel)

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

	factoryOf(::StartUpUseCase)
	factoryOf(::RequestReviewUseCase)
	factoryOf(::SetLastDestinationUseCase)
	factoryOf(::GetUpdateInfoUseCase)

	/* Exception handlers */

	factoryOf(::StartUpExceptionHandler)
}
