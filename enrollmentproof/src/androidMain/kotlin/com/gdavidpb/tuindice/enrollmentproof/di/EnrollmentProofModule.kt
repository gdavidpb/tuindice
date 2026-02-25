package com.gdavidpb.tuindice.enrollmentproof.di

import com.gdavidpb.tuindice.enrollmentproof.data.repository.StorageDataSource
import com.gdavidpb.tuindice.enrollmentproof.data.source.InternalStorageDataSource
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val enrollmentProofModule = module {
	/* Android data sources */

	factoryOf(::InternalStorageDataSource) { bind<StorageDataSource>() }
}
