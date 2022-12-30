package com.gdavidpb.tuindice.summary.di.modules

import com.gdavidpb.tuindice.base.BuildConfig
import com.gdavidpb.tuindice.base.utils.extensions.create
import com.gdavidpb.tuindice.summary.data.source.api.SummaryAPI
import com.gdavidpb.tuindice.summary.domain.usecase.*
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.summary.data.source.api.APIDataSource
import com.gdavidpb.tuindice.summary.data.source.encoder.ImageEncoderDataSource
import com.gdavidpb.tuindice.summary.data.source.storage.InternalStorageDataSource
import com.gdavidpb.tuindice.summary.domain.repository.SummaryRepository
import  com.gdavidpb.tuindice.summary.data.SummaryDataRepository
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

	factoryOf(::GetProfileUseCase)
	factoryOf(::GetProfilePictureUseCase)
	factoryOf(::UploadProfilePictureUseCase)
	factoryOf(::RemoveProfilePictureUseCase)

	/* Repositories */

	factoryOf(::SummaryDataRepository) { bind<SummaryRepository>() }

	/* Data sources */

	factoryOf(::APIDataSource)
	factoryOf(::ImageEncoderDataSource)
	factoryOf(::InternalStorageDataSource)

	/* Summary API */

	single {
		Retrofit.Builder()
			.baseUrl(BuildConfig.ENDPOINT_TU_INDICE_API)
			.addConverterFactory(GsonConverterFactory.create())
			.client(get())
			.build()
			.create<SummaryAPI>()
	}
}