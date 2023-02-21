package com.gdavidpb.tuindice.persistence.di

import androidx.room.Room
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.tracker.RoomWorkManagerDataSource
import com.gdavidpb.tuindice.persistence.data.room.tracker.TransactionDataSource
import com.gdavidpb.tuindice.persistence.data.room.schema.DatabaseModel
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

	/* Data sources */

	factoryOf(::RoomWorkManagerDataSource) { bind<TransactionDataSource>() }
}