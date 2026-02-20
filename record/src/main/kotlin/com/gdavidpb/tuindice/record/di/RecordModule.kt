package com.gdavidpb.tuindice.record.di

import com.gdavidpb.tuindice.record.data.repository.quarter.LocalDataSource
import com.gdavidpb.tuindice.record.data.repository.quarter.QuarterDataRepository
import com.gdavidpb.tuindice.record.data.repository.quarter.RemoteDataSource
import com.gdavidpb.tuindice.record.data.repository.quarter.SettingsDataSource
import com.gdavidpb.tuindice.record.data.repository.quarter.source.PreferencesDataSource
import com.gdavidpb.tuindice.record.data.repository.quarter.source.RecordApiDataSource
import com.gdavidpb.tuindice.record.data.repository.quarter.source.RoomDataSource
import com.gdavidpb.tuindice.record.data.utils.IndexComputationEngine
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import com.gdavidpb.tuindice.record.domain.usecase.GetQuartersUseCase
import com.gdavidpb.tuindice.record.domain.usecase.RemoveQuarterUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetSubjectGradeUseCase
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.GetQuartersExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.SetSubjectGradeExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.validator.SetSubjectGradeParamsValidator
import com.gdavidpb.tuindice.record.presentation.action.LoadQuartersActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.SetSubjectGradeActionProcessor
import com.gdavidpb.tuindice.record.presentation.viewmodel.RecordViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val recordModule = module {
	/* View Models */

	viewModelOf(::RecordViewModel)

	/* Action processor */

	factoryOf(::LoadQuartersActionProcessor)
	factoryOf(::SetSubjectGradeActionProcessor)

	/* Use cases */

	factoryOf(::GetQuartersUseCase)
	factoryOf(::RemoveQuarterUseCase)
	factoryOf(::SetSubjectGradeUseCase)

	/* Computation */

	single { IndexComputationEngine() }

	/* Validators */

	factoryOf(::SetSubjectGradeParamsValidator)

	/* Repositories */

	factoryOf(::QuarterDataRepository) { bind<QuarterRepository>() }

	/* Data sources */

	factoryOf(::RoomDataSource) { bind<LocalDataSource>() }
	factoryOf(::RecordApiDataSource) { bind<RemoteDataSource>() }
	factoryOf(::PreferencesDataSource) { bind<SettingsDataSource>() }

	/* Exception handlers */

	factoryOf(::GetQuartersExceptionHandler)
	factoryOf(::SetSubjectGradeExceptionHandler)
}
