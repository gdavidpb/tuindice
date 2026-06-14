package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.domain.usecase.GetUpdateInfoUseCase
import com.gdavidpb.tuindice.domain.usecase.RequestReviewUseCase
import com.gdavidpb.tuindice.domain.usecase.ScheduleSyncUseCase
import com.gdavidpb.tuindice.domain.usecase.SetLastMainSectionUseCase
import com.gdavidpb.tuindice.domain.usecase.StartUpUseCase
import com.gdavidpb.tuindice.domain.usecase.exceptionhandler.StartUpExceptionHandler
import com.gdavidpb.tuindice.presentation.machine.BrowserMachine
import com.gdavidpb.tuindice.presentation.machine.MainMachine
import com.gdavidpb.tuindice.presentation.viewmodel.BrowserViewModel
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val mainModule = module {
	/* View models */

	viewModelOf(::MainViewModel)

	factoryOf(::MainMachine)
	factoryOf(::BrowserMachine)
	viewModelOf(::BrowserViewModel)

	/* Use cases */

	factoryOf(::StartUpUseCase)
	factoryOf(::RequestReviewUseCase)
	factoryOf(::ScheduleSyncUseCase)
	factoryOf(::SetLastMainSectionUseCase)
	factoryOf(::GetUpdateInfoUseCase)

	/* Exception handlers */

	factoryOf(::StartUpExceptionHandler)
}
