package com.gdavidpb.tuindice.persistence.di

import androidx.room.Room
import com.gdavidpb.tuindice.base.BuildConfig
import com.gdavidpb.tuindice.base.utils.extension.create
import com.gdavidpb.tuindice.persistence.data.api.ApiDataSource
import com.gdavidpb.tuindice.persistence.data.api.SyncApi
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.internal.RoomDataSource
import com.gdavidpb.tuindice.persistence.data.room.schema.DatabaseModel
import com.gdavidpb.tuindice.persistence.data.tracker.TrackerDataRepository
import com.gdavidpb.tuindice.persistence.data.tracker.source.LocalDataSource
import com.gdavidpb.tuindice.persistence.data.tracker.source.RemoteDataSource
import com.gdavidpb.tuindice.persistence.data.tracker.source.SchedulerDataSource
import com.gdavidpb.tuindice.persistence.data.workmanager.WorkManagerDataSource
import com.gdavidpb.tuindice.persistence.domain.repository.TrackerRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val persistenceModule = module {

	/* Database */

	single {
		Room.databaseBuilder(androidContext(), TuIndiceDatabase::class.java, DatabaseModel.NAME)
			.fallbackToDestructiveMigration()
			.build()
	}

	/* Sync Api */

	single {
		Retrofit.Builder()
			.baseUrl(BuildConfig.ENDPOINT_TU_INDICE_API)
			.addConverterFactory(GsonConverterFactory.create())
			.client(get())
			.build()
			.create<SyncApi>()
	}

	/* Repositories */

	factoryOf(::TrackerDataRepository) { bind<TrackerRepository>() }

	/* Data sources */

	factoryOf(::RoomDataSource) { bind<LocalDataSource>() }
	factoryOf(::ApiDataSource) { bind<RemoteDataSource>() }
	factoryOf(::WorkManagerDataSource) { bind<SchedulerDataSource>() }
}