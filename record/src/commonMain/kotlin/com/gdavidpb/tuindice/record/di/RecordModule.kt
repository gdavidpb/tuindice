package com.gdavidpb.tuindice.record.di

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.persistence.data.room.RoomMutationEnvelopeStore
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelopeStore
import com.gdavidpb.tuindice.persistence.domain.mutation.StoreBackedMutationEngine
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
import com.gdavidpb.tuindice.record.presentation.viewmodel.RecordViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlinx.serialization.builtins.serializer

private const val RECORD_MUTATION_STORE_QUALIFIER = "recordMutationStore"
private const val RECORD_MUTATION_ENGINE_QUALIFIER = "recordMutationEngine"

val recordModule = module {
	viewModelOf(::RecordViewModel)

	single<MutationEnvelopeStore<String, AcademicRecordMutation>>(named(RECORD_MUTATION_STORE_QUALIFIER)) {
		RoomMutationEnvelopeStore(
			room = get(),
			storeId = RECORD_MUTATION_STORE_ID,
			scopeKeySerializer = String.serializer(),
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

	single<AcademicRecordLocalDataRepository> { AcademicRecordRoomDataSource(room = get()) }
	singleOf(::LocalSettingsDataSource) { bind<RecordSettingsDataRepository>() }
	singleOf(::AcademicRecordApiDataSource) { bind<AcademicRecordRemoteDataRepository>() }
}
