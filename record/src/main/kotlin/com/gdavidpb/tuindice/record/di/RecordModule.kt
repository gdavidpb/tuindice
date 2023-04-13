package com.gdavidpb.tuindice.record.di

import com.gdavidpb.tuindice.base.BuildConfig
import com.gdavidpb.tuindice.base.utils.extension.create
import com.gdavidpb.tuindice.record.data.api.ApiDataSource
import com.gdavidpb.tuindice.record.data.api.RecordApi
import com.gdavidpb.tuindice.record.data.api.parser.QuarterRemoveParser
import com.gdavidpb.tuindice.record.data.api.parser.SubjectUpdateParser
import com.gdavidpb.tuindice.record.data.preferences.PreferencesDataSource
import com.gdavidpb.tuindice.record.data.quarter.QuarterDataRepository
import com.gdavidpb.tuindice.record.data.quarter.source.LocalDataSource
import com.gdavidpb.tuindice.record.data.quarter.source.RemoteDataSource
import com.gdavidpb.tuindice.record.data.quarter.source.SettingsDataSource
import com.gdavidpb.tuindice.record.data.room.RoomDataSource
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import com.gdavidpb.tuindice.record.domain.usecase.GetQuartersUseCase
import com.gdavidpb.tuindice.record.domain.usecase.RemoveQuarterUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpdateSubjectUseCase
import com.gdavidpb.tuindice.record.domain.usecase.WithdrawSubjectUseCase
import com.gdavidpb.tuindice.record.domain.validator.UpdateSubjectParamsValidator
import com.gdavidpb.tuindice.record.presentation.viewmodel.RecordViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.annotation.KoinReflectAPI
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@KoinReflectAPI
val recordModule = module {
	/* View Models */

	viewModel<RecordViewModel>()

	/* Use cases */

	factoryOf(::GetQuartersUseCase)
	factoryOf(::RemoveQuarterUseCase)
	factoryOf(::UpdateSubjectUseCase)
	factoryOf(::WithdrawSubjectUseCase)

	/* Validators */

	factoryOf(::UpdateSubjectParamsValidator)

	/* Repositories */

	factoryOf(::QuarterDataRepository) { bind<QuarterRepository>() }

	/* Data sources */

	factoryOf(::RoomDataSource) { bind<LocalDataSource>() }
	factoryOf(::ApiDataSource) { bind<RemoteDataSource>() }
	factoryOf(::PreferencesDataSource) { bind<SettingsDataSource>() }

	/* Record Api */

	single {
		Retrofit.Builder()
			.baseUrl(BuildConfig.ENDPOINT_TU_INDICE_API)
			.addConverterFactory(GsonConverterFactory.create())
			.client(get())
			.build()
			.create<RecordApi>()
	}

	/* Parsers */

	factoryOf(::SubjectUpdateParser)
	factoryOf(::QuarterRemoveParser)
}