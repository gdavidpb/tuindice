package com.gdavidpb.tuindice.summary.di

import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import com.gdavidpb.tuindice.base.domain.startup.AppStartupTask
import com.gdavidpb.tuindice.summary.data.repository.share.ShareBackgroundRemoteDataRepository
import com.gdavidpb.tuindice.summary.data.repository.share.ShareBackgroundSettingsDataRepository
import com.gdavidpb.tuindice.summary.data.repository.user.LocalDataRepository
import com.gdavidpb.tuindice.summary.data.repository.user.RemoteDataRepository
import com.gdavidpb.tuindice.summary.data.repository.user.SettingsDataRepository
import com.gdavidpb.tuindice.summary.data.source.LocalSettingsDataSource
import com.gdavidpb.tuindice.summary.data.source.ProfilePictureImageLoaderDataSource
import com.gdavidpb.tuindice.summary.data.source.RoomDataSource
import com.gdavidpb.tuindice.summary.data.source.ShareBackgroundApiDataSource
import com.gdavidpb.tuindice.summary.data.source.ShareBackgroundDataSource
import com.gdavidpb.tuindice.summary.data.source.ShareBackgroundSettingsDataSource
import com.gdavidpb.tuindice.summary.data.source.SummaryApiDataSource
import com.gdavidpb.tuindice.summary.data.source.TermReportFeedbackDataSource
import com.gdavidpb.tuindice.summary.data.source.UserDataSource
import com.gdavidpb.tuindice.summary.domain.repository.ShareBackgroundRepository
import com.gdavidpb.tuindice.summary.domain.repository.ShareBackgroundSelectionRepository
import com.gdavidpb.tuindice.summary.domain.repository.TermReportFeedbackRepository
import com.gdavidpb.tuindice.summary.domain.repository.TermReportSeenRepository
import com.gdavidpb.tuindice.summary.domain.repository.UserRepository
import com.gdavidpb.tuindice.summary.domain.usecase.GetCameraAvailabilityUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.MarkTermReportSeenUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveShareTermUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveSummaryHubUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveSyncUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveTermReportEntryUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveTermReportUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RateTermReportUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RemoveProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.SetShareBackgroundPreferenceUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.ShareTermImageUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.SummaryObservations
import com.gdavidpb.tuindice.summary.domain.usecase.SummaryTermReports
import com.gdavidpb.tuindice.summary.domain.usecase.UpdateShareBackgroundsUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UpdateTermReportsUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UpdateUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UploadProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.ObserveTermReportExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.RateTermReportExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.RemoveProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.ShareTermImageExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UpdateTermReportsExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UpdateUserExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UploadProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.presentation.machine.ShareTermMachine
import com.gdavidpb.tuindice.summary.presentation.machine.SummaryMachine
import com.gdavidpb.tuindice.summary.presentation.machine.SummaryStatisticsMachine
import com.gdavidpb.tuindice.summary.presentation.machine.TermReportMachine
import com.gdavidpb.tuindice.summary.presentation.model.StatisticsTopBarActionBus
import com.gdavidpb.tuindice.summary.presentation.model.TermReportTopBarActionBus
import com.gdavidpb.tuindice.summary.presentation.viewmodel.ShareTermViewModel
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryStatisticsViewModel
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.summary.presentation.viewmodel.TermReportViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val summaryModule = module {
	/* View models */

	viewModelOf(::SummaryViewModel)
	viewModelOf(::SummaryStatisticsViewModel)
	viewModelOf(::ShareTermViewModel)
	viewModelOf(::TermReportViewModel)

	/* Top bar actions the Statistics screen listens to */

	singleOf(::StatisticsTopBarActionBus)
	singleOf(::TermReportTopBarActionBus)

	/* State machines */

	factoryOf(::SummaryMachine)
	factoryOf(::SummaryStatisticsMachine)
	factoryOf(::ShareTermMachine)
	factoryOf(::TermReportMachine)

	/* Use cases */

	factoryOf(::ObserveUserUseCase)
	factoryOf(::ObserveSyncUseCase)
	factoryOf(::ObserveSummaryHubUseCase)
	factoryOf(::SummaryObservations)
	factoryOf(::GetCameraAvailabilityUseCase)
	factoryOf(::UpdateUserUseCase)
	factoryOf(::UploadProfilePictureUseCase)
	factoryOf(::RemoveProfilePictureUseCase)
	factoryOf(::ShareTermImageUseCase)
	factoryOf(::ObserveShareTermUseCase)
	factoryOf(::SetShareBackgroundPreferenceUseCase)
	factoryOf(::UpdateShareBackgroundsUseCase)
	factoryOf(::ObserveTermReportEntryUseCase)
	factoryOf(::UpdateTermReportsUseCase)
	factoryOf(::SummaryTermReports)
	factoryOf(::ObserveTermReportUseCase)
	factoryOf(::MarkTermReportSeenUseCase)
	factoryOf(::RateTermReportUseCase)

	/* Repositories */

	factoryOf(::UserDataSource) { bind<UserRepository>() }
	factoryOf(::ShareBackgroundDataSource) { bind<ShareBackgroundRepository>() }

	/* Data sources */

	factoryOf(::RoomDataSource) { bind<LocalDataRepository>() }
	factoryOf(::SummaryApiDataSource) { bind<RemoteDataRepository>() }
	factoryOf(::ShareBackgroundApiDataSource) { bind<ShareBackgroundRemoteDataRepository>() }
	singleOf(::ShareBackgroundSettingsDataSource) {
		bind<ShareBackgroundSettingsDataRepository>()
		bind<SessionMemory>()
	}
	singleOf(::LocalSettingsDataSource) {
		bind<SettingsDataRepository>()
		bind<ShareBackgroundSelectionRepository>()
		bind<TermReportSeenRepository>()
		bind<SessionMemory>()
	}
	singleOf(::TermReportFeedbackDataSource) {
		bind<TermReportFeedbackRepository>()
		bind<SessionMemory>()
	}
	singleOf(::ProfilePictureImageLoaderDataSource) { bind<SessionMemory>() }
	// Named: an unnamed startup task would take the place of the platform's own.
	single<AppStartupTask>(named(PROFILE_PICTURE_IMAGE_LOADER_TASK_QUALIFIER)) {
		get<ProfilePictureImageLoaderDataSource>()
	}

	/* Exception handlers */

	factoryOf(::UpdateUserExceptionHandler)
	factoryOf(::RemoveProfilePictureExceptionHandler)
	factoryOf(::UploadProfilePictureExceptionHandler)
	factoryOf(::ShareTermImageExceptionHandler)
	factoryOf(::ObserveTermReportExceptionHandler)
	factoryOf(::RateTermReportExceptionHandler)
	factoryOf(::UpdateTermReportsExceptionHandler)
}

private const val PROFILE_PICTURE_IMAGE_LOADER_TASK_QUALIFIER = "summaryProfilePictureImageLoaderTask"
