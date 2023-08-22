package com.gdavidpb.tuindice.about.di

import com.gdavidpb.tuindice.about.presentation.viewmodel.AboutViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val aboutModule = module {
	/* ViewModel */

	viewModelOf(::AboutViewModel)
}