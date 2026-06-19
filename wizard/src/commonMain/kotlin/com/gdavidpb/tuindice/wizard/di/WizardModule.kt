package com.gdavidpb.tuindice.wizard.di

import com.gdavidpb.tuindice.wizard.data.source.InMemoryWizardStartOverrideDataSource
import com.gdavidpb.tuindice.wizard.domain.repository.WizardStartOverrideRepository
import com.gdavidpb.tuindice.wizard.domain.usecase.CompleteWizardUseCase
import com.gdavidpb.tuindice.wizard.domain.usecase.ShouldStartWizardUseCase
import com.gdavidpb.tuindice.wizard.presentation.model.WizardTopBarActionBus
import com.gdavidpb.tuindice.wizard.presentation.machine.WizardMachine
import com.gdavidpb.tuindice.wizard.presentation.viewmodel.WizardViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val wizardModule = module {
	/* View models */

	viewModelOf(::WizardViewModel)

	factoryOf(::WizardMachine)

	/* Use cases */

	factoryOf(::ShouldStartWizardUseCase)
	factoryOf(::CompleteWizardUseCase)

	/* Shared wizard runtime */

	singleOf(::InMemoryWizardStartOverrideDataSource) { bind<WizardStartOverrideRepository>() }
	singleOf(::WizardTopBarActionBus)
}
