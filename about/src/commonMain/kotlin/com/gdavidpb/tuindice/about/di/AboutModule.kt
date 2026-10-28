package com.gdavidpb.tuindice.about.di

import com.gdavidpb.tuindice.about.data.source.AboutDataSource
import com.gdavidpb.tuindice.about.domain.repository.AboutRepository
import com.gdavidpb.tuindice.about.domain.usecase.AssistantConsentWriteQueue
import com.gdavidpb.tuindice.about.domain.usecase.DeleteAssistantConversationsUseCase
import com.gdavidpb.tuindice.about.domain.usecase.LoadVersionNameUseCase
import com.gdavidpb.tuindice.about.domain.usecase.LoadVersionUseCase
import com.gdavidpb.tuindice.about.domain.usecase.ObserveAssistantAvailabilityUseCase
import com.gdavidpb.tuindice.about.domain.usecase.ObserveAssistantConsentUseCase
import com.gdavidpb.tuindice.about.domain.usecase.ObserveProfileIdentityUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenExternalUrlUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenStoreUseCase
import com.gdavidpb.tuindice.about.domain.usecase.RefreshAssistantConsentUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SendSupportEmailUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SetAssistantConsentUseCase
import com.gdavidpb.tuindice.about.presentation.machine.AboutExternalUseCases
import com.gdavidpb.tuindice.about.presentation.machine.AboutMachine
import com.gdavidpb.tuindice.about.presentation.machine.SettingsAssistantUseCases
import com.gdavidpb.tuindice.about.presentation.machine.SettingsContentUseCases
import com.gdavidpb.tuindice.about.presentation.machine.SettingsExternalUseCases
import com.gdavidpb.tuindice.about.presentation.machine.SettingsMachine
import com.gdavidpb.tuindice.about.presentation.resource.AboutTextProvider
import com.gdavidpb.tuindice.about.presentation.resource.DefaultAboutTextProvider
import com.gdavidpb.tuindice.about.presentation.resource.DefaultSettingsTextProvider
import com.gdavidpb.tuindice.about.presentation.resource.SettingsTextProvider
import com.gdavidpb.tuindice.about.presentation.viewmodel.AboutViewModel
import com.gdavidpb.tuindice.about.presentation.viewmodel.SettingsViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val aboutModule = module {
	/* View models */

	viewModelOf(::SettingsViewModel)
	viewModelOf(::AboutViewModel)

	factoryOf(::SettingsMachine)
	factoryOf(::AboutMachine)
	factoryOf(::DefaultSettingsTextProvider) { bind<SettingsTextProvider>() }
	factoryOf(::DefaultAboutTextProvider) { bind<AboutTextProvider>() }

	/* Use cases */

	factoryOf(::LoadVersionUseCase)
	factoryOf(::LoadVersionNameUseCase)
	factoryOf(::OpenExternalUrlUseCase)
	factoryOf(::SendSupportEmailUseCase)
	factoryOf(::OpenStoreUseCase)
	factoryOf(::ObserveProfileIdentityUseCase)
	factoryOf(::ObserveAssistantAvailabilityUseCase)
	factoryOf(::ObserveAssistantConsentUseCase)
	factoryOf(::SetAssistantConsentUseCase)
	singleOf(::AssistantConsentWriteQueue)
	factoryOf(::RefreshAssistantConsentUseCase)
	factoryOf(::DeleteAssistantConversationsUseCase)
	factoryOf(::SettingsContentUseCases)
	factoryOf(::SettingsAssistantUseCases)
	factoryOf(::SettingsExternalUseCases)
	factoryOf(::AboutExternalUseCases)

	/* Repositories */

	factoryOf(::AboutDataSource) { bind<AboutRepository>() }

}
