package com.gdavidpb.tuindice.login.di

import com.gdavidpb.tuindice.base.BuildConfig
import com.gdavidpb.tuindice.base.utils.extension.create
import com.gdavidpb.tuindice.login.data.api.ApiDataSource
import com.gdavidpb.tuindice.login.data.api.SignInApi
import com.gdavidpb.tuindice.login.data.login.LoginDataRepository
import com.gdavidpb.tuindice.login.data.login.source.RemoteDataSource
import com.gdavidpb.tuindice.login.domain.repository.LoginRepository
import com.gdavidpb.tuindice.login.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.login.domain.usecase.UpdatePasswordUseCase
import com.gdavidpb.tuindice.login.domain.usecase.exceptionhandler.SignInExceptionHandler
import com.gdavidpb.tuindice.login.domain.usecase.exceptionhandler.UpdatePasswordExceptionHandler
import com.gdavidpb.tuindice.login.domain.usecase.validator.SignInParamsValidator
import com.gdavidpb.tuindice.login.domain.usecase.validator.UpdatePasswordParamsValidator
import com.gdavidpb.tuindice.login.presentation.reducer.SignInReducer
import com.gdavidpb.tuindice.login.presentation.reducer.UpdatePasswordReducer
import com.gdavidpb.tuindice.login.presentation.viewmodel.SignInViewModel
import com.gdavidpb.tuindice.login.presentation.viewmodel.UpdatePasswordViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val loginModule = module {
	/* View Models */

	viewModelOf(::SignInViewModel)
	viewModelOf(::UpdatePasswordViewModel)

	/* Reducers */

	factoryOf(::SignInReducer)
	factoryOf(::UpdatePasswordReducer)

	/* Use cases */

	factoryOf(::SignInUseCase)
	factoryOf(::UpdatePasswordUseCase)

	/* Validators */

	factoryOf(::SignInParamsValidator)
	factoryOf(::UpdatePasswordParamsValidator)

	/* Repositories */

	factoryOf(::LoginDataRepository) { bind<LoginRepository>() }

	/* Data sources */

	factoryOf(::ApiDataSource) { bind<RemoteDataSource>() }

	/* SignIn Api */

	single {
		Retrofit.Builder()
			.baseUrl(BuildConfig.ENDPOINT_TU_INDICE_API)
			.addConverterFactory(GsonConverterFactory.create())
			.client(get())
			.build()
			.create<SignInApi>()
	}

	/* Exception handlers */

	factoryOf(::UpdatePasswordExceptionHandler)
	factoryOf(::SignInExceptionHandler)
}