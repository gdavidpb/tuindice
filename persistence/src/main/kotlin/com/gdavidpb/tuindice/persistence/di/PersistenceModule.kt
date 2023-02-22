package com.gdavidpb.tuindice.persistence.di

import androidx.room.Room
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.schema.DatabaseModel
import com.gdavidpb.tuindice.persistence.data.tracker.TrackerDataRepository
import com.gdavidpb.tuindice.persistence.data.tracker.source.SchedulerDataSource
import com.gdavidpb.tuindice.persistence.data.tracker.source.TransactionDataSource
import com.gdavidpb.tuindice.persistence.data.transaction.RoomDataSource
import com.gdavidpb.tuindice.persistence.data.workmanager.WorkManagerDataSource
import com.gdavidpb.tuindice.persistence.domain.repository.TrackerRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val persistenceModule = module {

	/* Database */

	single {
		Room.databaseBuilder(androidContext(), TuIndiceDatabase::class.java, DatabaseModel.NAME)
			.fallbackToDestructiveMigration()
			.build()
	}

	/* Repositories */

	factoryOf(::TrackerDataRepository) { bind<TrackerRepository>() }

	/* Data sources */

	factoryOf(::WorkManagerDataSource) { bind<SchedulerDataSource>() }
	factoryOf(::RoomDataSource) { bind<TransactionDataSource>() }
}