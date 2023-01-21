package com.gdavidpb.tuindice.base.di

import com.gdavidpb.tuindice.base.domain.usecase.GetUpdateInfoUseCase
import com.gdavidpb.tuindice.base.domain.usecase.RequestReviewUseCase
import com.gdavidpb.tuindice.base.domain.usecase.SetLastScreenUseCase
import com.gdavidpb.tuindice.base.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.base.presentation.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.annotation.KoinReflectAPI
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

@KoinReflectAPI
val baseModule = module {

	/* View Models */

	viewModel<MainViewModel>()

	/* Use cases */

	factoryOf(::SignOutUseCase)
	factoryOf(::SetLastScreenUseCase)
	factoryOf(::RequestReviewUseCase)
	factoryOf(::GetUpdateInfoUseCase)
}