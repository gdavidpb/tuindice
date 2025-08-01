package com.gdavidpb.tuindice.about.di

import com.gdavidpb.tuindice.about.data.repository.AboutDataRepository
import com.gdavidpb.tuindice.about.domain.repository.AboutRepository
import com.gdavidpb.tuindice.about.domain.usecase.LoadVersionUseCase
import com.gdavidpb.tuindice.about.presentation.action.ContactDeveloperActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.LoadVersionActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.OpenPrivacyPolicyActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.OpenTermsAndConditionsActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.OpenUrlActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.RateOnPlayStoreActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.ReportBugActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.ShareAppActionProcessor
import com.gdavidpb.tuindice.about.presentation.viewmodel.AboutViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val aboutModule = module {
	/* ViewModel */

	viewModelOf(::AboutViewModel)

	/* Use cases */
	factoryOf(::LoadVersionUseCase)

	/* Repositories */
	factory<AboutRepository> {
		AboutDataRepository(context = androidContext())
	}

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