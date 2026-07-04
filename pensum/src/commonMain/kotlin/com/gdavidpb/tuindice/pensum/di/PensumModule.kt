package com.gdavidpb.tuindice.pensum.di

import com.gdavidpb.tuindice.pensum.data.repository.PensumLocalDataRepository
import com.gdavidpb.tuindice.pensum.data.repository.PensumRemoteDataRepository
import com.gdavidpb.tuindice.pensum.data.source.KtorPensumApiDataSource
import com.gdavidpb.tuindice.pensum.data.source.LocalSettingsDataSource
import com.gdavidpb.tuindice.pensum.data.source.PensumDataSource
import com.gdavidpb.tuindice.pensum.data.source.PensumRoomDataSource
import com.gdavidpb.tuindice.pensum.domain.engine.PensumStatusEngine
import com.gdavidpb.tuindice.pensum.domain.repository.PensumRepository
import com.gdavidpb.tuindice.pensum.domain.repository.PensumSelectionRepository
import com.gdavidpb.tuindice.pensum.domain.usecase.EnsurePensumLoadedUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.ObservePensumUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.SelectPensumModalityUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.SelectPensumSelectionUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.SelectPensumUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.SetPensumSummaryCollapsedUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.UpdatePensumUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.exceptionhandler.UpdatePensumExceptionHandler
import com.gdavidpb.tuindice.pensum.presentation.machine.PensumMachine
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenSessionStore
import com.gdavidpb.tuindice.pensum.presentation.model.PensumTopBarActionBus
import com.gdavidpb.tuindice.pensum.presentation.viewmodel.PensumViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val pensumModule = module {
	viewModelOf(::PensumViewModel)

	factoryOf(::PensumMachine)

	factoryOf(::ObservePensumUseCase)
	factoryOf(::EnsurePensumLoadedUseCase)
	factoryOf(::UpdatePensumUseCase)
	factoryOf(::SelectPensumUseCase)
	factoryOf(::SelectPensumModalityUseCase)
	factoryOf(::SelectPensumSelectionUseCase)
	factoryOf(::SetPensumSummaryCollapsedUseCase)

	singleOf(::PensumStatusEngine)
	singleOf(::KtorPensumApiDataSource) { bind<PensumRemoteDataRepository>() }
	singleOf(::PensumRoomDataSource) { bind<PensumLocalDataRepository>() }
	singleOf(::LocalSettingsDataSource) { bind<PensumSelectionRepository>() }
	singleOf(::PensumDataSource) { bind<PensumRepository>() }

	factoryOf(::UpdatePensumExceptionHandler)
	singleOf(::PensumScreenSessionStore)
	singleOf(::PensumTopBarActionBus)
}
