package com.gdavidpb.tuindice.record.di

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.persistence.data.room.RoomMutationEnvelopeStore
import com.gdavidpb.tuindice.persistence.data.room.daos.PendingMutationDao
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelopeStore
import com.gdavidpb.tuindice.persistence.domain.mutation.StoreBackedMutationEngine
import com.gdavidpb.tuindice.persistence.domain.repository.PersistenceTransactionRunner
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordLocalDataRepository
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordRemoteDataRepository
import com.gdavidpb.tuindice.record.data.repository.RecordSettingsDataRepository
import com.gdavidpb.tuindice.record.data.mutation.AcademicRecordMutation
import com.gdavidpb.tuindice.record.data.mutation.RECORD_MUTATION_STORE_ID
import com.gdavidpb.tuindice.record.data.source.AcademicRecordApiDataSource
import com.gdavidpb.tuindice.record.data.source.AcademicRecordDataSource
import com.gdavidpb.tuindice.record.data.source.AcademicRecordRoomDataSource
import com.gdavidpb.tuindice.record.data.source.LocalSettingsDataSource
import com.gdavidpb.tuindice.record.data.source.RecordSelectionDataSource
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import com.gdavidpb.tuindice.record.domain.repository.RecordSelectionRepository
import com.gdavidpb.tuindice.record.domain.usecase.ObserveRecordUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetRecordViewModeUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetSelectedTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpdateRecordUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpsertAttemptSelectionUseCase
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.RecordExceptionHandler
import com.gdavidpb.tuindice.record.presentation.action.ObserveRecordActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.RefreshRecordActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.SelectRecordTermActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.SetRecordViewModeActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.UpsertAttemptSelectionActionProcessor
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

	/* Action processor */

	factoryOf(::ObserveRecordActionProcessor)
	factoryOf(::RefreshRecordActionProcessor)
	factoryOf(::SetRecordViewModeActionProcessor)
	factoryOf(::SelectRecordTermActionProcessor)
	factoryOf(::UpsertAttemptSelectionActionProcessor)

	/* Use cases */

	factoryOf(::ObserveRecordUseCase)
	factoryOf(::UpdateRecordUseCase)
	factoryOf(::SetRecordViewModeUseCase)
	factoryOf(::SetSelectedTermUseCase)
	factoryOf(::UpsertAttemptSelectionUseCase)

	/* Repositories */

	single<MutationEnvelopeStore<String, AcademicRecordMutation>>(named(RECORD_MUTATION_STORE_QUALIFIER)) {
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

	/* Data sources */

	single<AcademicRecordLocalDataRepository> {
		AcademicRecordRoomDataSource(
			academicRecordDao = get(),
			academicRecordSyncStateDao = get(),
			academicTermDao = get(),
			academicAttemptDao = get(),
			academicAttemptOverrideDao = get(),
			transactionRunner = get()
		)
	}
	singleOf(::LocalSettingsDataSource) { bind<RecordSettingsDataRepository>() }
	singleOf(::AcademicRecordApiDataSource) { bind<AcademicRecordRemoteDataRepository>() }

	/* Exception handlers */

	factoryOf(::RecordExceptionHandler)
}
