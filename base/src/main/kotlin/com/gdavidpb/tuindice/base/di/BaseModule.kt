package com.gdavidpb.tuindice.base.di

import com.gdavidpb.tuindice.base.domain.usecase.*
import com.gdavidpb.tuindice.base.presentation.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.annotation.KoinReflectAPI
import org.koin.dsl.module
import org.koin.core.module.dsl.factoryOf

@KoinReflectAPI
val baseModule = module {

	/* View Models */

	viewModel<MainViewModel>()

	/* Use cases */

	factoryOf(::SignOutUseCase)
	factoryOf(::SyncUseCase)
	factoryOf(::SetLastScreenUseCase)
	factoryOf(::RequestReviewUseCase)
	factoryOf(::GetUpdateInfoUseCase)
}