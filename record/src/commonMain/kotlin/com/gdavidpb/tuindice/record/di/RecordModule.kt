package com.gdavidpb.tuindice.record.di

import com.gdavidpb.tuindice.base.domain.repository.MutationOutboxRepository
import com.gdavidpb.tuindice.persistence.data.room.RoomMutationOutboxRepository
import com.gdavidpb.tuindice.record.data.repository.QuarterDataRepository
import com.gdavidpb.tuindice.record.data.repository.QuarterLocalDataSource
import com.gdavidpb.tuindice.record.data.repository.QuarterRemoteDataSource
import com.gdavidpb.tuindice.record.data.repository.QuarterSettingsDataSource
import com.gdavidpb.tuindice.record.data.repository.mutation.RecordMutation
import com.gdavidpb.tuindice.record.data.source.LocalSettingsDataSource
import com.gdavidpb.tuindice.record.data.source.RecordApiDataSource
import com.gdavidpb.tuindice.record.data.source.RoomDataSource
import com.gdavidpb.tuindice.record.data.source.VisibleRecordStateResolver
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import com.gdavidpb.tuindice.record.domain.service.IndexComputationEngine
import com.gdavidpb.tuindice.record.domain.usecase.ObserveQuartersUseCase
import com.gdavidpb.tuindice.record.domain.usecase.RemoveQuarterUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetSubjectGradeUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpdateQuartersUseCase
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.SetSubjectGradeExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.UpdateQuartersExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.validator.SetSubjectGradeParamsValidator
import com.gdavidpb.tuindice.record.presentation.action.ObserveQuartersActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.RefreshQuartersActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.SetSubjectGradeActionProcessor
import com.gdavidpb.tuindice.record.presentation.viewmodel.RecordViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val recordModule = module {
	/* View models */

	viewModelOf(::RecordViewModel)

	/* Action processor */

	factoryOf(::ObserveQuartersActionProcessor)
	factoryOf(::RefreshQuartersActionProcessor)
	factoryOf(::SetSubjectGradeActionProcessor)

	/* Use cases */

	factoryOf(::ObserveQuartersUseCase)
	factoryOf(::UpdateQuartersUseCase)
	factoryOf(::RemoveQuarterUseCase)
	factoryOf(::SetSubjectGradeUseCase)

	/* Computation */

	singleOf(::IndexComputationEngine)
	singleOf(::VisibleRecordStateResolver)

	/* Validators */

	factoryOf(::SetSubjectGradeParamsValidator)

	/* Repositories */

	singleOf(::QuarterDataRepository) { bind<QuarterRepository>() }
	single<MutationOutboxRepository<RecordMutation>> {
		RoomMutationOutboxRepository(
			room = get(),
			serializer = RecordMutation.serializer()
		)
	}

	/* Data sources */

	singleOf(::RoomDataSource) { bind<QuarterLocalDataSource>() }
	factoryOf(::RecordApiDataSource) { bind<QuarterRemoteDataSource>() }
	singleOf(::LocalSettingsDataSource) { bind<QuarterSettingsDataSource>() }

	/* Exception handlers */

	factoryOf(::UpdateQuartersExceptionHandler)
	factoryOf(::SetSubjectGradeExceptionHandler)
}
