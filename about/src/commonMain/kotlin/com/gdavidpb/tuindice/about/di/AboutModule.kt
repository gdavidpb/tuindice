package com.gdavidpb.tuindice.about.di

import com.gdavidpb.tuindice.about.data.source.AboutDataSource
import com.gdavidpb.tuindice.about.domain.repository.AboutRepository
import com.gdavidpb.tuindice.about.domain.usecase.DeleteAssistantConversationsUseCase
import com.gdavidpb.tuindice.about.domain.usecase.LoadVersionUseCase
import com.gdavidpb.tuindice.about.domain.usecase.ObserveAssistantAvailabilityUseCase
import com.gdavidpb.tuindice.about.domain.usecase.ObserveAssistantConsentUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenExternalUrlUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenStoreUseCase
import com.gdavidpb.tuindice.about.domain.usecase.RefreshAssistantConsentUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SendSupportEmailUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SetAssistantConsentUseCase
import com.gdavidpb.tuindice.about.presentation.machine.AboutAssistantUseCases
import com.gdavidpb.tuindice.about.presentation.machine.AboutExternalUseCases
import com.gdavidpb.tuindice.about.presentation.machine.AboutMachine
import com.gdavidpb.tuindice.about.presentation.resource.AboutTextProvider
import com.gdavidpb.tuindice.about.presentation.resource.DefaultAboutTextProvider
import com.gdavidpb.tuindice.about.presentation.viewmodel.AboutViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val aboutModule = module {
	/* View models */

	viewModelOf(::AboutViewModel)

	factoryOf(::AboutMachine)
	factoryOf(::DefaultAboutTextProvider) { bind<AboutTextProvider>() }

	/* Use cases */

	factoryOf(::LoadVersionUseCase)
	factoryOf(::OpenExternalUrlUseCase)
	factoryOf(::SendSupportEmailUseCase)
	factoryOf(::OpenStoreUseCase)
	factoryOf(::ObserveAssistantAvailabilityUseCase)
	factoryOf(::ObserveAssistantConsentUseCase)
	factoryOf(::SetAssistantConsentUseCase)
	factoryOf(::RefreshAssistantConsentUseCase)
	factoryOf(::DeleteAssistantConversationsUseCase)
	factoryOf(::AboutAssistantUseCases)
	factoryOf(::AboutExternalUseCases)

	/* Repositories */

	factoryOf(::AboutDataSource) { bind<AboutRepository>() }

}
