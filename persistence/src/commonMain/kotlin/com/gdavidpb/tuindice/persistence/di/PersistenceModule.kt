package com.gdavidpb.tuindice.persistence.di

import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicAttemptDao
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicAttemptOverrideDao
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicRecordDao
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicTermDao
import com.gdavidpb.tuindice.persistence.data.room.daos.EvaluationDao
import com.gdavidpb.tuindice.persistence.data.room.daos.EvaluationSyncStateDao
import com.gdavidpb.tuindice.persistence.data.room.daos.PendingMutationDao
import com.gdavidpb.tuindice.persistence.data.room.daos.UserDao
import com.gdavidpb.tuindice.persistence.data.source.RoomPersistenceMaintenanceDataSource
import com.gdavidpb.tuindice.persistence.data.source.RoomPersistenceTransactionRunner
import com.gdavidpb.tuindice.persistence.domain.repository.PersistenceMaintenanceRepository
import com.gdavidpb.tuindice.persistence.domain.repository.PersistenceTransactionRunner
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val persistenceModule = module {
	single<UserDao> { get<TuIndiceDatabase>().users }
	single<AcademicRecordDao> { get<TuIndiceDatabase>().academicRecords }
	single<AcademicTermDao> { get<TuIndiceDatabase>().academicTerms }
	single<AcademicAttemptDao> { get<TuIndiceDatabase>().academicAttempts }
	single<AcademicAttemptOverrideDao> { get<TuIndiceDatabase>().academicAttemptOverrides }
	single<EvaluationDao> { get<TuIndiceDatabase>().evaluations }
	single<EvaluationSyncStateDao> { get<TuIndiceDatabase>().evaluationSyncState }
	single<PendingMutationDao> { get<TuIndiceDatabase>().pendingMutations }

	singleOf(::RoomPersistenceTransactionRunner) { bind<PersistenceTransactionRunner>() }
	singleOf(::RoomPersistenceMaintenanceDataSource) { bind<PersistenceMaintenanceRepository>() }
}
