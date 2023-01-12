package com.gdavidpb.tuindice.enrollmentproof.di

import com.gdavidpb.tuindice.base.BuildConfig
import com.gdavidpb.tuindice.base.utils.extensions.create
import com.gdavidpb.tuindice.enrollmentproof.data.api.ApiDataSource
import com.gdavidpb.tuindice.enrollmentproof.data.api.EnrollmentProofApi
import com.gdavidpb.tuindice.enrollmentproof.data.enrollmentproof.EnrollmentProofDataRepository
import com.gdavidpb.tuindice.enrollmentproof.data.enrollmentproof.source.LocalDataSource
import com.gdavidpb.tuindice.enrollmentproof.data.enrollmentproof.source.RemoteDataSource
import com.gdavidpb.tuindice.enrollmentproof.data.enrollmentproof.source.StorageDataSource
import com.gdavidpb.tuindice.enrollmentproof.data.room.RoomDataSource
import com.gdavidpb.tuindice.enrollmentproof.data.storage.InternalStorageDataSource
import com.gdavidpb.tuindice.enrollmentproof.domain.repository.EnrollmentProofRepository
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.GetEnrollmentProofUseCase
import com.gdavidpb.tuindice.enrollmentproof.presentation.viewmodel.EnrollmentProofViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.annotation.KoinReflectAPI
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@KoinReflectAPI
val enrollmentProofModule = module {
	/* View Models */

	viewModel<EnrollmentProofViewModel>()

	/* Use cases */

	factoryOf(::GetEnrollmentProofUseCase)

	/* Repositories */

	factoryOf(::EnrollmentProofDataRepository) { bind<EnrollmentProofRepository>() }

	/* Data sources */

	factoryOf(::RoomDataSource) { bind<LocalDataSource>() }
	factoryOf(::ApiDataSource) { bind<RemoteDataSource>() }
	factoryOf(::InternalStorageDataSource) { bind<StorageDataSource>() }

	/* Enrollment Proof Api */

	single {
		Retrofit.Builder()
			.baseUrl(BuildConfig.ENDPOINT_TU_INDICE_API)
			.addConverterFactory(GsonConverterFactory.create())
			.client(get())
			.build()
			.create<EnrollmentProofApi>()
	}
}