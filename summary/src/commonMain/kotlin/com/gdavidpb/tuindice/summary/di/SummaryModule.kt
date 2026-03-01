package com.gdavidpb.tuindice.summary.di

import com.gdavidpb.tuindice.summary.data.repository.user.LocalDataSource
import com.gdavidpb.tuindice.summary.data.repository.user.RemoteDataSource
import com.gdavidpb.tuindice.summary.data.repository.user.SettingsDataSource
import com.gdavidpb.tuindice.summary.data.repository.user.UserDataRepository
import com.gdavidpb.tuindice.summary.data.repository.user.source.PreferencesDataSource
import com.gdavidpb.tuindice.summary.data.repository.user.source.RoomDataSource
import com.gdavidpb.tuindice.summary.data.repository.user.source.SummaryApiDataSource
import com.gdavidpb.tuindice.summary.domain.repository.UserRepository
import com.gdavidpb.tuindice.summary.domain.usecase.GetUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RemoveProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.TakeProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UploadProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.GetUserExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.RemoveProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UploadProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.validator.UploadProfilePictureParamsValidator
import com.gdavidpb.tuindice.summary.presentation.action.ConfirmRemoveProfilePictureActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.LoadSummaryActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.OpenProfilePictureSettingsActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.PickProfilePictureActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.RemoveProfilePictureActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.TakeProfilePictureActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.UploadProfilePictureActionProcessor
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val summaryModule = module {
	/* View models */

	factoryOf(::SummaryViewModel)

	/* Action processor */

	factoryOf(::LoadSummaryActionProcessor)
	factoryOf(::TakeProfilePictureActionProcessor)
	factoryOf(::UploadProfilePictureActionProcessor)
	factoryOf(::ConfirmRemoveProfilePictureActionProcessor)
	factoryOf(::PickProfilePictureActionProcessor)
	factoryOf(::RemoveProfilePictureActionProcessor)
	factoryOf(::OpenProfilePictureSettingsActionProcessor)

	/* Use cases */

	factoryOf(::GetUserUseCase)
	factoryOf(::TakeProfilePictureUseCase)
	factoryOf(::UploadProfilePictureUseCase)
	factoryOf(::RemoveProfilePictureUseCase)

	/* Validators */

	factoryOf(::UploadProfilePictureParamsValidator)

	/* Repositories */

	factoryOf(::UserDataRepository) { bind<UserRepository>() }

	/* Data sources */

	factoryOf(::RoomDataSource) { bind<LocalDataSource>() }
	factoryOf(::SummaryApiDataSource) { bind<RemoteDataSource>() }
	factoryOf(::PreferencesDataSource) { bind<SettingsDataSource>() }

	/* Exception handlers */

	factoryOf(::GetUserExceptionHandler)
	factoryOf(::RemoveProfilePictureExceptionHandler)
	factoryOf(::UploadProfilePictureExceptionHandler)
}
