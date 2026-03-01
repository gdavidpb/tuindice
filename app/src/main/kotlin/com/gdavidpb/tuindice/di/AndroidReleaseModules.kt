package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.about.di.aboutAndroidModule
import com.gdavidpb.tuindice.login.di.loginAndroidModule
import com.gdavidpb.tuindice.persistence.di.persistenceAndroidModule
import com.gdavidpb.tuindice.summary.di.summaryAndroidModule
import org.koin.core.module.Module

fun androidReleaseModules(): List<Module> {
	return sharedModules(
		platformModules = listOf(
			androidAppModule,
			aboutAndroidModule,
			persistenceAndroidModule,
			loginAndroidModule,
			summaryAndroidModule
		)
	)
}
