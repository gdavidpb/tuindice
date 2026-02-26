package com.gdavidpb.tuindice.about.di

import com.gdavidpb.tuindice.about.data.repository.AboutDataRepository
import com.gdavidpb.tuindice.about.data.repository.AppInfoDataSource
import com.gdavidpb.tuindice.about.data.repository.EnvironmentDataSource
import com.gdavidpb.tuindice.about.data.source.DefaultAppInfoDataSource
import com.gdavidpb.tuindice.about.data.source.DefaultEnvironmentDataSource
import com.gdavidpb.tuindice.about.domain.repository.AboutRepository
import com.gdavidpb.tuindice.about.domain.usecase.LoadVersionUseCase
import com.gdavidpb.tuindice.about.presentation.action.*
import com.gdavidpb.tuindice.about.presentation.resource.AboutTextProvider
import com.gdavidpb.tuindice.about.presentation.resource.DefaultAboutTextProvider
import com.gdavidpb.tuindice.about.presentation.viewmodel.AboutViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val aboutCommonModule = module {
	/* View models */

	factoryOf(::AboutViewModel)

	/* Use cases */

	factoryOf(::LoadVersionUseCase)

	/* Repositories */

	factoryOf(::AboutDataRepository) { bind<AboutRepository>() }

	/* Shared text resources */

	factoryOf(::DefaultAboutTextProvider) { bind<AboutTextProvider>() }
	factoryOf(::DefaultEnvironmentDataSource) { bind<EnvironmentDataSource>() }
	factoryOf(::DefaultAppInfoDataSource) { bind<AppInfoDataSource>() }

	/* Action processor */

	factoryOf(::LoadVersionActionProcessor)
	factoryOf(::ContactDeveloperActionProcessor)
	factoryOf(::OpenTermsAndConditionsActionProcessor)
	factoryOf(::OpenPrivacyPolicyActionProcessor)
	factoryOf(::ShareAppActionProcessor)
	factoryOf(::RateOnPlayStoreActionProcessor)
	factoryOf(::ReportBugActionProcessor)
	factoryOf(::OpenUrlActionProcessor)
}
