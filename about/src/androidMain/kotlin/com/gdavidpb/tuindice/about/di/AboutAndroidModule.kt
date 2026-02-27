package com.gdavidpb.tuindice.about.di

import com.gdavidpb.tuindice.about.data.repository.AppInfoDataSource
import com.gdavidpb.tuindice.about.data.repository.EnvironmentDataSource
import com.gdavidpb.tuindice.about.data.source.AndroidAppInfoDataSource
import com.gdavidpb.tuindice.about.data.source.AndroidEnvironmentDataSource
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val aboutAndroidModule = module {
	factoryOf(::AndroidEnvironmentDataSource) { bind<EnvironmentDataSource>() }
	factoryOf(::AndroidAppInfoDataSource) { bind<AppInfoDataSource>() }
}
