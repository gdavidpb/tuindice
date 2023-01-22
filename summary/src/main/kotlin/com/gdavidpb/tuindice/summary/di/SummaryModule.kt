package com.gdavidpb.tuindice.summary.di

import com.gdavidpb.tuindice.base.BuildConfig
import com.gdavidpb.tuindice.base.utils.extensions.create
import com.gdavidpb.tuindice.summary.data.account.AccountDataRepository
import com.gdavidpb.tuindice.summary.data.account.source.LocalDataSource
import com.gdavidpb.tuindice.summary.data.account.source.RemoteDataSource
import com.gdavidpb.tuindice.summary.data.api.ApiDataSource
import com.gdavidpb.tuindice.summary.data.api.SummaryApi
import com.gdavidpb.tuindice.summary.data.encoder.ImageEncoderDataSource
import com.gdavidpb.tuindice.summary.data.room.RoomDataSource
import com.gdavidpb.tuindice.summary.domain.repository.AccountRepository
import com.gdavidpb.tuindice.summary.domain.repository.EncoderRepository
import com.gdavidpb.tuindice.summary.domain.usecase.GetAccountUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RemoveProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UploadProfilePictureUseCase
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.summary.ui.manager.ProfilePictureManager
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.annotation.KoinReflectAPI
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@KoinReflectAPI
val summaryModule = module {
	/* View Models */

	viewModel<SummaryViewModel>()

	/* Use cases */

	factoryOf(::GetAccountUseCase)
	factoryOf(::UploadProfilePictureUseCase)
	factoryOf(::RemoveProfilePictureUseCase)

	/* Repositories */

	factoryOf(::AccountDataRepository) { bind<AccountRepository>() }
	factoryOf(::ImageEncoderDataSource) { bind<EncoderRepository>() }

	/* Data sources */

	factoryOf(::RoomDataSource) { bind<LocalDataSource>() }
	factoryOf(::ApiDataSource) { bind<RemoteDataSource>() }

	/* Summary Api */

	single {
		Retrofit.Builder()
			.baseUrl(BuildConfig.ENDPOINT_TU_INDICE_API)
			.addConverterFactory(GsonConverterFactory.create())
			.client(get())
			.build()
			.create<SummaryApi>()
	}

	/* Managers */

	factoryOf(::ProfilePictureManager)
}