package com.gdavidpb.tuindice.transactions.di

import androidx.work.ListenableWorker
import com.gdavidpb.tuindice.base.BuildConfig
import com.gdavidpb.tuindice.base.utils.extension.create
import com.gdavidpb.tuindice.transactions.data.repository.transactions.LocalDataSource
import com.gdavidpb.tuindice.transactions.data.repository.transactions.RemoteDataSource
import com.gdavidpb.tuindice.transactions.data.repository.transactions.ResolutionDataRepository
import com.gdavidpb.tuindice.transactions.data.repository.transactions.SchedulerDataSource
import com.gdavidpb.tuindice.transactions.data.repository.transactions.SyncApi
import com.gdavidpb.tuindice.transactions.data.repository.transactions.SyncWorker
import com.gdavidpb.tuindice.transactions.data.repository.transactions.TransactionDataRepository
import com.gdavidpb.tuindice.transactions.data.repository.transactions.source.RoomDataSource
import com.gdavidpb.tuindice.transactions.data.repository.transactions.source.SyncApiDataSource
import com.gdavidpb.tuindice.transactions.data.repository.transactions.source.WorkManagerDataSource
import com.gdavidpb.tuindice.transactions.data.repository.transactions.source.api.retrofit.TransactionInterceptor
import com.gdavidpb.tuindice.transactions.domain.repository.ResolutionRepository
import com.gdavidpb.tuindice.transactions.domain.repository.TransactionRepository
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import retrofit2.Retrofit

val transactionsModule = module {
	/* Sync Api */

	single {
		Retrofit.Builder()
			.baseUrl(BuildConfig.ENDPOINT_TU_INDICE_API)
			.addConverterFactory(get())
			.client(get())
			.build()
			.create<SyncApi>()
	}

	/* Utils */

	workerOf(::SyncWorker) { bind<ListenableWorker>() }

	singleOf(::TransactionInterceptor)

	/* Repositories */

	factoryOf(::TransactionDataRepository) { bind<TransactionRepository>() }
	factoryOf(::ResolutionDataRepository) { bind<ResolutionRepository>() }

	/* Data sources */

	factoryOf(::RoomDataSource) { bind<LocalDataSource>() }
	factoryOf(::SyncApiDataSource) { bind<RemoteDataSource>() }
	factoryOf(::WorkManagerDataSource) { bind<SchedulerDataSource>() }
}