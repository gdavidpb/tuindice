package com.gdavidpb.tuindice.record.di

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.persistence.data.room.RoomMutationEnvelopeStore
import com.gdavidpb.tuindice.persistence.data.room.daos.PendingMutationDao
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelopeStore
import com.gdavidpb.tuindice.persistence.domain.mutation.StoreBackedMutationEngine
import com.gdavidpb.tuindice.persistence.domain.repository.PersistenceTransactionRunner
import com.gdavidpb.tuindice.record.data.mutation.AcademicRecordMutation
import com.gdavidpb.tuindice.record.data.mutation.RECORD_MUTATION_STORE_ID
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordLocalDataRepository
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordRemoteDataRepository
import com.gdavidpb.tuindice.record.data.repository.RecordSettingsDataRepository
import com.gdavidpb.tuindice.record.data.source.AcademicRecordApiDataSource
import com.gdavidpb.tuindice.record.data.source.AcademicRecordDataSource
import com.gdavidpb.tuindice.record.data.source.AcademicRecordRoomDataSource
import com.gdavidpb.tuindice.record.data.source.LocalSettingsDataSource
import com.gdavidpb.tuindice.record.data.source.RecordSelectionDataSource
import com.gdavidpb.tuindice.record.data.source.SyntheticTermCreationDataSource
import com.gdavidpb.tuindice.record.data.source.SyntheticTermLoadPreviewDataSource
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import com.gdavidpb.tuindice.record.domain.repository.RecordSelectionRepository
import com.gdavidpb.tuindice.record.domain.repository.SyntheticTermCreationRepository
import com.gdavidpb.tuindice.record.domain.repository.SyntheticTermLoadPreviewRepository
import com.gdavidpb.tuindice.record.domain.usecase.CreateSyntheticTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.DeleteSyntheticTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.LoadSyntheticTermPreviewUseCase
import com.gdavidpb.tuindice.record.domain.usecase.LoadSyntheticTermEditSeedUseCase
import com.gdavidpb.tuindice.record.domain.usecase.ObserveRecordUseCase
import com.gdavidpb.tuindice.record.domain.usecase.ObserveSyntheticTermCreationUseCase
import com.gdavidpb.tuindice.record.domain.usecase.RefreshSyntheticTermSubjectSearchUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetRecordViewModeUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetSelectedTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpdateRecordUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpdateSyntheticTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpsertAttemptSelectionUseCase
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.RecordExceptionHandler
import com.gdavidpb.tuindice.record.presentation.action.CreateSyntheticTermActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.DeleteSyntheticTermActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.ObserveCreateSyntheticTermActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.ObserveRecordActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.RefreshRecordActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.SelectRecordTermActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.SetRecordViewModeActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.UpdateCreateSyntheticTermQueryActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.UpsertAttemptSelectionActionProcessor
import com.gdavidpb.tuindice.record.presentation.machine.CreateSyntheticTermMachine
import com.gdavidpb.tuindice.record.presentation.machine.RecordMachine
import com.gdavidpb.tuindice.record.presentation.viewmodel.CreateSyntheticTermViewModel
import com.gdavidpb.tuindice.record.presentation.viewmodel.RecordViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val RECORD_MUTATION_STORE_QUALIFIER = "recordMutationStore"
private const val RECORD_MUTATION_ENGINE_QUALIFIER = "recordMutationEngine"

val recordModule = module {
	/* View models */

	viewModelOf(::RecordViewModel)
	viewModelOf(::CreateSyntheticTermViewModel)

	/* Screen machines */

	factoryOf(::RecordMachine)
	factoryOf(::CreateSyntheticTermMachine)

	/* Action processor */

	factoryOf(::ObserveRecordActionProcessor)
	factoryOf(::RefreshRecordActionProcessor)
	factoryOf(::SetRecordViewModeActionProcessor)
	factoryOf(::SelectRecordTermActionProcessor)
	factoryOf(::UpsertAttemptSelectionActionProcessor)
	factoryOf(::DeleteSyntheticTermActionProcessor)
	factoryOf(::ObserveCreateSyntheticTermActionProcessor)
	factoryOf(::UpdateCreateSyntheticTermQueryActionProcessor)
	factoryOf(::CreateSyntheticTermActionProcessor)

	/* Use cases */

	factoryOf(::ObserveRecordUseCase)
	factoryOf(::UpdateRecordUseCase)
	factoryOf(::SetRecordViewModeUseCase)
	factoryOf(::SetSelectedTermUseCase)
	factoryOf(::UpsertAttemptSelectionUseCase)
	factoryOf(::DeleteSyntheticTermUseCase)
	factoryOf(::ObserveSyntheticTermCreationUseCase)
	factoryOf(::RefreshSyntheticTermSubjectSearchUseCase)
	factoryOf(::LoadSyntheticTermPreviewUseCase)
	factoryOf(::CreateSyntheticTermUseCase)
	factoryOf(::UpdateSyntheticTermUseCase)
	factoryOf(::LoadSyntheticTermEditSeedUseCase)

	/* Repositories */

	single<MutationEnvelopeStore<String, AcademicRecordMutation>>(
		named(
			RECORD_MUTATION_STORE_QUALIFIER
		)
	) {
		RoomMutationEnvelopeStore(
			pendingMutationDao = get<PendingMutationDao>(),
			transactionRunner = get<PersistenceTransactionRunner>(),
			storeId = RECORD_MUTATION_STORE_ID,
			commandSerializer = AcademicRecordMutation.serializer()
		)
	}
	single<StoreBackedMutationEngine<String, AcademicRecordMutation, AcademicRecord, AcademicRecord, AcademicRecord>>(
		named(RECORD_MUTATION_ENGINE_QUALIFIER)
	) {
		StoreBackedMutationEngine(
			storeId = RECORD_MUTATION_STORE_ID,
			outboxStore = get(named(RECORD_MUTATION_STORE_QUALIFIER))
		)
	}
	single<AcademicRecordRepository> {
		AcademicRecordDataSource(
			localDataSource = get(),
			remoteDataSource = get(),
			settingsDataSource = get(),
			mutationEngine = get(named(RECORD_MUTATION_ENGINE_QUALIFIER)),
			identifierRepository = get()
		)
	}
	singleOf(::RecordSelectionDataSource) { bind<RecordSelectionRepository>() }
	singleOf(::SyntheticTermCreationDataSource) { bind<SyntheticTermCreationRepository>() }
	singleOf(::SyntheticTermLoadPreviewDataSource) { bind<SyntheticTermLoadPreviewRepository>() }

	/* Data sources */

	singleOf(::LocalSettingsDataSource) { bind<RecordSettingsDataRepository>() }
	singleOf(::AcademicRecordApiDataSource) { bind<AcademicRecordRemoteDataRepository>() }
	singleOf(::AcademicRecordRoomDataSource) { bind<AcademicRecordLocalDataRepository>() }

	/* Exception handlers */

	factoryOf(::RecordExceptionHandler)
}
