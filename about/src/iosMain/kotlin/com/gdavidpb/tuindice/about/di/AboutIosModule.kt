package com.gdavidpb.tuindice.about.di

import com.gdavidpb.tuindice.about.data.repository.AppInfoDataSource
import com.gdavidpb.tuindice.about.data.repository.EnvironmentDataSource
import com.gdavidpb.tuindice.about.data.source.IosAppInfoDataSource
import com.gdavidpb.tuindice.about.data.source.IosEnvironmentDataSource
import com.gdavidpb.tuindice.about.data.source.IosExternalActionsDataSource
import com.gdavidpb.tuindice.about.domain.repository.ExternalActionsRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val aboutIosModule = module {
	factoryOf(::IosEnvironmentDataSource) { bind<EnvironmentDataSource>() }
	factoryOf(::IosAppInfoDataSource) { bind<AppInfoDataSource>() }
	factoryOf(::IosExternalActionsDataSource) { bind<ExternalActionsRepository>() }
}
