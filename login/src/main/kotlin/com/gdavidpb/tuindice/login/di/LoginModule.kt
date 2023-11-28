package com.gdavidpb.tuindice.login.di

import com.gdavidpb.tuindice.base.BuildConfig
import com.gdavidpb.tuindice.base.utils.extension.create
import com.gdavidpb.tuindice.login.data.repository.login.source.SignInApiDataSource
import com.gdavidpb.tuindice.login.data.repository.login.SignInApi
import com.gdavidpb.tuindice.login.data.repository.login.LoginDataRepository
import com.gdavidpb.tuindice.login.data.repository.login.RemoteDataSource
import com.gdavidpb.tuindice.login.domain.repository.LoginRepository
import com.gdavidpb.tuindice.login.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.login.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.login.domain.usecase.UpdatePasswordUseCase
import com.gdavidpb.tuindice.login.domain.usecase.exceptionhandler.SignInExceptionHandler
import com.gdavidpb.tuindice.login.domain.usecase.exceptionhandler.UpdatePasswordExceptionHandler
import com.gdavidpb.tuindice.login.domain.usecase.validator.SignInParamsValidator
import com.gdavidpb.tuindice.login.domain.usecase.validator.UpdatePasswordParamsValidator
import com.gdavidpb.tuindice.login.presentation.action.OpenPrivacyPolicyActionProcessor
import com.gdavidpb.tuindice.login.presentation.action.OpenTermsAndConditionsActionProcessor
import com.gdavidpb.tuindice.login.presentation.action.SetPasswordActionProcessor
import com.gdavidpb.tuindice.login.presentation.action.SetUpdatePasswordActionProcessor
import com.gdavidpb.tuindice.login.presentation.action.SetUsbIdActionProcessor
import com.gdavidpb.tuindice.login.presentation.action.SignInActionProcessor
import com.gdavidpb.tuindice.login.presentation.action.SignOutActionProcessor
import com.gdavidpb.tuindice.login.presentation.action.UpdatePasswordActionProcessor
import com.gdavidpb.tuindice.login.presentation.viewmodel.SignInViewModel
import com.gdavidpb.tuindice.login.presentation.viewmodel.SignOutViewModel
import com.gdavidpb.tuindice.login.presentation.viewmodel.UpdatePasswordViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import retrofit2.Retrofit

val loginModule = module {
	/* View Models */

	viewModelOf(::SignInViewModel)
	viewModelOf(::SignOutViewModel)
	viewModelOf(::UpdatePasswordViewModel)

	/* Action Processors */

	factoryOf(::SignInActionProcessor)
	factoryOf(::SetUsbIdActionProcessor)
	factoryOf(::SetPasswordActionProcessor)
	factoryOf(::OpenTermsAndConditionsActionProcessor)
	factoryOf(::OpenPrivacyPolicyActionProcessor)
	factoryOf(::SignOutActionProcessor)
	factoryOf(::SetUpdatePasswordActionProcessor)
	factoryOf(::UpdatePasswordActionProcessor)

	/* Use cases */

	factoryOf(::SignInUseCase)
	factoryOf(::SignOutUseCase)
	factoryOf(::UpdatePasswordUseCase)

	/* Validators */

	factoryOf(::SignInParamsValidator)
	factoryOf(::UpdatePasswordParamsValidator)

	/* Repositories */

	factoryOf(::LoginDataRepository) { bind<LoginRepository>() }

	/* Data sources */

	factoryOf(::SignInApiDataSource) { bind<RemoteDataSource>() }

	/* SignIn Api */

	single {
		Retrofit.Builder()
			.baseUrl(BuildConfig.ENDPOINT_TU_INDICE_API)
			.addConverterFactory(get())
			.client(get())
			.build()
			.create<SignInApi>()
	}

	/* Exception handlers */

	factoryOf(::UpdatePasswordExceptionHandler)
	factoryOf(::SignInExceptionHandler)
}