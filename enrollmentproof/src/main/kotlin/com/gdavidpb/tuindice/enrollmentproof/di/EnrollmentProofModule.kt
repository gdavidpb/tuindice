package com.gdavidpb.tuindice.enrollmentproof.di

import com.gdavidpb.tuindice.enrollmentproof.data.repository.enrollmentproof.EnrollmentProofDataRepository
import com.gdavidpb.tuindice.enrollmentproof.data.repository.enrollmentproof.LocalDataSource
import com.gdavidpb.tuindice.enrollmentproof.data.repository.enrollmentproof.RemoteDataSource
import com.gdavidpb.tuindice.enrollmentproof.data.repository.enrollmentproof.StorageDataSource
import com.gdavidpb.tuindice.enrollmentproof.data.repository.enrollmentproof.source.EnrollmentProofApiDataSource
import com.gdavidpb.tuindice.enrollmentproof.data.repository.enrollmentproof.source.InternalStorageDataSource
import com.gdavidpb.tuindice.enrollmentproof.data.repository.enrollmentproof.source.RoomDataSource
import com.gdavidpb.tuindice.enrollmentproof.domain.repository.EnrollmentProofRepository
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.FetchEnrollmentProofUseCase
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.exceptionhandler.FetchEnrollmentProofExceptionHandler
import com.gdavidpb.tuindice.enrollmentproof.presentation.action.FetchEnrollmentProofActionProcessor
import com.gdavidpb.tuindice.enrollmentproof.presentation.viewmodel.EnrollmentProofViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val enrollmentProofModule = module {
	/* View Models */

	viewModelOf(::EnrollmentProofViewModel)

	/* Action processors */

	factoryOf(::FetchEnrollmentProofActionProcessor)

	/* Use cases */

	factoryOf(::FetchEnrollmentProofUseCase)

	/* Repositories */

	factoryOf(::EnrollmentProofDataRepository) { bind<EnrollmentProofRepository>() }

	/* Data sources */

	factoryOf(::RoomDataSource) { bind<LocalDataSource>() }
	factoryOf(::EnrollmentProofApiDataSource) { bind<RemoteDataSource>() }
	factoryOf(::InternalStorageDataSource) { bind<StorageDataSource>() }

	/* Exception handlers */

	factoryOf(::FetchEnrollmentProofExceptionHandler)
}