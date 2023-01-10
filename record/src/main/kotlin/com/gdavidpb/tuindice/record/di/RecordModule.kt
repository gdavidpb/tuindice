package com.gdavidpb.tuindice.record.di

import com.gdavidpb.tuindice.base.BuildConfig
import com.gdavidpb.tuindice.base.utils.extensions.create
import com.gdavidpb.tuindice.record.data.api.EnrollmentProofApiDataSource
import com.gdavidpb.tuindice.record.data.api.QuarterApiDataSource
import com.gdavidpb.tuindice.record.data.api.RecordApi
import com.gdavidpb.tuindice.record.data.enrollmentproof.EnrollmentProofDataRepository
import com.gdavidpb.tuindice.record.data.quarter.QuarterDataRepository
import com.gdavidpb.tuindice.record.data.room.RoomDataSource
import com.gdavidpb.tuindice.record.data.storage.InternalStorageDataSource
import com.gdavidpb.tuindice.record.domain.repository.EnrollmentProofRepository
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import com.gdavidpb.tuindice.record.domain.usecase.GetEnrollmentProofUseCase
import com.gdavidpb.tuindice.record.domain.usecase.GetQuartersUseCase
import com.gdavidpb.tuindice.record.domain.usecase.RemoveQuarterUseCase
import com.gdavidpb.tuindice.record.presentation.viewmodel.RecordViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.annotation.KoinReflectAPI
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.gdavidpb.tuindice.record.data.enrollmentproof.source.LocalDataSource as EnrollmentProofLocalDataSource
import com.gdavidpb.tuindice.record.data.enrollmentproof.source.RemoteDataSource as EnrollmentProofRemoteDataSource
import com.gdavidpb.tuindice.record.data.quarter.source.LocalDataSource as QuarterLocalDataSource
import com.gdavidpb.tuindice.record.data.quarter.source.RemoteDataSource as QuarterRemoteDataSource

@KoinReflectAPI
val recordModule = module {
	/* View Models */

	viewModel<RecordViewModel>()

	/* Use cases */

	factoryOf(::GetQuartersUseCase)
	factoryOf(::GetEnrollmentProofUseCase)
	factoryOf(::RemoveQuarterUseCase)

	/* Repositories */

	factoryOf(::QuarterDataRepository) { bind<QuarterRepository>() }
	factoryOf(::EnrollmentProofDataRepository) { bind<EnrollmentProofRepository>() }

	/* Data sources */

	factoryOf(::RoomDataSource) { bind<QuarterLocalDataSource>() }
	factoryOf(::QuarterApiDataSource) { bind<QuarterRemoteDataSource>() }
	factoryOf(::InternalStorageDataSource) { bind<EnrollmentProofLocalDataSource>() }
	factoryOf(::EnrollmentProofApiDataSource) { bind<EnrollmentProofRemoteDataSource>() }

	/* Record Api */

	single {
		Retrofit.Builder()
			.baseUrl(BuildConfig.ENDPOINT_TU_INDICE_API)
			.addConverterFactory(GsonConverterFactory.create())
			.client(get())
			.build()
			.create<RecordApi>()
	}
}