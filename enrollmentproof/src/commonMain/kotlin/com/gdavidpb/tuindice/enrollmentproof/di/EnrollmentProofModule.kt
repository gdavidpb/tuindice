package com.gdavidpb.tuindice.enrollmentproof.di

import com.gdavidpb.tuindice.enrollmentproof.data.repository.DatabaseDataSource
import com.gdavidpb.tuindice.enrollmentproof.data.repository.EnrollmentProofApiDataSource
import com.gdavidpb.tuindice.enrollmentproof.data.repository.EnrollmentProofDataRepository
import com.gdavidpb.tuindice.enrollmentproof.data.repository.StorageDataSource
import com.gdavidpb.tuindice.enrollmentproof.data.source.FileKitStorageDataSource
import com.gdavidpb.tuindice.enrollmentproof.data.source.KtorEnrollmentProofApiDataSource
import com.gdavidpb.tuindice.enrollmentproof.data.source.RoomDatabaseDataSource
import com.gdavidpb.tuindice.enrollmentproof.domain.repository.EnrollmentProofRepository
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.FetchEnrollmentProofUseCase
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.exceptionhandler.FetchEnrollmentProofExceptionHandler
import com.gdavidpb.tuindice.enrollmentproof.presentation.action.FetchEnrollmentProofActionProcessor
import com.gdavidpb.tuindice.enrollmentproof.presentation.resource.DefaultEnrollmentProofTextProvider
import com.gdavidpb.tuindice.enrollmentproof.presentation.resource.EnrollmentProofTextProvider
import com.gdavidpb.tuindice.enrollmentproof.presentation.viewmodel.EnrollmentProofViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val enrollmentProofModule = module {
	/* View models */

	viewModelOf(::EnrollmentProofViewModel)

	/* Action processors */

	factoryOf(::FetchEnrollmentProofActionProcessor)

	/* Use cases */

	factoryOf(::FetchEnrollmentProofUseCase)

	/* Repositories */

	factoryOf(::EnrollmentProofDataRepository) { bind<EnrollmentProofRepository>() }

	/* Data sources */

	factoryOf(::RoomDatabaseDataSource) { bind<DatabaseDataSource>() }
	factoryOf(::KtorEnrollmentProofApiDataSource) { bind<EnrollmentProofApiDataSource>() }
	factoryOf(::FileKitStorageDataSource) { bind<StorageDataSource>() }

	/* Exception handlers */

	factoryOf(::FetchEnrollmentProofExceptionHandler)

	/* Shared text resources */

	factoryOf(::DefaultEnrollmentProofTextProvider) { bind<EnrollmentProofTextProvider>() }
}
