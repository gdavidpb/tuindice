package com.gdavidpb.tuindice.record.di

import com.gdavidpb.tuindice.record.data.repository.QuarterDataRepository
import com.gdavidpb.tuindice.record.data.repository.QuarterLocalDataSource
import com.gdavidpb.tuindice.record.data.repository.QuarterRemoteDataSource
import com.gdavidpb.tuindice.record.data.repository.QuarterSettingsDataSource
import com.gdavidpb.tuindice.record.data.source.PreferencesDataSource
import com.gdavidpb.tuindice.record.data.source.RecordApiDataSource
import com.gdavidpb.tuindice.record.data.source.RoomDataSource
import com.gdavidpb.tuindice.record.domain.service.IndexComputationEngine
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
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val recordModule = module {
	/* View models */

	factoryOf(::RecordViewModel)

	/* Action processor */

	factoryOf(::LoadQuartersActionProcessor)
	factoryOf(::SetSubjectGradeActionProcessor)

	/* Use cases */

	factoryOf(::GetQuartersUseCase)
	factoryOf(::RemoveQuarterUseCase)
	factoryOf(::SetSubjectGradeUseCase)

	/* Computation */

	singleOf(::IndexComputationEngine)

	/* Validators */

	factoryOf(::SetSubjectGradeParamsValidator)

	/* Repositories */

	factoryOf(::QuarterDataRepository) { bind<QuarterRepository>() }

	/* Data sources */

	singleOf(::RoomDataSource) { bind<QuarterLocalDataSource>() }
	factoryOf(::RecordApiDataSource) { bind<QuarterRemoteDataSource>() }
	factoryOf(::PreferencesDataSource) { bind<QuarterSettingsDataSource>() }

	/* Exception handlers */

	factoryOf(::GetQuartersExceptionHandler)
	factoryOf(::SetSubjectGradeExceptionHandler)
}
