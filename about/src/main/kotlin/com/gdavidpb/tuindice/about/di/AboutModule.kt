package com.gdavidpb.tuindice.about.di

import com.gdavidpb.tuindice.about.presentation.viewmodel.AboutViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.annotation.KoinReflectAPI
import org.koin.dsl.module

@KoinReflectAPI
val aboutModule = module {
	/* ViewModel */

	viewModel<AboutViewModel>()
}