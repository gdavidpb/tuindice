package com.gdavidpb.tuindice.enrollmentproof.di

import com.gdavidpb.tuindice.enrollmentproof.data.repository.StorageDataSource
import com.gdavidpb.tuindice.enrollmentproof.data.source.IosStorageDataSource
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val enrollmentProofIosModule = module {
	factoryOf(::IosStorageDataSource) { bind<StorageDataSource>() }
}
