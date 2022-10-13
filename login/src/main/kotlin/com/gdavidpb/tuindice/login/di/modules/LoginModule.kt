package com.gdavidpb.tuindice.login.di.modules

import org.koin.core.annotation.KoinReflectAPI
import org.koin.dsl.module
import com.gdavidpb.tuindice.login.domain.usecase.ReSignInUseCase
import com.gdavidpb.tuindice.login.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.login.domain.usecase.StartUpUseCase
import com.gdavidpb.tuindice.login.presentation.viewmodel.SignInViewModel
import com.gdavidpb.tuindice.login.presentation.viewmodel.SplashViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.factoryOf

@KoinReflectAPI
val loginModule = module {
	/* View Models */

	viewModel<SplashViewModel>()
	viewModel<SignInViewModel>()

	/* Use cases */

	factoryOf(::SignInUseCase)
	factoryOf(::ReSignInUseCase)
	factoryOf(::StartUpUseCase)
}