package com.gdavidpb.tuindice.login.di.modules

import com.gdavidpb.tuindice.base.BuildConfig
import com.gdavidpb.tuindice.base.utils.extensions.create
import com.gdavidpb.tuindice.login.data.source.api.ApiDataSource
import com.gdavidpb.tuindice.login.data.source.api.SignInApi
import com.gdavidpb.tuindice.login.data.source.room.RoomDataSource
import com.gdavidpb.tuindice.login.domain.repository.LocalRepository
import com.gdavidpb.tuindice.login.domain.repository.RemoteRepository
import com.gdavidpb.tuindice.login.domain.usecase.ReSignInUseCase
import com.gdavidpb.tuindice.login.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.login.domain.usecase.StartUpUseCase
import com.gdavidpb.tuindice.login.presentation.viewmodel.SignInViewModel
import com.gdavidpb.tuindice.login.presentation.viewmodel.SplashViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.annotation.KoinReflectAPI
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@KoinReflectAPI
val loginModule = module {
	/* View Models */

	viewModel<SplashViewModel>()
	viewModel<SignInViewModel>()

	/* Use cases */

	factoryOf(::SignInUseCase)
	factoryOf(::ReSignInUseCase)
	factoryOf(::StartUpUseCase)

	/* Repositories */

	factoryOf(::ApiDataSource) { bind<RemoteRepository>() }
	factoryOf(::RoomDataSource) { bind<LocalRepository>() }

	/* Api */

	single {
		Retrofit.Builder()
			.baseUrl(BuildConfig.ENDPOINT_TU_INDICE_API)
			.addConverterFactory(GsonConverterFactory.create())
			.client(get())
			.build()
			.create<SignInApi>()
	}
}