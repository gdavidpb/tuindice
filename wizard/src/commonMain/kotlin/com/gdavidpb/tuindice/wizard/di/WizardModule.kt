package com.gdavidpb.tuindice.wizard.di

import com.gdavidpb.tuindice.wizard.domain.usecase.MarkCoachmarkSeenUseCase
import com.gdavidpb.tuindice.wizard.domain.usecase.ResolveCoachmarkUseCase
import com.gdavidpb.tuindice.wizard.presentation.machine.CoachmarkOverlayMachine
import com.gdavidpb.tuindice.wizard.presentation.viewmodel.CoachmarkOverlayViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val wizardModule = module {
	viewModelOf(::CoachmarkOverlayViewModel)

	factoryOf(::CoachmarkOverlayMachine)

	factoryOf(::ResolveCoachmarkUseCase)
	factoryOf(::MarkCoachmarkSeenUseCase)
}
