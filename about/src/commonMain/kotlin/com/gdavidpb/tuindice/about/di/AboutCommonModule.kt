package com.gdavidpb.tuindice.about.di

import com.gdavidpb.tuindice.about.data.repository.AboutDataRepository
import com.gdavidpb.tuindice.about.domain.repository.AboutRepository
import com.gdavidpb.tuindice.about.domain.usecase.LoadVersionUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenStorePageUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenExternalUrlUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SendSupportEmailUseCase
import com.gdavidpb.tuindice.about.domain.usecase.ShareTextUseCase
import com.gdavidpb.tuindice.about.presentation.action.*
import com.gdavidpb.tuindice.about.presentation.viewmodel.AboutViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val aboutCommonModule = module {
	/* View models */

	factoryOf(::AboutViewModel)

	/* Use cases */

	factoryOf(::LoadVersionUseCase)
	factoryOf(::OpenExternalUrlUseCase)
	factoryOf(::SendSupportEmailUseCase)
	factoryOf(::ShareTextUseCase)
	factoryOf(::OpenStorePageUseCase)

	/* Repositories */

	factoryOf(::AboutDataRepository) { bind<AboutRepository>() }

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
