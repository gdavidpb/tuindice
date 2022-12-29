package com.gdavidpb.tuindice.summary.di.modules

import com.gdavidpb.tuindice.summary.domain.usecase.*
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.annotation.KoinReflectAPI
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

@KoinReflectAPI
val summaryModule = module {
	/* View Models */

	viewModel<SummaryViewModel>()

	/* Use cases */

	factoryOf(::GetProfileUseCase)
	factoryOf(::UpdateProfilePictureUseCase)
	factoryOf(::GetProfilePictureUseCase)
	factoryOf(::RemoveProfilePictureUseCase)
}