package com.gdavidpb.tuindice.about.di

import com.gdavidpb.tuindice.about.presentation.action.ContactDeveloperActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.OpenPrivacyPolicyActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.OpenTermsAndConditionsActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.OpenUrlActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.RateOnPlayStoreActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.ReportBugActionProcessor
import com.gdavidpb.tuindice.about.presentation.action.ShareAppActionProcessor
import com.gdavidpb.tuindice.about.presentation.viewmodel.AboutViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val aboutModule = module {
	/* ViewModel */

	viewModelOf(::AboutViewModel)

	/* Action processor */

	factoryOf(::ContactDeveloperActionProcessor)
	factoryOf(::OpenTermsAndConditionsActionProcessor)
	factoryOf(::OpenPrivacyPolicyActionProcessor)
	factoryOf(::ShareAppActionProcessor)
	factoryOf(::RateOnPlayStoreActionProcessor)
	factoryOf(::ReportBugActionProcessor)
	factoryOf(::OpenUrlActionProcessor)
}