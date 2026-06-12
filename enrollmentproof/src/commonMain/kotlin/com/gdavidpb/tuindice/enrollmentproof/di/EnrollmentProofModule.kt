package com.gdavidpb.tuindice.enrollmentproof.di

import com.gdavidpb.tuindice.enrollmentproof.data.repository.DatabaseDataRepository
import com.gdavidpb.tuindice.enrollmentproof.data.repository.EnrollmentProofApiDataRepository
import com.gdavidpb.tuindice.enrollmentproof.data.source.EnrollmentProofDataSource
import com.gdavidpb.tuindice.enrollmentproof.data.repository.StorageDataRepository
import com.gdavidpb.tuindice.enrollmentproof.data.source.FileKitStorageDataSource
import com.gdavidpb.tuindice.enrollmentproof.data.source.KtorEnrollmentProofApiDataSource
import com.gdavidpb.tuindice.enrollmentproof.data.source.RoomDatabaseDataSource
import com.gdavidpb.tuindice.enrollmentproof.domain.repository.EnrollmentProofRepository
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.FetchEnrollmentProofUseCase
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.exceptionhandler.FetchEnrollmentProofExceptionHandler
import com.gdavidpb.tuindice.enrollmentproof.presentation.machine.EnrollmentProofMachine
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

	factoryOf(::EnrollmentProofMachine)

	/* Use cases */

	factoryOf(::FetchEnrollmentProofUseCase)

	/* Repositories */

	factoryOf(::EnrollmentProofDataSource) { bind<EnrollmentProofRepository>() }

	/* Data sources */

	factoryOf(::RoomDatabaseDataSource) { bind<DatabaseDataRepository>() }
	factoryOf(::KtorEnrollmentProofApiDataSource) { bind<EnrollmentProofApiDataRepository>() }
	factoryOf(::FileKitStorageDataSource) { bind<StorageDataRepository>() }

	/* Exception handlers */

	factoryOf(::FetchEnrollmentProofExceptionHandler)

	/* Shared text resources */

	factoryOf(::DefaultEnrollmentProofTextProvider) { bind<EnrollmentProofTextProvider>() }
}
