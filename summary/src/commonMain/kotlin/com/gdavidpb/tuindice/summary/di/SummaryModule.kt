package com.gdavidpb.tuindice.summary.di

import com.gdavidpb.tuindice.summary.data.source.user.LocalDataSource
import com.gdavidpb.tuindice.summary.data.source.user.PictureEncoderDataSource
import com.gdavidpb.tuindice.summary.data.source.user.RemoteDataSource
import com.gdavidpb.tuindice.summary.data.source.user.SettingsDataSource
import com.gdavidpb.tuindice.summary.data.repository.user.UserDataRepository
import com.gdavidpb.tuindice.summary.data.source.user.FileKitSkiaPictureEncoderDataSource
import com.gdavidpb.tuindice.summary.data.source.user.LocalSettingsDataSource
import com.gdavidpb.tuindice.summary.data.source.user.RoomDataSource
import com.gdavidpb.tuindice.summary.data.source.user.SummaryApiDataSource
import com.gdavidpb.tuindice.summary.domain.repository.UserRepository
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RemoveProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UpdateUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UploadProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.RemoveProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UpdateUserExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UploadProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.validator.UploadProfilePictureParamsValidator
import com.gdavidpb.tuindice.summary.presentation.action.ObserveSummaryActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.ConfirmRemoveProfilePictureActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.OpenProfilePictureSettingsActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.PickProfilePictureActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.RefreshSummaryActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.RemoveProfilePictureActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.TakeProfilePictureActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.UploadProfilePictureActionProcessor
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.russhwolf.settings.Settings
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val summaryModule = module {
	/* View models */

	viewModelOf(::SummaryViewModel)

	/* Action processor */

	factoryOf(::ObserveSummaryActionProcessor)
	factoryOf(::RefreshSummaryActionProcessor)
	factoryOf(::TakeProfilePictureActionProcessor)
	factoryOf(::UploadProfilePictureActionProcessor)
	factoryOf(::ConfirmRemoveProfilePictureActionProcessor)
	factoryOf(::PickProfilePictureActionProcessor)
	factoryOf(::RemoveProfilePictureActionProcessor)
	factoryOf(::OpenProfilePictureSettingsActionProcessor)

	/* Use cases */

	factoryOf(::ObserveUserUseCase)
	factoryOf(::UpdateUserUseCase)
	factoryOf(::UploadProfilePictureUseCase)
	factoryOf(::RemoveProfilePictureUseCase)

	/* Validators */

	factoryOf(::UploadProfilePictureParamsValidator)

	/* Repositories */

	factoryOf(::UserDataRepository) { bind<UserRepository>() }

	/* Data sources */

	factoryOf(::RoomDataSource) { bind<LocalDataSource>() }
	factoryOf(::SummaryApiDataSource)
	factory<RemoteDataSource> {
		get<SummaryApiDataSource>()
	}
	single<SettingsDataSource> {
		LocalSettingsDataSource(get<Settings>())
	}
	factoryOf(::FileKitSkiaPictureEncoderDataSource) { bind<PictureEncoderDataSource>() }

	/* Exception handlers */

	factoryOf(::UpdateUserExceptionHandler)
	factoryOf(::RemoveProfilePictureExceptionHandler)
	factoryOf(::UploadProfilePictureExceptionHandler)
}
