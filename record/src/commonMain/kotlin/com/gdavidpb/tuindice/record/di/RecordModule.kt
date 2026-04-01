package com.gdavidpb.tuindice.record.di

import com.gdavidpb.tuindice.persistence.data.room.RoomMutationEnvelopeStore
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelopeStore
import com.gdavidpb.tuindice.persistence.domain.mutation.StoreBackedMutationEngine
import com.gdavidpb.tuindice.record.data.source.QuarterDataSource
import com.gdavidpb.tuindice.record.data.source.QuarterSelectionDataSource
import com.gdavidpb.tuindice.record.data.repository.QuarterLocalDataRepository
import com.gdavidpb.tuindice.record.data.repository.QuarterRemoteDataRepository
import com.gdavidpb.tuindice.record.data.repository.QuarterSettingsDataRepository
import com.gdavidpb.tuindice.record.data.mutation.RECORD_MUTATION_STORE_ID
import com.gdavidpb.tuindice.record.data.mutation.RecordMutation
import com.gdavidpb.tuindice.record.data.mutation.RecordMutationAck
import com.gdavidpb.tuindice.record.data.model.quarter.LocalQuarter
import com.gdavidpb.tuindice.record.data.source.LocalSettingsDataSource
import com.gdavidpb.tuindice.record.data.source.RecordApiDataSource
import com.gdavidpb.tuindice.record.data.source.RoomDataSource
import com.gdavidpb.tuindice.record.data.resolver.VisibleRecordStateResolver
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import com.gdavidpb.tuindice.record.domain.repository.QuarterSelectionRepository
import com.gdavidpb.tuindice.record.domain.service.IndexComputationEngine
import com.gdavidpb.tuindice.record.domain.usecase.AddQuarterUseCase
import com.gdavidpb.tuindice.record.domain.usecase.GetSelectedQuarterIdUseCase
import com.gdavidpb.tuindice.record.domain.usecase.ObserveQuartersUseCase
import com.gdavidpb.tuindice.record.domain.usecase.RemoveQuarterUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetSelectedQuarterIdUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetSubjectGradeUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpdateQuartersUseCase
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.SetSubjectGradeExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.UpdateQuartersExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.validator.SetSubjectGradeParamsValidator
import com.gdavidpb.tuindice.record.presentation.action.ObserveQuartersActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.RefreshQuartersActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.SelectQuarterActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.SetSubjectGradeActionProcessor
import com.gdavidpb.tuindice.record.presentation.viewmodel.RecordViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlinx.serialization.builtins.serializer

private const val RECORD_MUTATION_STORE_QUALIFIER = "recordMutationStore"
private const val RECORD_MUTATION_ENGINE_QUALIFIER = "recordMutationEngine"

val recordModule = module {
	/* View models */

	viewModelOf(::RecordViewModel)

	/* Action processor */

	factoryOf(::ObserveQuartersActionProcessor)
	factoryOf(::RefreshQuartersActionProcessor)
	factoryOf(::SelectQuarterActionProcessor)
	factoryOf(::SetSubjectGradeActionProcessor)

	/* Use cases */

	factoryOf(::AddQuarterUseCase)
	factoryOf(::GetSelectedQuarterIdUseCase)
	factoryOf(::ObserveQuartersUseCase)
	factoryOf(::UpdateQuartersUseCase)
	factoryOf(::RemoveQuarterUseCase)
	factoryOf(::SetSelectedQuarterIdUseCase)
	factoryOf(::SetSubjectGradeUseCase)

	/* Computation */

	singleOf(::IndexComputationEngine)
	singleOf(::VisibleRecordStateResolver)

	/* Validators */

	factoryOf(::SetSubjectGradeParamsValidator)

	/* Repositories */

	single<MutationEnvelopeStore<String, RecordMutation>>(named(RECORD_MUTATION_STORE_QUALIFIER)) {
		RoomMutationEnvelopeStore(
			room = get(),
			storeId = RECORD_MUTATION_STORE_ID,
			scopeKeySerializer = String.serializer(),
			commandSerializer = RecordMutation.serializer()
		)
	}
	single<StoreBackedMutationEngine<String, RecordMutation, List<LocalQuarter>, List<LocalQuarter>, RecordMutationAck>>(
		named(RECORD_MUTATION_ENGINE_QUALIFIER)
	) {
		StoreBackedMutationEngine(
			storeId = RECORD_MUTATION_STORE_ID,
			outboxStore = get(named(RECORD_MUTATION_STORE_QUALIFIER))
		)
	}
	single<QuarterRepository> {
		QuarterDataSource(
			localDataSource = get(),
			remoteDataSource = get(),
			settingsDataSource = get(),
			mutationEngine = get(named(RECORD_MUTATION_ENGINE_QUALIFIER)),
			identifierRepository = get()
		)
	}
	singleOf(::QuarterSelectionDataSource) { bind<QuarterSelectionRepository>() }

	/* Data sources */

	single<QuarterLocalDataRepository> {
		RoomDataSource(
			room = get(),
			indexComputationEngine = get(),
			mutationEngine = get(named(RECORD_MUTATION_ENGINE_QUALIFIER)),
			visibleRecordStateResolver = get()
		)
	}
	factoryOf(::RecordApiDataSource) { bind<QuarterRemoteDataRepository>() }
	singleOf(::LocalSettingsDataSource) { bind<QuarterSettingsDataRepository>() }

	/* Exception handlers */

	factoryOf(::UpdateQuartersExceptionHandler)
	factoryOf(::SetSubjectGradeExceptionHandler)
}
