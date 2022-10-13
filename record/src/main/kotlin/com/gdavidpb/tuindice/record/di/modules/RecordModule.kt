package com.gdavidpb.tuindice.record.di.modules

import org.koin.core.annotation.KoinReflectAPI
import org.koin.dsl.module
import com.gdavidpb.tuindice.record.domain.usecase.GetEnrollmentProofUseCase
import com.gdavidpb.tuindice.record.domain.usecase.GetQuartersUseCase
import com.gdavidpb.tuindice.record.domain.usecase.RemoveQuarterUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpdateQuarterUseCase
import com.gdavidpb.tuindice.record.presentation.viewmodel.RecordViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.factoryOf

@KoinReflectAPI
val recordModule = module {
	/* View Models */

	viewModel<RecordViewModel>()

	/* Use cases */

	factoryOf(::GetQuartersUseCase)
	factoryOf(::UpdateQuarterUseCase)
	factoryOf(::GetEnrollmentProofUseCase)
	factoryOf(::RemoveQuarterUseCase)
}