package com.gdavidpb.tuindice.summary.di

import com.gdavidpb.tuindice.summary.data.source.IosImageEncoderDataSource
import com.gdavidpb.tuindice.summary.domain.repository.EncoderRepository
import com.gdavidpb.tuindice.summary.presentation.route.IosProfilePictureActionsFactory
import com.gdavidpb.tuindice.summary.presentation.route.ProfilePictureActionsFactory
import com.gdavidpb.tuindice.summary.ui.view.IosProfilePictureViewRenderer
import com.gdavidpb.tuindice.summary.ui.view.ProfilePictureViewRenderer
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val summaryIosModule = module {
	factoryOf(::IosImageEncoderDataSource) { bind<EncoderRepository>() }
	factoryOf(::IosProfilePictureActionsFactory) { bind<ProfilePictureActionsFactory>() }
	factoryOf(::IosProfilePictureViewRenderer) { bind<ProfilePictureViewRenderer>() }
}
