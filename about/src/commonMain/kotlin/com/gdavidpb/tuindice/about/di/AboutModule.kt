package com.gdavidpb.tuindice.about.di

import com.gdavidpb.tuindice.about.data.source.AboutDataSource
import com.gdavidpb.tuindice.about.domain.repository.AboutRepository
import com.gdavidpb.tuindice.about.domain.usecase.LoadVersionUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenExternalUrlUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenStoreUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SendSupportEmailUseCase
import com.gdavidpb.tuindice.about.presentation.action.*
import com.gdavidpb.tuindice.about.presentation.viewmodel.AboutViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val aboutModule = module {
	/* View models */

	viewModelOf(::AboutViewModel)

	/* Use cases */

	factoryOf(::LoadVersionUseCase)
	factoryOf(::OpenExternalUrlUseCase)
	factoryOf(::SendSupportEmailUseCase)
	factoryOf(::OpenStoreUseCase)

	/* Repositories */

	factoryOf(::AboutDataSource) { bind<AboutRepository>() }

	/* Action processor */

	factoryOf(::LoadVersionActionProcessor)
	factoryOf(::ContactDeveloperActionProcessor)
	factoryOf(::OpenTermsAndConditionsActionProcessor)
	factoryOf(::OpenPrivacyPolicyActionProcessor)
	factoryOf(::OpenSupportActionProcessor)
	factoryOf(::ShareAppActionProcessor)
	factoryOf(::RateOnStoreActionProcessor)
	factoryOf(::ReportBugActionProcessor)
	factoryOf(::OpenUrlActionProcessor)
	factoryOf(::SetAnalyticsCollectionEnabledActionProcessor)
}
