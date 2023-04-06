package com.gdavidpb.tuindice.transactions.di

import com.gdavidpb.tuindice.base.BuildConfig
import com.gdavidpb.tuindice.base.utils.extension.create
import com.gdavidpb.tuindice.transactions.data.api.ApiDataSource
import com.gdavidpb.tuindice.transactions.data.api.SyncApi
import com.gdavidpb.tuindice.transactions.data.offline.ResolutionDataRepository
import com.gdavidpb.tuindice.transactions.data.offline.TransactionDataRepository
import com.gdavidpb.tuindice.transactions.data.offline.source.LocalDataSource
import com.gdavidpb.tuindice.transactions.data.offline.source.RemoteDataSource
import com.gdavidpb.tuindice.transactions.data.offline.source.SchedulerDataSource
import com.gdavidpb.tuindice.transactions.data.room.RoomDataSource
import com.gdavidpb.tuindice.transactions.data.workmanager.WorkManagerDataSource
import com.gdavidpb.tuindice.transactions.domain.repository.ResolutionRepository
import com.gdavidpb.tuindice.transactions.domain.repository.TransactionRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val transactionsModule = module {
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

	factoryOf(::TransactionDataRepository) { bind<TransactionRepository>() }
	factoryOf(::ResolutionDataRepository) { bind<ResolutionRepository>() }

	/* Data sources */

	factoryOf(::RoomDataSource) { bind<LocalDataSource>() }
	factoryOf(::ApiDataSource) { bind<RemoteDataSource>() }
	factoryOf(::WorkManagerDataSource) { bind<SchedulerDataSource>() }
}