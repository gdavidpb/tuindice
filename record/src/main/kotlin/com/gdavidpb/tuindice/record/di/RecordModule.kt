package com.gdavidpb.tuindice.record.di

import com.gdavidpb.tuindice.base.BuildConfig
import com.gdavidpb.tuindice.base.utils.extension.create
import com.gdavidpb.tuindice.record.data.repository.quarter.CacheDataSource
import com.gdavidpb.tuindice.record.data.repository.quarter.LocalDataSource
import com.gdavidpb.tuindice.record.data.repository.quarter.QuarterDataRepository
import com.gdavidpb.tuindice.record.data.repository.quarter.RecordApi
import com.gdavidpb.tuindice.record.data.repository.quarter.RemoteDataSource
import com.gdavidpb.tuindice.record.data.repository.quarter.SettingsDataSource
import com.gdavidpb.tuindice.record.data.repository.quarter.source.MemoryDataSource
import com.gdavidpb.tuindice.record.data.repository.quarter.source.PreferencesDataSource
import com.gdavidpb.tuindice.record.data.repository.quarter.source.RecordApiDataSource
import com.gdavidpb.tuindice.record.data.repository.quarter.source.RoomDataSource
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import com.gdavidpb.tuindice.record.domain.usecase.GetQuartersUseCase
import com.gdavidpb.tuindice.record.domain.usecase.RemoveQuarterUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpdateSubjectUseCase
import com.gdavidpb.tuindice.record.domain.usecase.WithdrawSubjectUseCase
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.GetQuartersExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.UpdateSubjectExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.validator.UpdateSubjectParamsValidator
import com.gdavidpb.tuindice.record.presentation.action.LoadQuartersActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.UpdateSubjectActionProcessor
import com.gdavidpb.tuindice.record.presentation.viewmodel.RecordViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import retrofit2.Retrofit

val recordModule = module {
	/* View Models */

	viewModelOf(::RecordViewModel)

	/* Action processor */

	factoryOf(::LoadQuartersActionProcessor)
	factoryOf(::UpdateSubjectActionProcessor)

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
	factoryOf(::RecordApiDataSource) { bind<RemoteDataSource>() }
	factoryOf(::MemoryDataSource) { bind<CacheDataSource>() }
	factoryOf(::PreferencesDataSource) { bind<SettingsDataSource>() }

	/* Record Api */

	single {
		Retrofit.Builder()
			.baseUrl(BuildConfig.ENDPOINT_TU_INDICE_API)
			.addConverterFactory(get())
			.client(get())
			.build()
			.create<RecordApi>()
	}

	/* Exception handlers */

	factoryOf(::GetQuartersExceptionHandler)
	factoryOf(::UpdateSubjectExceptionHandler)
}