package com.gdavidpb.tuindice.persistence.di

import androidx.room.Room
import com.gdavidpb.tuindice.base.domain.repository.PersistenceRepository
import com.gdavidpb.tuindice.persistence.data.source.room.RoomDataSource
import com.gdavidpb.tuindice.persistence.data.source.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.source.room.schema.DatabaseModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val persistenceModule = module {

	/* Repositories */

	factoryOf(::RoomDataSource) { bind<PersistenceRepository>() }

	/* Database */

	single {
		Room.databaseBuilder(androidContext(), TuIndiceDatabase::class.java, DatabaseModel.NAME)
			.fallbackToDestructiveMigration()
			.build()
	}
}