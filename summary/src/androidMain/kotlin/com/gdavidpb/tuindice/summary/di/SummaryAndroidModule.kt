package com.gdavidpb.tuindice.summary.di

import com.gdavidpb.tuindice.summary.data.repository.user.PictureEncoderDataSource
import com.gdavidpb.tuindice.summary.data.repository.user.source.ImageEncoderDataSource
import com.gdavidpb.tuindice.summary.presentation.route.AndroidProfilePictureActionsFactory
import com.gdavidpb.tuindice.summary.presentation.route.ProfilePictureActionsFactory
import com.gdavidpb.tuindice.summary.ui.view.AndroidProfilePictureViewRenderer
import com.gdavidpb.tuindice.summary.ui.view.ProfilePictureViewRenderer
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val summaryAndroidModule = module {
	/* Android data sources */

	factoryOf(::ImageEncoderDataSource) { bind<PictureEncoderDataSource>() }
	factoryOf(::AndroidProfilePictureActionsFactory) { bind<ProfilePictureActionsFactory>() }
	factoryOf(::AndroidProfilePictureViewRenderer) { bind<ProfilePictureViewRenderer>() }
}
