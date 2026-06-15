package com.gdavidpb.tuindice.summary.di

import com.gdavidpb.tuindice.summary.data.repository.user.LocalDataRepository
import com.gdavidpb.tuindice.summary.data.repository.user.PictureEncoderDataRepository
import com.gdavidpb.tuindice.summary.data.repository.user.ProfilePictureInputDataRepository
import com.gdavidpb.tuindice.summary.data.repository.user.RemoteDataRepository
import com.gdavidpb.tuindice.summary.data.repository.user.SettingsDataRepository
import com.gdavidpb.tuindice.summary.data.source.UserDataSource
import com.gdavidpb.tuindice.summary.data.source.FileKitSkiaPictureEncoderDataSource
import com.gdavidpb.tuindice.summary.data.source.LocalSettingsDataSource
import com.gdavidpb.tuindice.summary.data.source.RoomDataSource
import com.gdavidpb.tuindice.summary.data.source.SummaryApiDataSource
import com.gdavidpb.tuindice.summary.domain.repository.UserRepository
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RemoveProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UpdateUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UploadProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.RemoveProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UpdateUserExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UploadProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.presentation.machine.SummaryMachine
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val summaryModule = module {
	/* View models */

	viewModelOf(::SummaryViewModel)

	/* State machines */

	factoryOf(::SummaryMachine)

	/* Use cases */

	factoryOf(::ObserveUserUseCase)
	factoryOf(::UpdateUserUseCase)
	factoryOf(::UploadProfilePictureUseCase)
	factoryOf(::RemoveProfilePictureUseCase)

	/* Repositories */

	factoryOf(::UserDataSource) { bind<UserRepository>() }

	/* Data sources */

	factoryOf(::RoomDataSource) { bind<LocalDataRepository>() }
	factoryOf(::SummaryApiDataSource) { bind<RemoteDataRepository>() }
	singleOf(::LocalSettingsDataSource) { bind<SettingsDataRepository>() }
	// Platform-specific binding for ProfilePictureInputDataRepository comes from platform modules.
	factoryOf(::FileKitSkiaPictureEncoderDataSource) { bind<PictureEncoderDataRepository>() }

	/* Exception handlers */

	factoryOf(::UpdateUserExceptionHandler)
	factoryOf(::RemoveProfilePictureExceptionHandler)
	factoryOf(::UploadProfilePictureExceptionHandler)
}
