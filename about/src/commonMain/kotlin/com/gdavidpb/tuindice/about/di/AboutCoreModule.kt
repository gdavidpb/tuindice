package com.gdavidpb.tuindice.about.di

import com.gdavidpb.tuindice.about.data.repository.AboutDataRepository
import com.gdavidpb.tuindice.about.domain.repository.AboutRepository
import com.gdavidpb.tuindice.about.domain.repository.AboutVersionTextProvider
import com.gdavidpb.tuindice.about.domain.repository.DefaultAboutVersionTextProvider
import com.gdavidpb.tuindice.about.domain.usecase.LoadVersionUseCase
import com.gdavidpb.tuindice.about.presentation.action.ContactDeveloperActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.LoadVersionActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.OpenPrivacyPolicyActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.OpenTermsAndConditionsActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.OpenUrlActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.RateOnPlayStoreActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.ReportBugActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.ShareAppActionProcessor
import com.gdavidpb.tuindice.about.presentation.resource.AboutTextProvider
import com.gdavidpb.tuindice.about.presentation.resource.DefaultAboutTextProvider
import com.gdavidpb.tuindice.about.presentation.viewmodel.AboutViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val aboutCoreModule = module {
	/* View models */

	factoryOf(::AboutViewModel)

	/* Use cases */

	factoryOf(::LoadVersionUseCase)

	/* Repositories */

	factoryOf(::AboutDataRepository) { bind<AboutRepository>() }

	/* Shared text resources */

	factoryOf(::DefaultAboutTextProvider) { bind<AboutTextProvider>() }
	factoryOf(::DefaultAboutVersionTextProvider) { bind<AboutVersionTextProvider>() }

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
