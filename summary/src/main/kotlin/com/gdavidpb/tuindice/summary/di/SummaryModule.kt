package com.gdavidpb.tuindice.summary.di

import com.gdavidpb.tuindice.base.BuildConfig
import com.gdavidpb.tuindice.base.utils.extension.create
import com.gdavidpb.tuindice.summary.data.account.AccountDataRepository
import com.gdavidpb.tuindice.summary.data.account.source.LocalDataSource
import com.gdavidpb.tuindice.summary.data.account.source.RemoteDataSource
import com.gdavidpb.tuindice.summary.data.account.source.SettingsDataSource
import com.gdavidpb.tuindice.summary.data.api.ApiDataSource
import com.gdavidpb.tuindice.summary.data.api.SummaryApi
import com.gdavidpb.tuindice.summary.data.encoder.ImageEncoderDataSource
import com.gdavidpb.tuindice.summary.data.preferences.PreferencesDataSource
import com.gdavidpb.tuindice.summary.data.room.RoomDataSource
import com.gdavidpb.tuindice.summary.domain.repository.AccountRepository
import com.gdavidpb.tuindice.summary.domain.repository.EncoderRepository
import com.gdavidpb.tuindice.summary.domain.usecase.GetAccountUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RemoveProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.TakeProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UploadProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.GetAccountExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.RemoveProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UploadProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.validator.UploadProfilePictureParamsValidator
import com.gdavidpb.tuindice.summary.presentation.reducer.RemoveProfilePictureReducer
import com.gdavidpb.tuindice.summary.presentation.reducer.SummaryReducer
import com.gdavidpb.tuindice.summary.presentation.reducer.TakeProfilePictureReducer
import com.gdavidpb.tuindice.summary.presentation.reducer.UploadProfilePictureReducer
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val summaryModule = module {
	/* View Models */

	viewModelOf(::SummaryViewModel)

	/* Reducers */

	factoryOf(::SummaryReducer)
	factoryOf(::TakeProfilePictureReducer)
	factoryOf(::UploadProfilePictureReducer)
	factoryOf(::RemoveProfilePictureReducer)

	/* Use cases */

	factoryOf(::GetAccountUseCase)
	factoryOf(::TakeProfilePictureUseCase)
	factoryOf(::UploadProfilePictureUseCase)
	factoryOf(::RemoveProfilePictureUseCase)

	/* Validators */

	factoryOf(::UploadProfilePictureParamsValidator)

	/* Repositories */

	factoryOf(::AccountDataRepository) { bind<AccountRepository>() }
	factoryOf(::ImageEncoderDataSource) { bind<EncoderRepository>() }

	/* Data sources */

	factoryOf(::RoomDataSource) { bind<LocalDataSource>() }
	factoryOf(::ApiDataSource) { bind<RemoteDataSource>() }
	factoryOf(::PreferencesDataSource) { bind<SettingsDataSource>() }

	/* Summary Api */

	single {
		Retrofit.Builder()
			.baseUrl(BuildConfig.ENDPOINT_TU_INDICE_API)
			.addConverterFactory(GsonConverterFactory.create())
			.client(get())
			.build()
			.create<SummaryApi>()
	}

	/* Exception handlers */

	factoryOf(::GetAccountExceptionHandler)
	factoryOf(::RemoveProfilePictureExceptionHandler)
	factoryOf(::UploadProfilePictureExceptionHandler)
}